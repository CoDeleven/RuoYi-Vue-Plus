package com.boxhilltravel.customer.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.boxhilltravel.core.domain.CustomerContactInfo;
import com.boxhilltravel.core.domain.CustomerProfile;
import com.boxhilltravel.core.mapper.CustomerContactInfoMapper;
import com.boxhilltravel.core.mapper.CustomerProfileMapper;
import com.boxhilltravel.customer.domain.bo.CustomerEmailCodeBo;
import com.boxhilltravel.customer.domain.bo.CustomerSocialBindingBo;
import com.boxhilltravel.customer.domain.bo.UpdateCustomerEmailBo;
import com.boxhilltravel.customer.domain.bo.UpdateCustomerPasswordBo;
import com.boxhilltravel.customer.domain.bo.UpdateCustomerProfileBo;
import com.boxhilltravel.customer.domain.vo.ContactInfoVo;
import com.boxhilltravel.customer.domain.vo.CustomerAccountProfileVo;
import com.boxhilltravel.customer.domain.vo.CustomerAccountSecurityVo;
import com.boxhilltravel.customer.service.ICustomerAccountService;
import com.boxhilltravel.customer.service.ICustomerAuthService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.regex.RegexValidator;
import org.dromara.common.mail.config.properties.MailProperties;
import org.dromara.common.mail.core.MailBuilder;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.service.ISysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class CustomerAccountServiceImpl implements ICustomerAccountService {

    private static final Pattern CONTACT_EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Set<String> CODE_PURPOSES = Set.of("bind_email", "confirm_old_email", "confirm_new_email", "set_password");
    private static final String CODE_KEY = "account:security:email-code:";
    private static final Duration CODE_TTL = Duration.ofMinutes(5);

    private final ISysUserService userService;
    private final SysUserMapper userMapper;
    private final CustomerProfileMapper profileMapper;
    private final CustomerContactInfoMapper contactInfoMapper;
    private final ICustomerAuthService authService;
    private final MailProperties mailProperties;

    @Override
    public CustomerAccountProfileVo getProfile() {
        return buildProfileResp(currentUserId());
    }

    @Override
    public CustomerAccountProfileVo updateProfile(UpdateCustomerProfileBo bo) {
        Long userId = currentUserId();
        loadSysUser(userId);
        CustomerContactInfo contactInfo = normalizeContactInfo(userId, bo.getContactInfo());
        CustomerContactInfo existing = contactInfoMapper.selectById(userId);
        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            contactInfo.setCreatedAt(now);
            contactInfo.setUpdatedAt(now);
            contactInfoMapper.insert(contactInfo);
        } else {
            contactInfo.setUpdatedAt(now);
            contactInfoMapper.updateById(contactInfo);
        }
        return buildProfileResp(userId);
    }

    @Override
    public CustomerAccountSecurityVo getSecurity() {
        Long userId = currentUserId();
        SysUserVo user = loadSysUser(userId);
        CustomerAccountSecurityVo vo = new CustomerAccountSecurityVo();
        vo.setPasswordConfigured(Boolean.TRUE.equals(user.getPasswordConfigured()));
        vo.setEmail(user.getEmail());
        vo.setMaskedEmail(maskEmail(user.getEmail()));
        vo.setEmailVerified(Boolean.TRUE.equals(user.getEmailVerified()));
        vo.setSocialBindings(authService.listAccountBindings(userId));
        return vo;
    }

    @Override
    public void sendEmailCode(CustomerEmailCodeBo bo) {
        Long userId = currentUserId();
        SysUserVo user = loadSysUser(userId);
        String purpose = normalizePurpose(bo.getPurpose());
        String target;
        if ("confirm_old_email".equals(purpose) || "set_password".equals(purpose)) {
            requireVerifiedEmail(user);
            target = normalizeEmail(user.getEmail());
        } else {
            target = normalizeEmail(bo.getEmail());
            ensureEmailAvailable(target, userId);
            if ("bind_email".equals(purpose) && StringUtils.isNotBlank(user.getEmail())) {
                throw new ServiceException("Use email replacement for an existing email address");
            }
            if ("confirm_new_email".equals(purpose) && StringUtils.isBlank(user.getEmail())) {
                throw new ServiceException("Use email binding for an account without an email address");
            }
        }
        if (!mailProperties.getEnabled()) {
            throw new ServiceException("Email service is not available");
        }
        String code = RandomUtil.randomNumbers(4);
        MailBuilder.of().to(target).subject("Account security verification").text(
            "Your verification code is **" + code + "**. It is valid for **5 minutes**. Do not share it with anyone."
        ).send();
        RedisUtils.setCacheObject(codeKey(purpose, userId, target), code, CODE_TTL);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmail(UpdateCustomerEmailBo bo) {
        Long userId = currentUserId();
        SysUserVo user = loadSysUser(userId);
        String newEmail = normalizeEmail(bo.getNewEmail());
        ensureEmailAvailable(newEmail, userId);
        if (StringUtils.isBlank(user.getEmail())) {
            consumeCode("bind_email", userId, newEmail, bo.getNewEmailCode());
        } else {
            requireVerifiedEmail(user);
            consumeCode("confirm_old_email", userId, normalizeEmail(user.getEmail()), bo.getOldEmailCode());
            consumeCode("confirm_new_email", userId, newEmail, bo.getNewEmailCode());
        }
        int rows = userMapper.lambda()
            .set(SysUser::getEmail, newEmail)
            .set(SysUser::getUserName, newEmail)
            .set(SysUser::getEmailVerified, true)
            .eq(SysUser::getUserId, userId)
            .updateCount();
        if (rows != 1) throw new ServiceException("Failed to update email address");
        syncContactEmail(userId, newEmail);
    }

    @Override
    public void updatePassword(UpdateCustomerPasswordBo bo) {
        Long userId = currentUserId();
        SysUserVo user = loadSysUser(userId);
        if (Boolean.TRUE.equals(user.getPasswordConfigured())) {
            if (StringUtils.isBlank(bo.getCurrentPassword()) || !BCrypt.checkpw(bo.getCurrentPassword(), user.getPassword())) {
                throw new ServiceException("Current password is incorrect");
            }
        } else {
            requireVerifiedEmail(user);
            consumeCode("set_password", userId, normalizeEmail(user.getEmail()), bo.getEmailCode());
        }
        if (BCrypt.checkpw(bo.getNewPassword(), user.getPassword())) {
            throw new ServiceException("New password must be different from the current password");
        }
        int rows = userMapper.lambda()
            .set(SysUser::getPassword, BCrypt.hashpw(bo.getNewPassword()))
            .set(SysUser::getPasswordConfigured, true)
            .eq(SysUser::getUserId, userId)
            .updateCount();
        if (rows != 1) throw new ServiceException("Failed to update password");
    }

    @Override
    public String socialBindingUrl(String source) {
        return authService.accountBindingUrl(source, currentUserId());
    }

    @Override
    public void bindSocial(CustomerSocialBindingBo bo) {
        authService.bindAccount(bo, currentUserId());
    }

    @Override
    public void unbindSocial(String source) {
        SysUserVo user = loadSysUser(currentUserId());
        requireVerifiedEmail(user);
        authService.unbindAccount(source, user.getUserId());
    }

    private CustomerAccountProfileVo buildProfileResp(Long userId) {
        SysUserVo user = loadSysUser(userId);
        CustomerProfile profile = profileMapper.selectById(userId);
        CustomerContactInfo contactInfo = contactInfoMapper.selectById(userId);
        CustomerAccountProfileVo vo = new CustomerAccountProfileVo();
        vo.setId(user.getUserId());
        vo.setUsername(user.getUserName());
        vo.setEmail(user.getEmail());
        vo.setNickname(profile != null && StringUtils.isNotBlank(profile.getNickname()) ? profile.getNickname() : user.getNickName());
        vo.setAvatar(user.getAvatar());
        vo.setAvatarUrl(profile == null ? null : profile.getAvatarUrl());
        vo.setStatus(user.getStatus());
        vo.setEmailVerified(Boolean.TRUE.equals(user.getEmailVerified()));
        vo.setCreatedAt(user.getCreateTime());
        vo.setContactInfo(toContactInfoVo(contactInfo));
        return vo;
    }

    private Long currentUserId() {
        Long userId = LoginHelper.getUserId();
        if (userId == null) throw new ServiceException("User not logged in");
        return userId;
    }

    private SysUserVo loadSysUser(Long userId) {
        SysUserVo user = userService.selectUserById(userId);
        if (user == null) throw new ServiceException("User not found");
        return user;
    }

    private void requireVerifiedEmail(SysUserVo user) {
        if (StringUtils.isBlank(user.getEmail()) || !Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new ServiceException("A verified email address is required");
        }
    }

    private void ensureEmailAvailable(String email, Long userId) {
        boolean occupied = userMapper.lambda()
            .and(group -> group.eq(SysUser::getEmail, email).or().eq(SysUser::getUserName, email))
            .ne(SysUser::getUserId, userId)
            .exists();
        if (occupied) throw new ServiceException("Email address is already in use");
    }

    private void consumeCode(String purpose, Long userId, String email, String supplied) {
        if (StringUtils.isBlank(supplied)) throw new ServiceException("Verification code cannot be empty");
        String key = codeKey(purpose, userId, email);
        String expected = RedisUtils.getCacheObject(key);
        RedisUtils.deleteObject(key);
        if (expected == null) throw new ServiceException("Verification code has expired");
        if (!StringUtils.equalsIgnoreCase(expected, supplied)) throw new ServiceException("Verification code is incorrect");
    }

    private String codeKey(String purpose, Long userId, String email) {
        return CODE_KEY + purpose + ":" + userId + ":" + email;
    }

    private String normalizePurpose(String value) {
        String purpose = StringUtils.trim(value);
        if (StringUtils.isBlank(purpose) || !CODE_PURPOSES.contains(purpose)) throw new ServiceException("Unsupported verification purpose");
        return purpose;
    }

    private String normalizeEmail(String value) {
        String email = StringUtils.trim(value);
        if (StringUtils.isBlank(email) || email.length() > 50 || !RegexValidator.isEmail(email)) throw new ServiceException("Invalid email address");
        return email.toLowerCase(Locale.ROOT);
    }

    private String maskEmail(String email) {
        if (StringUtils.isBlank(email) || !email.contains("@")) return email;
        int at = email.indexOf('@');
        String name = email.substring(0, at);
        return name.substring(0, Math.min(2, name.length())) + "***" + email.substring(at);
    }

    private void syncContactEmail(Long userId, String email) {
        CustomerContactInfo info = contactInfoMapper.selectById(userId);
        LocalDateTime now = LocalDateTime.now();
        if (info == null) {
            info = new CustomerContactInfo();
            info.setCustomerUserId(userId);
            info.setContactEmail(email);
            info.setCreatedAt(now);
            info.setUpdatedAt(now);
            contactInfoMapper.insert(info);
        } else {
            info.setContactEmail(email);
            info.setUpdatedAt(now);
            contactInfoMapper.updateById(info);
        }
    }

    private CustomerContactInfo normalizeContactInfo(Long userId, UpdateCustomerProfileBo.ContactInfo req) {
        CustomerContactInfo info = new CustomerContactInfo();
        info.setCustomerUserId(userId);
        info.setMailingAddress(normalizeText(req.getMailingAddress(), 255, "Mailing address"));
        info.setCity(normalizeText(req.getCity(), 128, "City"));
        info.setPostalCode(normalizeText(req.getPostalCode(), 32, "Postal or Zip"));
        info.setRegion(normalizeText(req.getRegion(), 128, "Province, State or Region"));
        info.setCountry(normalizeText(req.getCountry(), 128, "Country"));
        String email = normalizeText(req.getContactEmail(), 128, "Email");
        if (StringUtils.isNotBlank(email) && !CONTACT_EMAIL_PATTERN.matcher(email).matches()) throw new ServiceException("Invalid contact email format");
        info.setContactEmail(email);
        info.setPhone(normalizeText(req.getPhone(), 32, "Phone"));
        return info;
    }

    private ContactInfoVo toContactInfoVo(CustomerContactInfo info) {
        ContactInfoVo vo = new ContactInfoVo();
        if (info == null) return vo;
        vo.setMailingAddress(info.getMailingAddress());
        vo.setCity(info.getCity());
        vo.setPostalCode(info.getPostalCode());
        vo.setRegion(info.getRegion());
        vo.setCountry(info.getCountry());
        vo.setContactEmail(info.getContactEmail());
        vo.setPhone(info.getPhone());
        return vo;
    }

    private String normalizeText(String value, int max, String field) {
        String text = StringUtils.trim(value);
        if (StringUtils.isBlank(text)) return null;
        if (text.length() > max) throw new ServiceException(field + " length cannot exceed " + max);
        return text;
    }
}
