package com.boxhilltravel.core.domain.vo;

import com.boxhilltravel.core.domain.HolidaysOrderRefund;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order refund view object.
 */
@Data
@AutoMapper(target = HolidaysOrderRefund.class)
public class HolidaysOrderRefundVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long orderId;

    private Long paymentId;

    private String stripeRefundId;

    private BigDecimal amount;

    private Long amountMinor;

    private String currency;

    private String status;

    private String reason;

    private LocalDateTime createTime;

}
