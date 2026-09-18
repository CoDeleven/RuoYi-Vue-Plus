package com.boxhilltravel.core.domain.bo;

import com.boxhilltravel.core.domain.HolidaysDestinationTagRel;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;

/**
 * Destination tag relation business object holidays_destination_tag_rel.
 */
@Data
@AutoMapper(target = HolidaysDestinationTagRel.class, reverseConvertGenerate = false)
public class HolidaysDestinationTagRelBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "{boxhilltravel.validation.primaryKey.required}", groups = { EditGroup.class })
    private Long id;

    @NotNull(message = "{boxhilltravel.validation.destination.required}", groups = { AddGroup.class, EditGroup.class })
    private Long destinationId;

    @NotBlank(message = "{boxhilltravel.validation.tag.required}", groups = { AddGroup.class, EditGroup.class })
    private String dictValue;

    @NotNull(message = "{boxhilltravel.validation.sort.required}", groups = { AddGroup.class, EditGroup.class })
    private Integer sortOrder;

    /**
     * 目的地名称筛选
     */
    private String destinationName;
}
