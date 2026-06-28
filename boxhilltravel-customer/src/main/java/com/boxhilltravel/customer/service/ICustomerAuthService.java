package com.boxhilltravel.customer.service;

import com.boxhilltravel.customer.domain.bo.CustomerSocialLoginBo;
import com.boxhilltravel.customer.domain.vo.CustomerLoginVo;

public interface ICustomerAuthService {

    String socialBindingUrl(String source);

    CustomerLoginVo socialLogin(CustomerSocialLoginBo bo);

}
