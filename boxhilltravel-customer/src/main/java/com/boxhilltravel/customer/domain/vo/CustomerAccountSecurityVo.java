package com.boxhilltravel.customer.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class CustomerAccountSecurityVo {

    private Boolean passwordConfigured;

    private String email;

    private String maskedEmail;

    private Boolean emailVerified;

    private List<CustomerSocialBindingVo> socialBindings;
}
