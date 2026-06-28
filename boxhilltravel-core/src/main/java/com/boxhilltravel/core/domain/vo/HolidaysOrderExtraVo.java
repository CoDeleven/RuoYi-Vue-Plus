package com.boxhilltravel.core.domain.vo;

import com.boxhilltravel.core.domain.HolidaysOrderExtra;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Order extra view object holidays_order_extra.
 */
@Data
@AutoMapper(target = HolidaysOrderExtra.class)
public class HolidaysOrderExtraVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long orderId;

    private String extraType;

    private Long refId;

    private String title;

    private String description;

    private BigDecimal amount;

    private Integer quantity;

    private String metadata;

}
