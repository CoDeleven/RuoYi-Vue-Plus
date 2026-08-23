package com.boxhilltravel.customer.domain.vo;

import lombok.Data;

@Data
public class CustomerSocialBindingVo {

    private String source;

    private Boolean bound;

    private String displayName;

    private String avatar;
}
