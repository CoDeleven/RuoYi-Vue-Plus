package com.boxhilltravel.customer.service;

import com.boxhilltravel.customer.domain.bo.UpdateCustomerProfileBo;
import com.boxhilltravel.customer.domain.vo.CustomerAccountProfileVo;

/**
 * Customer account service.
 */
public interface ICustomerAccountService {

    CustomerAccountProfileVo getProfile();

    CustomerAccountProfileVo updateProfile(UpdateCustomerProfileBo bo);

}
