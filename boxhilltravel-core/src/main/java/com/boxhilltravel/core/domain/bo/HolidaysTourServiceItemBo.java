package com.boxhilltravel.core.domain.bo;

import com.boxhilltravel.core.domain.HolidaysTourServiceItem;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 线路服务项业务对象 holidays_tour_service_item
 *
 * @author Lion Li
 * @date 2026-06-28 21:44:19
 */
@Data
@AutoMapper(target = HolidaysTourServiceItem.class, reverseConvertGenerate = false)
public class HolidaysTourServiceItemBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "{boxhilltravel.validation.id.required}", groups = { EditGroup.class })
    private Long id;

    /**
     * 线路ID
     */
    @NotNull(message = "{boxhilltravel.validation.tourId.required}", groups = { AddGroup.class, EditGroup.class })
    private Long tourId;

    /**
     * 项目类型 1=包含项 2=不包含项
     */
    @NotNull(message = "{boxhilltravel.validation.itemType.required}", groups = { AddGroup.class, EditGroup.class })
    private Long itemType;

    /**
     * 内容
     */
    @NotBlank(message = "{boxhilltravel.validation.content.required}", groups = { AddGroup.class, EditGroup.class })
    private String content;

    /**
     * 排序
     */
    @NotNull(message = "{boxhilltravel.validation.sort.required}", groups = { AddGroup.class, EditGroup.class })
    private Integer sortOrder;


}

