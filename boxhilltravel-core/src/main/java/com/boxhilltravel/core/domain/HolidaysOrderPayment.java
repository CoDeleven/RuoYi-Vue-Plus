package com.boxhilltravel.core.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order payment object holidays_order_payment.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("holidays_order_payment")
public class HolidaysOrderPayment extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String paymentNo;

    private String paymentMethod;

    private BigDecimal amount;

    private String currency;

    private String status;

    private String stripeCheckoutSessionId;

    private String checkoutUrl;

    private String stripePaymentIntentId;

    private Long amountMinor;

    private LocalDateTime checkoutExpiresAt;

    private String failureCode;

    private LocalDateTime paidTime;

}
