package com.boxhilltravel.core.domain.vo;

import com.boxhilltravel.core.domain.HolidaysOrderTraveler;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Order traveller view object holidays_order_traveler.
 */
@Data
@AutoMapper(target = HolidaysOrderTraveler.class)
public class HolidaysOrderTravelerVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
