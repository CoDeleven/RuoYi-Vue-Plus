package com.boxhilltravel.customer.service;

import com.boxhilltravel.core.domain.bo.CustomerContactUsBo;

/**
 * Customer contact us service.
 */
public interface ICustomerContactUsService {

    /**
     * Submit a contact us record.
     *
     * @param bo contact us form
     * @param ipAddress client ip address
     * @return whether saved successfully
     */
    Boolean submit(CustomerContactUsBo bo, String ipAddress);
}
