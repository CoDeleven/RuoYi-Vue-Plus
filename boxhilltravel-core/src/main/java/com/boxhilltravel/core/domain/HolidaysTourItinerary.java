package com.boxhilltravel.core.domain;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 行程对象 holidays_tour_itinerary
 *
 * @author Lion Li
 * @date 2026-06-27 15:20:48
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("holidays_tour_itinerary")
public class HolidaysTourItinerary extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 线路ID
     */
    private Long tourId;

    /**
     * 第几天
     */
    private Integer dayNumber;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 起始destinationId
     */
    private Integer fromDestinationId;

    /**
     * 结束destinationId
     */
    private Integer toDestinationId;

    /**
     * 餐食 B/L/D
     */
    private String meals;


}


