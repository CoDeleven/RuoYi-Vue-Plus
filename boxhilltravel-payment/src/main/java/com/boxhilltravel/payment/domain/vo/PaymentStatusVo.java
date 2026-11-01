package com.boxhilltravel.payment.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Customer payment status response.
 */
@Data
public class PaymentStatusVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long orderId;

    private Integer orderStatus;

    private String paymentStatus;

    private BigDecimal paidAmount;

    private BigDecimal refundedAmount;

    private String currency;

    private LocalDateTime paymentDeadline;

}
