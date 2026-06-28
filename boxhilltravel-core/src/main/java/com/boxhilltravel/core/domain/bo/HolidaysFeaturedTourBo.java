package com.boxhilltravel.core.domain.bo;

import com.boxhilltravel.core.domain.HolidaysFeaturedTour;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 精选线路业务对象 holidays_featured_tour
 *
 * @author Lion Li
 * @date 2026-06-29 00:06:41
 */
@Data
@AutoMapper(target = HolidaysFeaturedTour.class, reverseConvertGenerate = false)
public class HolidaysFeaturedTourBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 线路ID，引用 holidays_tour.id
     */
    @NotNull(message = "线路ID，引用 holidays_tour.id不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long tourId;

    /**
     * 展示顺序，数值越小越靠前
     */
    @NotNull(message = "展示顺序，数值越小越靠前不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer sortOrder;


}

