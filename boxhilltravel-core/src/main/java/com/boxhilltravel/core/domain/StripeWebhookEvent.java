package com.boxhilltravel.core.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * Stripe webhook event inbox record.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stripe_webhook_event")
public class StripeWebhookEvent extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String eventId;

    private String eventType;

    private String objectId;

    private String payload;

    private String processStatus;

    private String errorMessage;

    private java.time.LocalDateTime receivedAt;

    private java.time.LocalDateTime processedAt;

}
