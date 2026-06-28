package com.boxhilltravel.core.domain.vo;

import com.boxhilltravel.core.domain.HolidaysTourItinerary;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.annotation.TranslationType;
import org.dromara.common.translation.config.TranslationConfig;
import org.dromara.common.translation.constant.TransConstant;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;



/**
 * 行程视图对象 holidays_tour_itinerary
 *
 * @author Lion Li
 * @date 2026-06-27 15:20:48
 */
@FieldNameConstants
@Data
@AutoMapper(target = HolidaysTourItinerary.class)
public class HolidaysTourItineraryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     *
     */
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

    private Long fromDestinationId;
    private Long toDestinationId;
    /**
     * 出发地
     */
    @Translation(type = TransConstant.DESTINATION_ID_TO_NAME, mapper = "fromDestinationId")
    private String fromDestination;

    /**
     * 目的地
     */
    @Translation(type = TransConstant.DESTINATION_ID_TO_NAME, mapper = "toDestinationId")
    private String toDestination;

    /**
     * 餐食 B/L/D
     */
    private String meals;
}


