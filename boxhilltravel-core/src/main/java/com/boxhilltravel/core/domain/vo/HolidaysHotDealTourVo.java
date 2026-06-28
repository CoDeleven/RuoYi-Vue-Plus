package com.boxhilltravel.core.domain.vo;

import com.boxhilltravel.core.domain.HolidaysHotDealTour;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;



/**
 * 热卖线路视图对象 holidays_hot_deal_tour
 *
 * @author Lion Li
 * @date 2026-06-28 23:38:47
 */
@Data
@AutoMapper(target = HolidaysHotDealTour.class)
public class HolidaysHotDealTourVo implements Serializable {

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


