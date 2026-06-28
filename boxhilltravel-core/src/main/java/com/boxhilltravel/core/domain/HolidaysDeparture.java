package com.boxhilltravel.core.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Tour departure object holidays_departure.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("holidays_departure")
public class HolidaysDeparture extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long tourId;

    private LocalDate departureDate;

    private LocalDate returnDate;

    private Long durationDays;

    private Integer departureType;

    private Long maxCapacity;

    private Long minCapacity;

    private Long bookedCount;

    private Long availableCount;

    private BigDecimal basePrice;

    private BigDecimal salePrice;

    private BigDecimal discountRate;

    private Integer status;

}
