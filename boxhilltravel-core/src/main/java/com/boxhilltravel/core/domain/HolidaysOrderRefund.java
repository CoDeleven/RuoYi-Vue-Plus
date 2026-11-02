package com.boxhilltravel.core.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * Stripe refund record.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("holidays_order_refund")
public class HolidaysOrderRefund extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long paymentId;

    private String stripeRefundId;

    private BigDecimal amount;

    private Long amountMinor;

    private String currency;

    private String status;

    private String reason;

}
