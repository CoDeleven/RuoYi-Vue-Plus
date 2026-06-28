package com.boxhilltravel.core.domain.bo;

import com.boxhilltravel.core.domain.HolidaysFaqItem;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * FAQ条目业务对象 holidays_faq_item
 *
 * @author Lion Li
 * @date 2026-06-29 15:46:06
 */
@Data
@AutoMapper(target = HolidaysFaqItem.class, reverseConvertGenerate = false)
public class HolidaysFaqItemBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 分组ID
     */
    @NotNull(message = "分组ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long groupId;

    /**
     * Module
     */
    private Integer module;

    /**
     * 问题
     */
    @NotBlank(message = "问题不能为空", groups = { AddGroup.class, EditGroup.class })
    private String question;

    /**
     * 答案(支持HTML)
     */
    private String answer;

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

