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
import com.boxhilltravel.customer.domain.bo.CustomerSocialBindingBo;
import com.boxhilltravel.customer.domain.bo.CustomerSocialLoginBo;
import com.boxhilltravel.customer.domain.vo.CustomerLoginVo;
import com.boxhilltravel.customer.domain.vo.CustomerSocialBindingVo;
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
import org.dromara.common.redis.utils.RedisUtils;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CustomerAuthServiceImpl implements ICustomerAuthService {

    private static final Set<String> SUPPORTED_SOURCES = Set.of("facebook", "google");
    private static final String SOCIAL_GRANT_TYPE = "social";
    private static final String BIND_STATE_KEY = "account:security:social-state:";
    private static final int USERNAME_MAX_LENGTH = 30;

    private final SocialProperties socialProperties;
    private final ISysClientService clientService;
    private final ISysSocialService socialService;
    private final ISysUserService userService;
    private final SysUserMapper userMapper;
    private final CustomerProfileMapper customerProfileMapper;
    private final CustomerContactInfoMapper customerContactInfoMapper;

    @Override
    public String socialBindingUrl(String source) {
        return buildAuthorizationUrl(normalizeSource(source), null);
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

    @Override
    public String accountBindingUrl(String source, Long userId) {
        if (userId == null) {
            throw new ServiceException("User not logged in");
        }
        return buildAuthorizationUrl(normalizeSource(source), userId);
    }

    @Override
    @Lock4j
    @Transactional(rollbackFor = Exception.class)
    public void bindAccount(CustomerSocialBindingBo bo, Long userId) {
        String source = normalizeSource(bo.getSource());
        String stateUserId = RedisUtils.getCacheObject(BIND_STATE_KEY + bo.getSocialState());
        RedisUtils.deleteObject(BIND_STATE_KEY + bo.getSocialState());
        if (!StringUtils.equals(String.valueOf(userId), stateUserId)) {
            throw new ServiceException("Social binding request has expired");
        }
        AuthUser authUser = loadAuthUser(source, bo.getSocialCode(), bo.getSocialState());
        String authId = buildAuthId(authUser);
        List<SysSocialVo> sameIdentity = socialService.selectByAuthId(authId);
        if (CollUtil.isNotEmpty(sameIdentity)) {
            if (userId.equals(sameIdentity.getFirst().getUserId())) {
                return;
            }
            throw new ServiceException("This social account is already used by another account");
        }
        boolean providerAlreadyBound = socialService.queryListByUserId(userId).stream()
            .anyMatch(item -> source.equalsIgnoreCase(item.getSource()));
        if (providerAlreadyBound) {
            throw new ServiceException("A different " + source + " account is already bound");
        }
        saveSocialBinding(userId, authUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindAccount(String source, Long userId) {
        String normalized = normalizeSource(source);
        SysSocialVo binding = socialService.queryListByUserId(userId).stream()
            .filter(item -> normalized.equalsIgnoreCase(item.getSource()))
            .findFirst()
            .orElseThrow(() -> new ServiceException("Social account is not bound"));
        if (!socialService.deleteWithValidById(binding.getId())) {
            throw new ServiceException("Failed to unbind social account");
        }
    }

    @Override
    public List<CustomerSocialBindingVo> listAccountBindings(Long userId) {
        List<SysSocialVo> bindings = socialService.queryListByUserId(userId);
        List<CustomerSocialBindingVo> result = new ArrayList<>(2);
        for (String source : List.of("facebook", "google")) {
            SysSocialVo binding = bindings.stream()
                .filter(item -> source.equalsIgnoreCase(item.getSource()))
                .findFirst().orElse(null);
            CustomerSocialBindingVo vo = new CustomerSocialBindingVo();
            vo.setSource(source);
            vo.setBound(binding != null);
            vo.setDisplayName(binding == null ? null : StringUtils.blankToDefault(binding.getNickName(), binding.getUserName()));
            vo.setAvatar(binding == null ? null : binding.getAvatar());
            result.add(vo);
        }
        return result;
    }

    private String buildAuthorizationUrl(String source, Long bindingUserId) {
        SocialLoginConfigProperties config = socialProperties.getType().get(source);
        if (config == null) {
            throw new ServiceException(source + " social login is not supported");
        }
        String state = AuthStateUtils.createState();
        if (bindingUserId != null) {
            RedisUtils.setCacheObject(BIND_STATE_KEY + state, String.valueOf(bindingUserId), Duration.ofMinutes(3));
        }
        AuthRequest authRequest = SocialUtils.getAuthRequest(source, socialProperties);
        return authRequest.authorize(state);
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

    private AuthUser loadAuthUser(String source, String code, String state) {
        AuthResponse<AuthUser> response = SocialUtils.loginAuth(source, code, state, socialProperties);
        if (!response.ok() || response.getData() == null || StringUtils.isBlank(response.getData().getUuid())) {
            throw new ServiceException(response.getMsg() == null ? "Failed to get social account information" : response.getMsg());
        }
        return response.getData();
    }

    private Long resolveCustomerUserId(AuthUser authUser) {
        List<SysSocialVo> bindings = socialService.selectByAuthId(buildAuthId(authUser));
        if (CollUtil.isNotEmpty(bindings)) {
            return bindings.getFirst().getUserId();
        }
        Long userId = createSocialCustomer(authUser);
        saveSocialBinding(userId, authUser);
        return userId;
    }

    private Long createSocialCustomer(AuthUser authUser) {
        String username = resolveUsername(authUser);
        SysUserBo user = new SysUserBo();
        user.setUserName(username);
        user.setNickName(resolveNickname(authUser, username));
        user.setEmail(null);
        user.setEmailVerified(false);
        user.setPassword(BCrypt.hashpw(UUID.randomUUID().toString()));
        user.setPasswordConfigured(false);
        user.setUserType(UserType.APP_USER.getUserType());
        user.setStatus(SystemConstants.NORMAL);
        if (!userService.registerUser(user)) {
            throw new UserException("user.register.error");
        }
        SysUser created = userMapper.lambda().eq(SysUser::getUserName, username).one();
        if (created == null) {
            throw new UserException("user.register.error");
        }
        initCustomerExtensions(created.getUserId(), created.getNickName(), authUser.getAvatar());
        return created.getUserId();
    }

    private void initCustomerExtensions(Long userId, String nickname, String avatarUrl) {
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
        social.setEmail(null);
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
        }
        if (!socialService.insertByBo(social)) {
            throw new ServiceException("Failed to bind social account");
        }
    }

    private SysUserVo loadLoginUser(Long userId) {
        SysUserVo user = userMapper.selectVoById(userId);
        if (user == null) throw new UserException("user.not.exists", "");
        if (SystemConstants.DISABLE.equals(user.getStatus())) throw new UserException("user.blocked", user.getUserName());
        if (!UserType.APP_USER.getUserType().equals(user.getUserType())) throw new ServiceException("Only customer accounts can use this login endpoint");
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
        CustomerLoginVo vo = new CustomerLoginVo();
        vo.setAccessToken(StpUtil.getTokenValue());
        vo.setExpireIn(StpUtil.getTokenTimeout());
        vo.setClientId(client.getClientId());
        return vo;
    }

    private String buildAuthId(AuthUser user) { return normalizeSource(user.getSource()) + user.getUuid(); }

    private String normalizeSource(String source) {
        String value = StringUtils.trim(source);
        if (StringUtils.isBlank(value) || !SUPPORTED_SOURCES.contains(value.toLowerCase(Locale.ROOT))) {
            throw new ServiceException("Only facebook and google are supported");
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private String resolveUsername(AuthUser user) {
        String base = truncate(normalizeSource(user.getSource()) + "_" + sanitize(user.getUuid()), USERNAME_MAX_LENGTH);
        if (!userMapper.lambda().eq(SysUser::getUserName, base).exists()) return base;
        for (int i = 1; i <= 9999; i++) {
            String suffix = "_" + i;
            String candidate = truncate(base, USERNAME_MAX_LENGTH - suffix.length()) + suffix;
            if (!userMapper.lambda().eq(SysUser::getUserName, candidate).exists()) return candidate;
        }
        throw new ServiceException("Unable to allocate customer username");
    }

    private String resolveNickname(AuthUser user, String fallback) {
        String nickname = StringUtils.blankToDefault(user.getNickname(), user.getUsername());
        return truncate(StringUtils.blankToDefault(nickname, fallback), USERNAME_MAX_LENGTH);
    }

    private String sanitize(String value) {
        String result = StringUtils.blankToDefault(value, UUID.randomUUID().toString()).replaceAll("[^A-Za-z0-9_]", "");
        return StringUtils.blankToDefault(result, UUID.randomUUID().toString().replace("-", ""));
    }

    private String truncate(String value, int max) { return value != null && value.length() > max ? value.substring(0, max) : value; }
}
