package com.boxhilltravel.core.domain.vo;

import com.boxhilltravel.core.domain.HolidaysOrderPayment;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order payment view object holidays_order_payment.
 */
@Data
@AutoMapper(target = HolidaysOrderPayment.class)
public class HolidaysOrderPaymentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long orderId;

    private String paymentNo;

    private String paymentMethod;

    private BigDecimal amount;

    private String currency;

    private String status;

    private LocalDateTime paidTime;

}
