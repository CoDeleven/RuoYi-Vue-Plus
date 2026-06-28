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
 * Order extra object holidays_order_extra.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("holidays_order_extra")
public class HolidaysOrderExtra extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String extraType;

    private Long refId;

    private String title;

    private String description;

    private BigDecimal amount;

    private Integer quantity;

    private String metadata;

}
