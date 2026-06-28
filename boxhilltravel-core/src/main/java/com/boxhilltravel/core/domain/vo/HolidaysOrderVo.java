package com.boxhilltravel.core.domain.vo;

import com.boxhilltravel.core.domain.HolidaysOrder;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order view object holidays_order.
 */
@Data
@AutoMapper(target = HolidaysOrder.class)
public class HolidaysOrderVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String orderNo;

    private Long customerUserId;

    private String customerUsername;

    private String customerEmail;

    private Long tourId;

    private String tourCode;

    private String tourName;

    private String tourCoverImage;

    private Long departureId;

    private LocalDate departureDate;

    private LocalDate returnDate;

    private Integer travelerCount;

    private String currency;

    private BigDecimal unitPrice;

    private BigDecimal tourAmount;

    private BigDecimal extrasAmount;

    private BigDecimal totalAmount;

    private BigDecimal paidAmount;

    private Integer status;

    private String contactName;

    private String contactEmail;

    private String contactPhone;

    private String contactAddress;

    private String contactCity;

    private String contactRegion;

    private String contactPostalCode;

    private String contactCountry;

    private Boolean termsAccepted;

    private String remark;

    private LocalDateTime createTime;

    private List<HolidaysOrderTravelerVo> travelers;

    private List<HolidaysOrderExtraVo> extras;

    private HolidaysOrderPaymentVo payment;

}
