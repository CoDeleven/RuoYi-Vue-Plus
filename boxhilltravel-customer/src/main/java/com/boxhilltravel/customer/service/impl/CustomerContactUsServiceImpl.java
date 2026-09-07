package com.boxhilltravel.customer.service.impl;

import com.boxhilltravel.core.domain.CustomerContactUs;
import com.boxhilltravel.core.domain.bo.CustomerContactUsBo;
import com.boxhilltravel.core.mapper.CustomerContactUsMapper;
import com.boxhilltravel.customer.service.ICustomerContactUsService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Customer contact us service implementation.
 */
@RequiredArgsConstructor
@Service
public class CustomerContactUsServiceImpl implements ICustomerContactUsService {

    private static final int READ_STATUS_UNREAD = 0;

    private final CustomerContactUsMapper contactUsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(CustomerContactUsBo bo, String ipAddress) {
        CustomerContactUs add = MapstructUtils.convert(bo, CustomerContactUs.class);
        if (add == null) {
            throw new ServiceException("Contact us request is required");
        }
        normalize(add);
        add.setIpAddress(StringUtils.trim(ipAddress));
        add.setReadStatus(READ_STATUS_UNREAD);
        LocalDateTime now = LocalDateTime.now();
        add.setCreateTime(now);
        add.setUpdateTime(now);
        return contactUsMapper.insert(add) > 0;
    }

    private void normalize(CustomerContactUs add) {
        add.setFullName(StringUtils.trim(add.getFullName()));
        add.setEmailAddress(StringUtils.trim(add.getEmailAddress()));
        add.setPhoneNumber(StringUtils.trim(add.getPhoneNumber()));
        add.setEnquiryType(StringUtils.trim(add.getEnquiryType()));
        add.setMessage(StringUtils.trim(add.getMessage()));
    }
}
