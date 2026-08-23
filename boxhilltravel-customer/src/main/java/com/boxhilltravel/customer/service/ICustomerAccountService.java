package com.boxhilltravel.customer.service;

import com.boxhilltravel.customer.domain.bo.CustomerEmailCodeBo;
import com.boxhilltravel.customer.domain.bo.CustomerSocialBindingBo;
import com.boxhilltravel.customer.domain.bo.UpdateCustomerEmailBo;
import com.boxhilltravel.customer.domain.bo.UpdateCustomerPasswordBo;
import com.boxhilltravel.customer.domain.bo.UpdateCustomerProfileBo;
import com.boxhilltravel.customer.domain.vo.CustomerAccountProfileVo;
import com.boxhilltravel.customer.domain.vo.CustomerAccountSecurityVo;

public interface ICustomerAccountService {

    CustomerAccountProfileVo getProfile();

    CustomerAccountProfileVo updateProfile(UpdateCustomerProfileBo bo);

    CustomerAccountSecurityVo getSecurity();

    void sendEmailCode(CustomerEmailCodeBo bo);

    void updateEmail(UpdateCustomerEmailBo bo);

    void updatePassword(UpdateCustomerPasswordBo bo);

    String socialBindingUrl(String source);

    void bindSocial(CustomerSocialBindingBo bo);

    void unbindSocial(String source);
}
