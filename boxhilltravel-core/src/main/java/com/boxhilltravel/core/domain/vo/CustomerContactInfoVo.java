package com.boxhilltravel.core.domain.vo;

import com.boxhilltravel.core.domain.CustomerContactInfo;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Customer contact extension view object.
 */
@Data
@AutoMapper(target = CustomerContactInfo.class)
public class CustomerContactInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long customerUserId;

    private String mailingAddress;

    private String city;

    private String postalCode;

    private String region;

    private String country;

    private String contactEmail;

    private String phone;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
