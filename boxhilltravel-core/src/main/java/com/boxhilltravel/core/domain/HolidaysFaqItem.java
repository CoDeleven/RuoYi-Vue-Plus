package com.boxhilltravel.core.domain;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * FAQ条目对象 holidays_faq_item
 *
 * @author Lion Li
 * @date 2026-06-29 15:46:06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("holidays_faq_item")
public class HolidaysFaqItem extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分组ID
     */
    private Long groupId;

    /**
     * Module
     */
    private Integer module;

    /**
     * 问题
     */
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


