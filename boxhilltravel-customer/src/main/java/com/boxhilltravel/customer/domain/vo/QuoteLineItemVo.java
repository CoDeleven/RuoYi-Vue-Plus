package com.boxhilltravel.customer.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Checkout quote line item.
 */
@Data
public class QuoteLineItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String type;

    private String title;

    private Integer quantity;

    private BigDecimal amount;

}
