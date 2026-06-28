package com.boxhilltravel.core.domain.vo;

import com.boxhilltravel.core.domain.HolidaysFeaturedTour;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;



/**
 * 精选线路视图对象 holidays_featured_tour
 *
 * @author Lion Li
 * @date 2026-06-29 00:06:41
 */
@Data
@AutoMapper(target = HolidaysFeaturedTour.class)
public class HolidaysFeaturedTourVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 线路ID，引用 holidays_tour.id
     */
    private Long tourId;

    /**
     * 展示顺序，数值越小越靠前
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}


