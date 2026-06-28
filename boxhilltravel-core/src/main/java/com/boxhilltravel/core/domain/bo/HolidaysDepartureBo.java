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
    @NotNull(message = "不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 线路ID
     */
    @NotNull(message = "线路ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long tourId;

    /**
     * 出发日期
     */
    @NotNull(message = "出发日期不能为空", groups = { AddGroup.class, EditGroup.class })
    private LocalDateTime departureDate;

    /**
     * 返程日期
     */
    @NotNull(message = "返程日期不能为空", groups = { AddGroup.class, EditGroup.class })
    private LocalDateTime returnDate;

    /**
     * 行程天数
     */
    @NotNull(message = "行程天数不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer durationDays;

    /**
     * 团期类型 1固定 2不固定
     */
    @NotNull(message = "团期类型 1固定 2不固定不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long departureType;

    /**
     * 核载人数
     */
    @NotNull(message = "核载人数不能为空", groups = { AddGroup.class, EditGroup.class })
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
    @NotNull(message = "原价不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal basePrice;

    /**
     * 售价
     */
    @NotNull(message = "售价不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal salePrice;

    /**
     * 折扣率
     */
    private BigDecimal discountRate;

    /**
     * 状态 1可预订 2已满 3已结束 4已取消
     */
    @NotNull(message = "状态 1可预订 2已满 3已结束 4已取消不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long status;

    /**
     * 备注
     */
    private String remark;


}

