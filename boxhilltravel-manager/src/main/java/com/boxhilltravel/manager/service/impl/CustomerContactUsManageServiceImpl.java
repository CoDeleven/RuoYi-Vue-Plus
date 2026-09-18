package com.boxhilltravel.manager.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boxhilltravel.core.domain.CustomerContactUs;
import com.boxhilltravel.core.domain.bo.CustomerContactUsBo;
import com.boxhilltravel.core.domain.vo.CustomerContactUsVo;
import com.boxhilltravel.core.mapper.CustomerContactUsMapper;
import com.boxhilltravel.manager.service.ICustomerContactUsManageService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Customer contact us manage service implementation.
 */
@RequiredArgsConstructor
@Service
public class CustomerContactUsManageServiceImpl implements ICustomerContactUsManageService {

    private static final int READ_STATUS_UNREAD = 0;
    private static final int READ_STATUS_READ = 1;

    private final CustomerContactUsMapper contactUsMapper;

    @Override
    public CustomerContactUsVo queryById(Long id) {
        if (id == null) {
            throw new ServiceException("Contact us record id is required");
        }
        CustomerContactUsVo vo = contactUsMapper.selectVoById(id);
        if (vo == null) {
            throw new ServiceException("Contact us record not found");
        }
        return vo;
    }

    @Override
    public PageResult<CustomerContactUsVo> queryPageList(CustomerContactUsBo bo, PageQuery pageQuery) {
        Page<CustomerContactUsVo> result = contactUsMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return PageResult.build(result.getRecords(), result.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateReadStatus(Long id, Integer readStatus) {
        if (id == null) {
            throw new ServiceException("Contact us record id is required");
        }
        int normalized = normalizeReadStatus(readStatus);
        CustomerContactUs existing = contactUsMapper.selectById(id);
        if (existing == null) {
            throw new ServiceException("Contact us record not found");
        }
        existing.setReadStatus(normalized);
        existing.setReadTime(normalized == READ_STATUS_READ ? LocalDateTime.now() : null);
        existing.setUpdateTime(LocalDateTime.now());
        return contactUsMapper.updateById(existing) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return contactUsMapper.deleteByIds(ids) > 0;
    }

    private Wrapper<CustomerContactUs> buildQueryWrapper(CustomerContactUsBo bo) {
        CustomerContactUsBo query = bo == null ? new CustomerContactUsBo() : bo;
        return QueryBuilder.lambda(CustomerContactUs.class)
            .likeIfText(CustomerContactUs::getFullName, query.getFullName())
            .likeIfText(CustomerContactUs::getEmailAddress, query.getEmailAddress())
            .likeIfText(CustomerContactUs::getPhoneNumber, query.getPhoneNumber())
            .likeIfText(CustomerContactUs::getEnquiryType, query.getEnquiryType())
            .likeIfText(CustomerContactUs::getMessage, query.getMessage())
            .likeIfText(CustomerContactUs::getIpAddress, query.getIpAddress())
            .eqIfPresent(CustomerContactUs::getReadStatus, query.getReadStatus())
            .betweenParams(CustomerContactUs::getCreateTime, query.getParams(), "beginTime", "endTime")
            .orderByDesc(CustomerContactUs::getCreateTime)
            .orderByDesc(CustomerContactUs::getId)
            .build();
    }

    private int normalizeReadStatus(Integer readStatus) {
        if (readStatus == null) {
            return READ_STATUS_READ;
        }
        if (!Integer.valueOf(READ_STATUS_UNREAD).equals(readStatus) && !Integer.valueOf(READ_STATUS_READ).equals(readStatus)) {
            throw new ServiceException("Invalid contact us read status");
        }
        return readStatus;
    }
}
