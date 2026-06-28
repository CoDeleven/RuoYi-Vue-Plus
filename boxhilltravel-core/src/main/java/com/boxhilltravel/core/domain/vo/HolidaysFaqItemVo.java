package com.boxhilltravel.core.domain.vo;

import com.boxhilltravel.core.domain.HolidaysFaqItem;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;



/**
 * FAQ条目视图对象 holidays_faq_item
 *
 * @author Lion Li
 * @date 2026-06-29 15:46:06
 */
@Data
@AutoMapper(target = HolidaysFaqItem.class)
public class HolidaysFaqItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 分组ID
     */
    private Long groupId;

    /**
     * 分组名称
     */
    private String groupName;

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


