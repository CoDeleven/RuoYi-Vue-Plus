package com.boxhilltravel.customer.domain.vo;

import lombok.Data;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Customer order list item.
 */
@Data
public class OrderListItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String orderNo;

    private Long tourId;

    private String tourName;

    @Translation(type = TransConstant.OSS_ID_TO_URL)
    private String tourCoverImage;

    private LocalDate departureDate;

    private Integer travelerCount;

    private String currency;

    private BigDecimal totalAmount;

    private Integer status;

    private LocalDateTime createTime;

}
