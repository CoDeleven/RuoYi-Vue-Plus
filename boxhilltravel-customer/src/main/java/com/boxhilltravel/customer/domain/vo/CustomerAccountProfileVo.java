package com.boxhilltravel.customer.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Current customer account profile.
 */
@Data
public class CustomerAccountProfileVo {

    private Long id;

    private String username;

    private String email;

    private String nickname;

    private Long avatar;

    private String avatarUrl;

    private String status;

    private Boolean emailVerified;

    private LocalDateTime createdAt;

    private ContactInfoVo contactInfo;

}
