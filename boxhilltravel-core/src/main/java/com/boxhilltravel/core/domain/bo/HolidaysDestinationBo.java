package com.boxhilltravel.core.domain.bo;

import com.boxhilltravel.core.domain.HolidaysDestination;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 目的地分类业务对象 holidays_destination
 *
 * @author Lion Li
 * @date 2026-06-27 16:41:24
 */
@Data
@AutoMapper(target = HolidaysDestination.class, reverseConvertGenerate = false)
public class HolidaysDestinationBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 目的地ID
     */
    @NotNull(message = "目的地ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 目的地名称
     */
    @NotBlank(message = "目的地名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String name;

    /**
     * 目的地英文名称
     */
    private String nameEn;

    /**
     * 父级ID，0表示顶级（大洲）
     */
    private Long parentId;

    /**
     * 层级：1大洲 2国家 3城市
     */
    @NotNull(message = "层级：1大洲 2国家 3城市不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long level;

    /**
     * 封面图URL
     */
    private String image;

    /**
     *
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态 0禁用 1启用
     */
    @NotNull(message = "状态 0禁用 1启用不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long status;

    /**
     * 删除时间
     */
    private LocalDateTime deletedAt;

    /**
     * 查询参数
     */
    private Map<String, Object> params = new HashMap<>();

}

