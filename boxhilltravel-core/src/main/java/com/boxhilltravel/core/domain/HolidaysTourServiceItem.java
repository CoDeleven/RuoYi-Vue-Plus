package com.boxhilltravel.core.domain;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 线路服务项对象 holidays_tour_service_item
 *
 * @author Lion Li
 * @date 2026-06-28 21:44:19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("holidays_tour_service_item")
public class HolidaysTourServiceItem extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 线路ID
     */
    private Long tourId;

    /**
     * 项目类型 1=包含项 2=不包含项
     */
    private Long itemType;

    /**
     * 内容
     */
    private String content;

    /**
     * 排序
     */
    private Integer sortOrder;


}


