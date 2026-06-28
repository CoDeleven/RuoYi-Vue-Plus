package com.boxhilltravel.customer.service.impl;

import com.boxhilltravel.core.domain.CustomerContactInfo;
import com.boxhilltravel.core.domain.CustomerProfile;
import com.boxhilltravel.core.mapper.CustomerContactInfoMapper;
import com.boxhilltravel.core.mapper.CustomerProfileMapper;
import com.boxhilltravel.customer.domain.bo.UpdateCustomerProfileBo;
import com.boxhilltravel.customer.domain.vo.ContactInfoVo;
import com.boxhilltravel.customer.domain.vo.CustomerAccountProfileVo;
import com.boxhilltravel.customer.service.ICustomerAccountService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.service.ISysUserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Customer account service implementation.
 */
@RequiredArgsConstructor
@Service
public class CustomerAccountServiceImpl implements ICustomerAccountService {

    private static final Pattern CONTACT_EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final ISysUserService userService;
    private final CustomerProfileMapper profileMapper;
    private final CustomerContactInfoMapper contactInfoMapper;

    @Override
    public CustomerAccountProfileVo getProfile() {
        return buildProfileResp(LoginHelper.getUserId());
    }

    @Override
    public CustomerAccountProfileVo updateProfile(UpdateCustomerProfileBo bo) {
        Long userId = LoginHelper.getUserId();
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
        vo.setEmailVerified(true);
        vo.setCreatedAt(user.getCreateTime());
        vo.setContactInfo(toContactInfoVo(contactInfo));
        return vo;
    }

    private SysUserVo loadSysUser(Long userId) {
        if (userId == null) {
            throw new ServiceException("User not logged in");
        }
        SysUserVo user = userService.selectUserById(userId);
        if (user == null) {
            throw new ServiceException("User not found");
        }
        return user;
    }

    private CustomerContactInfo normalizeContactInfo(Long userId, UpdateCustomerProfileBo.ContactInfo req) {
        CustomerContactInfo contactInfo = new CustomerContactInfo();
        contactInfo.setCustomerUserId(userId);
        contactInfo.setMailingAddress(normalizeText(req.getMailingAddress(), 255, "Mailing address"));
        contactInfo.setCity(normalizeText(req.getCity(), 128, "City"));
        contactInfo.setPostalCode(normalizeText(req.getPostalCode(), 32, "Postal or Zip"));
        contactInfo.setRegion(normalizeText(req.getRegion(), 128, "Province, State or Region"));
        contactInfo.setCountry(normalizeText(req.getCountry(), 128, "Country"));
        String email = normalizeText(req.getContactEmail(), 128, "Email");
        if (StringUtils.isNotBlank(email) && !CONTACT_EMAIL_PATTERN.matcher(email).matches()) {
            throw new ServiceException("Invalid contact email format");
        }
        contactInfo.setContactEmail(email);
        contactInfo.setPhone(normalizeText(req.getPhone(), 32, "Phone"));
        return contactInfo;
    }

    private ContactInfoVo toContactInfoVo(CustomerContactInfo contactInfo) {
        ContactInfoVo vo = new ContactInfoVo();
        if (contactInfo == null) {
            return vo;
        }
        vo.setMailingAddress(contactInfo.getMailingAddress());
        vo.setCity(contactInfo.getCity());
        vo.setPostalCode(contactInfo.getPostalCode());
        vo.setRegion(contactInfo.getRegion());
        vo.setCountry(contactInfo.getCountry());
        vo.setContactEmail(contactInfo.getContactEmail());
        vo.setPhone(contactInfo.getPhone());
        return vo;
    }

    private String normalizeText(String value, int maxLength, String fieldName) {
        String trimmed = StringUtils.trim(value);
        if (StringUtils.isBlank(trimmed)) {
            return null;
        }
        if (trimmed.length() > maxLength) {
            throw new ServiceException(fieldName + " length cannot exceed " + maxLength);
        }
        return trimmed;
    }

}
