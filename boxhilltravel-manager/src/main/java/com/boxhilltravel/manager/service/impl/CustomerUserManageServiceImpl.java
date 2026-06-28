package com.boxhilltravel.manager.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boxhilltravel.core.domain.CustomerContactInfo;
import com.boxhilltravel.core.domain.CustomerProfile;
import com.boxhilltravel.core.domain.vo.CustomerContactInfoVo;
import com.boxhilltravel.core.domain.vo.CustomerProfileVo;
import com.boxhilltravel.core.mapper.CustomerContactInfoMapper;
import com.boxhilltravel.core.mapper.CustomerProfileMapper;
import com.boxhilltravel.manager.domain.bo.CustomerUserBo;
import com.boxhilltravel.manager.domain.vo.CustomerUserDetailVo;
import com.boxhilltravel.manager.domain.vo.CustomerUserVo;
import com.boxhilltravel.manager.service.ICustomerUserManageService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.enums.UserType;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Customer user manager service implementation.
 */
@RequiredArgsConstructor
@Service
public class CustomerUserManageServiceImpl implements ICustomerUserManageService {

    private final SysUserMapper userMapper;
    private final CustomerProfileMapper profileMapper;
    private final CustomerContactInfoMapper contactInfoMapper;

    @Override
    public PageResult<CustomerUserVo> queryPageList(CustomerUserBo bo, PageQuery pageQuery) {
        Page<SysUserVo> page = userMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        List<CustomerUserVo> records = page.getRecords().stream().map(this::toListVo).toList();
        fillCustomerExtensions(records);
        return PageResult.build(records, page.getTotal());
    }

    @Override
    public CustomerUserDetailVo queryById(Long userId) {
        SysUserVo user = loadCustomerUser(userId);
        CustomerUserDetailVo vo = toDetailVo(user);
        CustomerProfileVo profile = profileMapper.selectVoById(userId);
        CustomerContactInfoVo contactInfo = contactInfoMapper.selectVoById(userId);
        vo.setProfile(profile == null ? new CustomerProfileVo() : profile);
        vo.setContactInfo(contactInfo == null ? new CustomerContactInfoVo() : contactInfo);
        if (profile != null) {
            vo.setProfileNickname(profile.getNickname());
            vo.setAvatarUrl(profile.getAvatarUrl());
        }
        if (contactInfo != null) {
            vo.setContactEmail(contactInfo.getContactEmail());
            vo.setContactPhone(contactInfo.getPhone());
        }
        return vo;
    }

    @Override
    public Boolean updateStatus(Long userId, String status) {
        validateStatus(status);
        loadCustomerUser(userId);
        int updated = userMapper.lambda()
            .set(SysUser::getStatus, status)
            .eq(SysUser::getUserId, userId)
            .eq(SysUser::getUserType, UserType.APP_USER.getUserType())
            .updateCount();
        if (updated == 0) {
            throw new ServiceException("Customer user status update failed");
        }
        return true;
    }

    private Wrapper<SysUser> buildQueryWrapper(CustomerUserBo bo) {
        CustomerUserBo query = bo == null ? new CustomerUserBo() : bo;
        return QueryBuilder.lambda(SysUser.class)
            .eq(SysUser::getDelFlag, SystemConstants.NORMAL)
            .eq(SysUser::getUserType, UserType.APP_USER.getUserType())
            .likeIfText(SysUser::getUserName, query.getUserName())
            .likeIfText(SysUser::getNickName, query.getNickName())
            .likeIfText(SysUser::getEmail, query.getEmail())
            .likeIfText(SysUser::getPhoneNumber, query.getPhoneNumber())
            .eqIfText(SysUser::getStatus, query.getStatus())
            .betweenParams(SysUser::getCreateTime, query.getParams(), "beginTime", "endTime")
            .orderByDesc(SysUser::getCreateTime)
            .orderByDesc(SysUser::getUserId)
            .build();
    }

    private SysUserVo loadCustomerUser(Long userId) {
        if (userId == null) {
            throw new ServiceException("Customer user id is required");
        }
        SysUserVo user = userMapper.lambda()
            .eq(SysUser::getUserId, userId)
            .eq(SysUser::getUserType, UserType.APP_USER.getUserType())
            .voOne();
        if (user == null) {
            throw new ServiceException("Customer user not found");
        }
        return user;
    }

    private void fillCustomerExtensions(List<CustomerUserVo> records) {
        if (records.isEmpty()) {
            return;
        }
        List<Long> userIds = records.stream().map(CustomerUserVo::getUserId).toList();
        Map<Long, CustomerProfile> profileMap = profileMapper.lambda()
            .in(CustomerProfile::getCustomerUserId, userIds)
            .list()
            .stream()
            .collect(java.util.stream.Collectors.toMap(CustomerProfile::getCustomerUserId, p -> p));
        Map<Long, CustomerContactInfo> contactInfoMap = contactInfoMapper.lambda()
            .in(CustomerContactInfo::getCustomerUserId, userIds)
            .list()
            .stream()
            .collect(java.util.stream.Collectors.toMap(CustomerContactInfo::getCustomerUserId, c -> c));
        records.forEach(record -> {
            CustomerProfile profile = profileMap.get(record.getUserId());
            if (profile != null) {
                record.setProfileNickname(profile.getNickname());
                record.setAvatarUrl(profile.getAvatarUrl());
            }
            CustomerContactInfo contactInfo = contactInfoMap.get(record.getUserId());
            if (contactInfo != null) {
                record.setContactEmail(contactInfo.getContactEmail());
                record.setContactPhone(contactInfo.getPhone());
            }
        });
    }

    private CustomerUserVo toListVo(SysUserVo user) {
        CustomerUserVo vo = new CustomerUserVo();
        vo.setUserId(user.getUserId());
        vo.setUserName(user.getUserName());
        vo.setNickName(user.getNickName());
        vo.setUserType(user.getUserType());
        vo.setEmail(user.getEmail());
        vo.setPhoneNumber(user.getPhoneNumber());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setLoginIp(user.getLoginIp());
        vo.setLoginDate(user.getLoginDate());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        return vo;
    }

    private CustomerUserDetailVo toDetailVo(SysUserVo user) {
        CustomerUserDetailVo vo = new CustomerUserDetailVo();
        vo.setUserId(user.getUserId());
        vo.setDeptId(user.getDeptId());
        vo.setUserName(user.getUserName());
        vo.setNickName(user.getNickName());
        vo.setUserType(user.getUserType());
        vo.setEmail(user.getEmail());
        vo.setPhoneNumber(user.getPhoneNumber());
        vo.setGender(user.getGender());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setLoginIp(user.getLoginIp());
        vo.setLoginDate(user.getLoginDate());
        vo.setRemark(user.getRemark());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        return vo;
    }

    private void validateStatus(String status) {
        if (!StringUtils.equalsAny(status, SystemConstants.NORMAL, SystemConstants.DISABLE)) {
            throw new ServiceException("Invalid customer user status");
        }
    }

}
