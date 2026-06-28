package com.boxhilltravel.core.domain;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * FAQ分组对象 holidays_faq_group
 *
 * @author Lion Li
 * @date 2026-06-29 15:50:16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("holidays_faq_group")
public class HolidaysFaqGroup extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
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


