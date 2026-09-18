package com.boxhilltravel.core.domain.bo;

import com.boxhilltravel.core.domain.HolidaysDeparture;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 团期业务对象 holidays_departure
 *
 * @author Lion Li
 * @date 2026-06-30 14:47:38
 */
@Data
@AutoMapper(target = HolidaysDeparture.class, reverseConvertGenerate = false)
public class HolidaysDepartureBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @NotNull(message = "{boxhilltravel.validation.primaryKey.required}", groups = { EditGroup.class })
    private Long id;

    /**
     * 线路ID
     */
    @NotNull(message = "{boxhilltravel.validation.tourId.required}", groups = { AddGroup.class, EditGroup.class })
    private Long tourId;

    /**
     * 出发日期
     */
    @NotNull(message = "{boxhilltravel.validation.departureDate.required}", groups = { AddGroup.class, EditGroup.class })
    private LocalDateTime departureDate;

    /**
     * 返程日期
     */
    @NotNull(message = "{boxhilltravel.validation.returnDate.required}", groups = { AddGroup.class, EditGroup.class })
    private LocalDateTime returnDate;

    /**
     * 行程天数
     */
    @NotNull(message = "{boxhilltravel.validation.durationDays.required}", groups = { AddGroup.class, EditGroup.class })
    private Integer durationDays;

    /**
     * 团期类型 1固定 2不固定
     */
    @NotNull(message = "{boxhilltravel.validation.departureType.required}", groups = { AddGroup.class, EditGroup.class })
    private Long departureType;

    /**
     * 核载人数
     */
    @NotNull(message = "{boxhilltravel.validation.capacity.required}", groups = { AddGroup.class, EditGroup.class })
    private Integer maxCapacity;

    /**
     * 最小成团人数
     */
    private Integer minCapacity;

    /**
     * 已预订人数
     */
    private Integer bookedCount;

    /**
     * 剩余名额
     */
    private Integer availableCount;

    /**
     * 原价
     */
    @NotNull(message = "{boxhilltravel.validation.originalPrice.required}", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal basePrice;

    /**
     * 售价
     */
    @NotNull(message = "{boxhilltravel.validation.salePrice.required}", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal salePrice;

    /**
     * 折扣率
     */
    private BigDecimal discountRate;

    /**
     * 状态 1可预订 2已满 3已结束 4已取消
     */
    @NotNull(message = "{boxhilltravel.validation.status.required}", groups = { AddGroup.class, EditGroup.class })
    private Long status;

    /**
     * 备注
     */
    private String remark;


}

