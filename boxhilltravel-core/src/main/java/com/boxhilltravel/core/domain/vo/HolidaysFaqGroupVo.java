package com.boxhilltravel.core.domain.vo;

import com.boxhilltravel.core.domain.HolidaysFaqGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;



/**
 * FAQ分组视图对象 holidays_faq_group
 *
 * @author Lion Li
 * @date 2026-06-29 15:50:16
 */
@Data
@AutoMapper(target = HolidaysFaqGroup.class)
public class HolidaysFaqGroupVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 分组名称
     */
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


