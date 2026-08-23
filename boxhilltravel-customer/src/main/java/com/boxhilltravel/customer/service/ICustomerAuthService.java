package com.boxhilltravel.customer.service;

import com.boxhilltravel.customer.domain.bo.CustomerSocialBindingBo;
import com.boxhilltravel.customer.domain.bo.CustomerSocialLoginBo;
import com.boxhilltravel.customer.domain.vo.CustomerLoginVo;
import com.boxhilltravel.customer.domain.vo.CustomerSocialBindingVo;

import java.util.List;

public interface ICustomerAuthService {

    String socialBindingUrl(String source);

    CustomerLoginVo socialLogin(CustomerSocialLoginBo bo);

    String accountBindingUrl(String source, Long userId);

    void bindAccount(CustomerSocialBindingBo bo, Long userId);

    void unbindAccount(String source, Long userId);

    List<CustomerSocialBindingVo> listAccountBindings(Long userId);
}
