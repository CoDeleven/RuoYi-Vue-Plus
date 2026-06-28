package com.boxhilltravel.core.domain;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 行程活动对象 holidays_tour_itinerary_activity
 *
 * @author Lion Li
 * @date 2026-06-28 12:58:35
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("holidays_tour_itinerary_activity")
public class HolidaysTourItineraryActivity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 行程ID
     */
    private Long itineraryId;

    /**
     * 线路ID
     */
    private Long tourId;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 活动图标
     */
    private String activityIcon;

    /**
     * 副标签列表, JSON列表
     */
    private String subtitle;

    /**
     *
     */
    private Integer sortOrder;

    /**
     * 是否在预览时展示：0不展示，1展示
     */
    private Long showInPreview;


}


