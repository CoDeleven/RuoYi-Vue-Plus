package com.boxhilltravel.customer.domain.vo;

import lombok.Data;

/**
 * Customer contact info.
 */
@Data
public class ContactInfoVo {

    private String mailingAddress;

    private String city;

    private String postalCode;

    private String region;

    private String country;

    private String contactEmail;

    private String phone;

}
