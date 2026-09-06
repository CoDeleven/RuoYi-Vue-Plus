package com.boxhilltravel.core.domain.bo;

import com.boxhilltravel.core.domain.HolidaysTourItinerary;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 行程业务对象 holidays_tour_itinerary
 *
 * @author Lion Li
 * @date 2026-06-27 15:20:48
 */
@Data
@AutoMapper(target = HolidaysTourItinerary.class, reverseConvertGenerate = false)
public class HolidaysTourItineraryBo implements Serializable {

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
     * 第几天
     */
    @NotNull(message = "{boxhilltravel.validation.dayNumber.required}", groups = { AddGroup.class, EditGroup.class })
    private Integer dayNumber;

    /**
     * 标题
     */
    @NotBlank(message = "{boxhilltravel.validation.title.required}", groups = { AddGroup.class, EditGroup.class })
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

