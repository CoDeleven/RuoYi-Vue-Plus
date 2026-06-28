package com.boxhilltravel.core.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Customer contact extension for sys_user.
 */
@Data
@TableName("customer_contact_info")
public class CustomerContactInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "customer_user_id", type = IdType.INPUT)
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
