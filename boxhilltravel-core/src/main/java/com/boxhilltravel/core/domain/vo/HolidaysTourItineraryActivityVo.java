package com.boxhilltravel.core.domain.vo;

import com.boxhilltravel.core.domain.HolidaysTourItineraryActivity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;



/**
 * 行程活动视图对象 holidays_tour_itinerary_activity
 *
 * @author Lion Li
 * @date 2026-06-28 12:58:35
 */
@Data
@AutoMapper(target = HolidaysTourItineraryActivity.class)
public class HolidaysTourItineraryActivityVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     *
     */

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


