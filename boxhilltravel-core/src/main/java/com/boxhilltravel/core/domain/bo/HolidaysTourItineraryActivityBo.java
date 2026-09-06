package com.boxhilltravel.core.domain.bo;

import com.boxhilltravel.core.domain.HolidaysTourItineraryActivity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 行程活动业务对象 holidays_tour_itinerary_activity
 *
 * @author Lion Li
 * @date 2026-06-28 12:58:35
 */
@Data
@AutoMapper(target = HolidaysTourItineraryActivity.class, reverseConvertGenerate = false)
public class HolidaysTourItineraryActivityBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @NotNull(message = "{boxhilltravel.validation.primaryKey.required}", groups = { EditGroup.class })
    private Long id;

    /**
     * 行程ID
     */
    @NotNull(message = "{boxhilltravel.validation.itineraryId.required}", groups = { AddGroup.class, EditGroup.class })
    private Long itineraryId;

    /**
     * 线路ID
     */
    @NotNull(message = "{boxhilltravel.validation.tourId.required}", groups = { AddGroup.class, EditGroup.class })
    private Long tourId;

    /**
     * 标题
     */
    @NotBlank(message = "{boxhilltravel.validation.title.required}", groups = { AddGroup.class, EditGroup.class })
    private String title;

    /**
     * 描述
     */
    @NotBlank(message = "{boxhilltravel.validation.description.required}", groups = { AddGroup.class, EditGroup.class })
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
    @NotNull(message = "{boxhilltravel.validation.primaryKey.required}", groups = { AddGroup.class, EditGroup.class })
    private Integer sortOrder;

    /**
     * 是否在预览时展示：0不展示，1展示
     */
    @NotNull(message = "{boxhilltravel.validation.preview.required}", groups = { AddGroup.class, EditGroup.class })
    private Long showInPreview;


}

