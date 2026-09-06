package com.boxhilltravel.core.domain.bo;

import com.boxhilltravel.core.domain.HolidaysFaqGroup;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * FAQ分组业务对象 holidays_faq_group
 *
 * @author Lion Li
 * @date 2026-06-29 15:50:16
 */
@Data
@AutoMapper(target = HolidaysFaqGroup.class, reverseConvertGenerate = false)
public class HolidaysFaqGroupBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @NotNull(message = "{boxhilltravel.validation.primaryKey.required}", groups = { EditGroup.class })
    private Long id;

    /**
     * 分组名称
     */
    @NotBlank(message = "{boxhilltravel.validation.groupName.required}", groups = { AddGroup.class, EditGroup.class })
    private String groupName;

    /**
     * Module
     */
    private Integer module;

    /**
     * 分组图标(emoji)
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态（1启用 0停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;


}

