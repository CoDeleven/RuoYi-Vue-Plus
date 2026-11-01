package com.boxhilltravel.payment.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Refund response.
 */
@Data
public class RefundVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long orderId;

    private String stripeRefundId;

    private BigDecimal amount;

    private String currency;

    private String status;

    private String reason;

    private LocalDateTime createTime;

}
