package com.boxhilltravel.payment.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Checkout session returned to the customer client.
 */
@Data
public class PaymentCheckoutVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long orderId;

    private String orderNo;

    private String sessionId;

    private String checkoutUrl;

    private String currency;

    private BigDecimal amount;

    private String paymentStatus;

    private LocalDateTime expiresAt;

}
