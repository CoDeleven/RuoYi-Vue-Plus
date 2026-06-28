package com.boxhilltravel.customer.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Departure item.
 */
@Data
public class DepartureItemVo {

    private Long id;

    private String startDate;

    private String endDate;

    private Integer duration;

    private BigDecimal basePrice;

    private BigDecimal salePrice;

    private Integer availableCount;

    private Integer status;

}
