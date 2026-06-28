package com.boxhilltravel.core.domain;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 精选线路对象 holidays_featured_tour
 *
 * @author Lion Li
 * @date 2026-06-29 00:06:41
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("holidays_featured_tour")
public class HolidaysFeaturedTour extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 线路ID，引用 holidays_tour.id
     */
    private Long tourId;

    /**
     * 展示顺序，数值越小越靠前
     */
    private Integer sortOrder;


}


