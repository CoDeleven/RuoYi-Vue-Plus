package com.boxhilltravel.customer.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Checkout price quote.
 */
@Data
public class CheckoutQuoteVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String currency;

    private BigDecimal unitPrice;

    private BigDecimal tourAmount;

    private BigDecimal extrasAmount;

    private BigDecimal totalAmount;

    private Boolean taxesIncluded;

    private List<QuoteLineItemVo> lineItems;

}
