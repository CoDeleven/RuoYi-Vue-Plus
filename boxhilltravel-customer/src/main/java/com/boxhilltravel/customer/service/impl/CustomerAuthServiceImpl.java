package com.boxhilltravel.customer.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.lock.annotation.Lock4j;
import com.boxhilltravel.core.domain.CustomerContactInfo;
import com.boxhilltravel.core.domain.CustomerProfile;
import com.boxhilltravel.core.mapper.CustomerContactInfoMapper;
import com.boxhilltravel.core.mapper.CustomerProfileMapper;
import com.boxhilltravel.customer.domain.bo.CustomerSocialLoginBo;
import com.boxhilltravel.customer.domain.vo.CustomerLoginVo;
import com.boxhilltravel.customer.service.ICustomerAuthService;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.enums.UserType;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.social.config.properties.SocialLoginConfigProperties;
import org.dromara.common.social.config.properties.SocialProperties;
import org.dromara.common.social.utils.SocialUtils;
import org.dromara.system.api.model.LoginUser;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.bo.SysSocialBo;
import org.dromara.system.domain.bo.SysUserBo;
import org.dromara.system.domain.vo.SysClientVo;
import org.dromara.system.domain.vo.SysSocialVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.service.ISysClientService;
import org.dromara.system.service.ISysSocialService;
import org.dromara.system.service.ISysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Customer authentication service implementation.
 */
@RequiredArgsConstructor
@Service
public class CustomerAuthServiceImpl implements ICustomerAuthService {

    private static final String GOOGLE = "google";
    private static final Set<String> SUPPORTED_SOURCES = Set.of("facebook", GOOGLE);
    private static final String SOCIAL_GRANT_TYPE = "social";
    private static final int USERNAME_MAX_LENGTH = 30;
    private static final int EMAIL_MAX_LENGTH = 50;

    private final SocialProperties socialProperties;
    private final ISysClientService clientService;
    private final ISysSocialService socialService;
    private final ISysUserService userService;
    private final SysUserMapper userMapper;
    private final CustomerProfileMapper customerProfileMapper;
    private final CustomerContactInfoMapper customerContactInfoMapper;

    @Override
    public String socialBindingUrl(String source) {
        String normalizedSource = normalizeSource(source);
        SocialLoginConfigProperties socialConfig = socialProperties.getType().get(normalizedSource);
        if (socialConfig == null) {
            throw new ServiceException(normalizedSource + " social login is not supported");
        }
        AuthRequest authRequest = SocialUtils.getAuthRequest(normalizedSource, socialProperties);
        return authRequest.authorize(AuthStateUtils.createState());
    }

    @Override
    @Lock4j
    @Transactional(rollbackFor = Exception.class)
    public CustomerLoginVo socialLogin(CustomerSocialLoginBo bo) {
        String source = normalizeSource(bo.getSource());
        SysClientVo client = loadSocialClient(bo.getClientId());
        AuthUser authUser = loadAuthUser(source, bo.getSocialCode(), bo.getSocialState());
        Long userId = resolveCustomerUserId(authUser);
        SysUserVo user = loadLoginUser(userId);
        LoginHelper.login(buildLoginUser(user, client), buildLoginParameter(client));
        return buildLoginVo(client);
    }

    private SysClientVo loadSocialClient(String clientId) {
        SysClientVo client = clientService.queryByClientId(clientId);
        if (client == null || !StringUtils.contains(client.getGrantType(), SOCIAL_GRANT_TYPE)) {
            throw new ServiceException("Client is not allowed to use social login");
        }
        if (!SystemConstants.NORMAL.equals(client.getStatus())) {
            throw new ServiceException("Client is disabled");
        }
        return client;
    }

    private AuthUser loadAuthUser(String source, String socialCode, String socialState) {
        AuthResponse<AuthUser> response = SocialUtils.loginAuth(source, socialCode, socialState, socialProperties);
        if (!response.ok()) {
            throw new ServiceException(response.getMsg());
        }
        AuthUser authUser = response.getData();
        if (authUser == null || StringUtils.isBlank(authUser.getUuid())) {
            throw new ServiceException("Failed to get social account information");
        }
        return authUser;
    }

    private Long resolveCustomerUserId(AuthUser authUser) {
        String authId = buildAuthId(authUser);
        List<SysSocialVo> bindings = socialService.selectByAuthId(authId);
        if (CollUtil.isNotEmpty(bindings)) {
            return bindings.getFirst().getUserId();
        }

        SysUserVo existing = loadAppUserByEmail(authUser.getEmail());
        Long userId = existing == null ? createSocialCustomer(authUser) : existing.getUserId();
        saveSocialBinding(userId, authUser);
        return userId;
    }

    private SysUserVo loadAppUserByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (StringUtils.isBlank(normalizedEmail)) {
            return null;
        }
        return userMapper.lambda()
            .eq(SysUser::getEmail, normalizedEmail)
            .eq(SysUser::getUserType, UserType.APP_USER.getUserType())
            .voOne();
    }

    private Long createSocialCustomer(AuthUser authUser) {
        String email = resolveEmail(authUser);
        String username = resolveUsername(authUser, email);
        SysUserBo user = new SysUserBo();
        user.setUserName(username);
        user.setNickName(resolveNickname(authUser, username));
        user.setEmail(email);
        user.setPassword(BCrypt.hashpw(UUID.randomUUID().toString()));
        user.setUserType(UserType.APP_USER.getUserType());
        user.setStatus(SystemConstants.NORMAL);
        if (!userService.registerUser(user)) {
            throw new UserException("user.register.error");
        }

        SysUser created = userMapper.lambda()
            .eq(SysUser::getUserName, username)
            .eq(SysUser::getUserType, UserType.APP_USER.getUserType())
            .one();
        if (created == null) {
            throw new UserException("user.register.error");
        }
        initCustomerExtensions(created.getUserId(), created.getNickName(), created.getEmail(), authUser.getAvatar());
        return created.getUserId();
    }

    private void initCustomerExtensions(Long userId, String nickname, String email, String avatarUrl) {
        LocalDateTime now = LocalDateTime.now();
        if (customerProfileMapper.selectById(userId) == null) {
            CustomerProfile profile = new CustomerProfile();
            profile.setCustomerUserId(userId);
            profile.setNickname(nickname);
            profile.setAvatarUrl(StringUtils.trim(avatarUrl));
            profile.setCreatedAt(now);
            profile.setUpdatedAt(now);
            customerProfileMapper.insert(profile);
        }
        if (customerContactInfoMapper.selectById(userId) == null) {
            CustomerContactInfo contactInfo = new CustomerContactInfo();
            contactInfo.setCustomerUserId(userId);
            contactInfo.setContactEmail(email);
            contactInfo.setCreatedAt(now);
            contactInfo.setUpdatedAt(now);
            customerContactInfoMapper.insert(contactInfo);
        }
    }

    private void saveSocialBinding(Long userId, AuthUser authUser) {
        SysSocialBo social = new SysSocialBo();
        social.setUserId(userId);
        social.setAuthId(buildAuthId(authUser));
        social.setSource(normalizeSource(authUser.getSource()));
        social.setOpenId(authUser.getUuid());
        social.setUserName(StringUtils.blankToDefault(authUser.getUsername(), authUser.getUuid()));
        social.setNickName(resolveNickname(authUser, authUser.getUuid()));
        social.setEmail(normalizeEmail(authUser.getEmail()));
        social.setAvatar(StringUtils.trim(authUser.getAvatar()));

        AuthToken token = authUser.getToken();
        if (token != null) {
            social.setAccessToken(token.getAccessToken());
            social.setExpireIn(token.getExpireIn());
            social.setRefreshToken(token.getRefreshToken());
            social.setAccessCode(token.getAccessCode());
            social.setUnionId(token.getUnionId());
            social.setScope(token.getScope());
            social.setTokenType(token.getTokenType());
            social.setIdToken(token.getIdToken());
            social.setMacAlgorithm(token.getMacAlgorithm());
            social.setMacKey(token.getMacKey());
            social.setCode(token.getCode());
            social.setOauthToken(token.getOauthToken());
            social.setOauthTokenSecret(token.getOauthTokenSecret());
        }
        socialService.insertByBo(social);
    }

    private SysUserVo loadLoginUser(Long userId) {
        SysUserVo user = userMapper.selectVoById(userId);
        if (user == null) {
            throw new UserException("user.not.exists", "");
        }
        if (SystemConstants.DISABLE.equals(user.getStatus())) {
            throw new UserException("user.blocked", user.getUserName());
        }
        if (!UserType.APP_USER.getUserType().equals(user.getUserType())) {
            throw new ServiceException("Only customer accounts can use this login endpoint");
        }
        return user;
    }

    private LoginUser buildLoginUser(SysUserVo user, SysClientVo client) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setDeptId(user.getDeptId());
        loginUser.setUsername(user.getUserName());
        loginUser.setNickname(user.getNickName());
        loginUser.setUserType(user.getUserType());
        loginUser.setClientKey(client.getClientKey());
        loginUser.setDeviceType(client.getDeviceType());
        return loginUser;
    }

    private SaLoginParameter buildLoginParameter(SysClientVo client) {
        SaLoginParameter model = new SaLoginParameter();
        model.setDeviceType(client.getDeviceType());
        model.setTimeout(client.getTimeout());
        model.setActiveTimeout(client.getActiveTimeout());
        model.setExtra(LoginHelper.CLIENT_KEY, client.getClientId());
        model.setExtra(LoginHelper.CLIENT_ACCESS_PATH_KEY, client.getAccessPath());
        model.setExtra(LoginHelper.CLIENT_IP_WHITELIST_KEY, client.getIpWhitelist());
        return model;
    }

    private CustomerLoginVo buildLoginVo(SysClientVo client) {
        CustomerLoginVo loginVo = new CustomerLoginVo();
        loginVo.setAccessToken(StpUtil.getTokenValue());
        loginVo.setExpireIn(StpUtil.getTokenTimeout());
        loginVo.setClientId(client.getClientId());
        return loginVo;
    }

    private String buildAuthId(AuthUser authUser) {
        return normalizeSource(authUser.getSource()) + authUser.getUuid();
    }

    private String normalizeSource(String source) {
        String normalized = StringUtils.trim(source).toLowerCase(Locale.ROOT);
        if (!SUPPORTED_SOURCES.contains(normalized)) {
            throw new ServiceException("Only facebook and google social login are supported");
        }
        return normalized;
    }

    private String normalizeEmail(String email) {
        String normalized = StringUtils.trim(email).toLowerCase(Locale.ROOT);
        return StringUtils.isBlank(normalized) ? null : normalized;
    }

    private String resolveEmail(AuthUser authUser) {
        String email = normalizeEmail(authUser.getEmail());
        if (StringUtils.isNotBlank(email) && email.length() <= EMAIL_MAX_LENGTH) {
            return email;
        }
        String source = normalizeSource(authUser.getSource());
        String domain = "@" + source + ".local";
        return truncate(source + "_" + sanitize(authUser.getUuid()), EMAIL_MAX_LENGTH - domain.length()) + domain;
    }

    private String resolveUsername(AuthUser authUser, String email) {
        String source = normalizeSource(authUser.getSource());
        String username = email.length() <= USERNAME_MAX_LENGTH
            ? email
            : source + "_" + sanitize(authUser.getUuid());
        username = truncate(username, USERNAME_MAX_LENGTH);
        if (!userMapper.lambda().eq(SysUser::getUserName, username).exists()) {
            return username;
        }
        String base = truncate(source + "_" + sanitize(authUser.getUuid()), USERNAME_MAX_LENGTH - 5);
        for (int i = 1; i <= 9999; i++) {
            String candidate = base + "_" + i;
            if (!userMapper.lambda().eq(SysUser::getUserName, candidate).exists()) {
                return candidate;
            }
        }
        throw new ServiceException("Unable to allocate customer username");
    }

    private String resolveNickname(AuthUser authUser, String fallback) {
        String nickname = StringUtils.blankToDefault(authUser.getNickname(), authUser.getUsername());
        return truncate(StringUtils.blankToDefault(nickname, fallback), USERNAME_MAX_LENGTH);
    }

    private String sanitize(String value) {
        String sanitized = StringUtils.blankToDefault(value, UUID.randomUUID().toString())
            .replaceAll("[^A-Za-z0-9_]", "");
        return StringUtils.blankToDefault(sanitized, UUID.randomUUID().toString().replace("-", ""));
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

}
