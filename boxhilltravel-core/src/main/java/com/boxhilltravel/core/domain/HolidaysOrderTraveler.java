package com.boxhilltravel.core.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.time.LocalDate;

/**
 * Order traveller object holidays_order_traveler.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("holidays_order_traveler")
public class HolidaysOrderTraveler extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Integer travelerNo;

    private Boolean primaryTraveler;

    private String title;

    private String firstName;

    private String middleName;

    private String lastName;

    private Boolean noMiddleName;

    private LocalDate dateOfBirth;

    private String email;

    private String phone;

    private String placeOfBirth;

    private String nationality;

    private String passportNumber;

    private LocalDate passportExpiryDate;

    private String address;

    private String city;

    private String region;

    private String postalCode;

    private String country;

}
