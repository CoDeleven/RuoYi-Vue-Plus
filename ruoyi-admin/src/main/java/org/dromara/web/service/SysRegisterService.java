package org.dromara.web.service;

import cn.hutool.crypto.digest.BCrypt;
import com.boxhilltravel.core.domain.CustomerContactInfo;
import com.boxhilltravel.core.domain.CustomerProfile;
import com.boxhilltravel.core.mapper.CustomerContactInfoMapper;
import com.boxhilltravel.core.mapper.CustomerProfileMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.Constants;
import org.dromara.common.core.constant.GlobalConstants;
import org.dromara.common.core.enums.UserType;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.exception.user.CaptchaException;
import org.dromara.common.core.exception.user.CaptchaExpireException;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.common.core.utils.MessageUtils;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.log.event.LoginInfoEvent;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.web.config.properties.CaptchaProperties;
import org.dromara.system.api.model.RegisterBody;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.bo.SysUserBo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.service.ISysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 注册校验方法
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysRegisterService {

    private final ISysUserService userService;
    private final SysUserMapper userMapper;
    private final CustomerProfileMapper customerProfileMapper;
    private final CustomerContactInfoMapper customerContactInfoMapper;
    private final CaptchaProperties captchaProperties;

    /**
     * 注册
     *
     * @param registerBody 注册请求参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterBody registerBody) {
        UserType userTypeEnum = UserType.getUserType(StringUtils.blankToDefault(registerBody.getUserType(), UserType.SYS_USER.getUserType()));
        boolean appUser = UserType.APP_USER == userTypeEnum;
        String username = appUser ? normalizeEmail(registerBody.getEmail()) : registerBody.getUsername();
        String password = registerBody.getPassword();
        // 校验用户类型是否存在
        String userType = userTypeEnum.getUserType();

        boolean captchaEnabled = captchaProperties.getEnable();
        // 验证码开关
        if (captchaEnabled && !appUser) {
            validateCaptcha(username, registerBody.getCode(), registerBody.getUuid());
        }
        if (appUser) {
            validateEmailCode(username, registerBody.getEmailCode());
        }
        SysUserBo sysUser = new SysUserBo();
        sysUser.setUserName(username);
        sysUser.setNickName(username);
        sysUser.setEmail(appUser ? username : registerBody.getEmail());
        sysUser.setPassword(BCrypt.hashpw(password));
        sysUser.setUserType(userType);

        boolean exist = userMapper.lambda()
            .eq(SysUser::getUserName, sysUser.getUserName())
            .exists();
        if (exist) {
            throw new UserException("user.register.save.error", username);
        }
        boolean emailExist = StringUtils.isNotBlank(sysUser.getEmail()) && userMapper.lambda()
            .eq(SysUser::getEmail, sysUser.getEmail())
            .exists();
        if (emailExist) {
            throw new ServiceException("Email address already exists");
        }
        boolean regFlag = userService.registerUser(sysUser);
        if (!regFlag) {
            throw new UserException("user.register.error");
        }
        if (appUser) {
            initCustomerExtensions(username);
        }
        recordLoginInfo(username, Constants.REGISTER, MessageUtils.message("user.register.success"));
    }

    private void validateEmailCode(String email, String emailCode) {
        if (StringUtils.isBlank(emailCode)) {
            throw new ServiceException("Email verification code cannot be empty");
        }
        String verifyKey = GlobalConstants.CAPTCHA_CODE_KEY + email;
        String captcha = RedisUtils.getCacheObject(verifyKey);
        RedisUtils.deleteObject(verifyKey);
        if (captcha == null) {
            throw new ServiceException("Email verification code has expired");
        }
        if (!StringUtils.equalsIgnoreCase(emailCode, captcha)) {
            throw new ServiceException("Email verification code is incorrect");
        }
    }

    private void initCustomerExtensions(String username) {
        SysUser user = userMapper.lambda()
            .eq(SysUser::getUserName, username)
            .one();
        if (user == null) {
            throw new UserException("user.register.error");
        }
        LocalDateTime now = LocalDateTime.now();
        if (customerProfileMapper.selectById(user.getUserId()) == null) {
            CustomerProfile profile = new CustomerProfile();
            profile.setCustomerUserId(user.getUserId());
            profile.setNickname(user.getNickName());
            profile.setCreatedAt(now);
            profile.setUpdatedAt(now);
            customerProfileMapper.insert(profile);
        }
        if (customerContactInfoMapper.selectById(user.getUserId()) == null) {
            CustomerContactInfo contactInfo = new CustomerContactInfo();
            contactInfo.setCustomerUserId(user.getUserId());
            contactInfo.setContactEmail(user.getEmail());
            contactInfo.setCreatedAt(now);
            contactInfo.setUpdatedAt(now);
            customerContactInfoMapper.insert(contactInfo);
        }
    }

    private String normalizeEmail(String email) {
        String value = StringUtils.trim(email);
        if (StringUtils.isBlank(value)) {
            throw new ServiceException("Email address cannot be empty");
        }
        return value.toLowerCase();
    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code     验证码
     * @param uuid     唯一标识
     */
    public void validateCaptcha(String username, String code, String uuid) {
        String verifyKey = GlobalConstants.CAPTCHA_CODE_KEY + StringUtils.blankToDefault(uuid, "");
        String captcha = RedisUtils.getCacheObject(verifyKey);
        RedisUtils.deleteObject(verifyKey);
        if (captcha == null) {
            recordLoginInfo(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire"));
            throw new CaptchaExpireException();
        }
        if (!StringUtils.equalsIgnoreCase(code, captcha)) {
            recordLoginInfo(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error"));
            throw new CaptchaException();
        }
    }

    /**
     * 记录登录信息
     *
     * @param username 用户名
     * @param status   状态
     * @param message  消息内容
     */
    private void recordLoginInfo(String username, String status, String message) {
        LoginInfoEvent loginInfoEvent = new LoginInfoEvent();
        loginInfoEvent.setUsername(username);
        loginInfoEvent.setStatus(status);
        loginInfoEvent.setMessage(message);
        HttpServletRequest request = ServletUtils.getRequest();
        if (request != null) {
            loginInfoEvent.setIp(ServletUtils.getClientIP(request));
            loginInfoEvent.setUserAgent(request.getHeader("User-Agent"));
            loginInfoEvent.setClientId(request.getHeader(LoginHelper.CLIENT_KEY));
        }
        SpringUtils.context().publishEvent(loginInfoEvent);
    }

}
