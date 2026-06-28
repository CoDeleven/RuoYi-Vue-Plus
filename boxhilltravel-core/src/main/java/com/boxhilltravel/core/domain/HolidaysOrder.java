package com.boxhilltravel.core.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Customer order object holidays_order.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("holidays_order")
public class HolidaysOrder extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
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

}
