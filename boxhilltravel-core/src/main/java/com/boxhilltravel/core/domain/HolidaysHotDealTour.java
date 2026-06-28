package com.boxhilltravel.core.domain;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 热卖线路对象 holidays_hot_deal_tour
 *
 * @author Lion Li
 * @date 2026-06-28 23:38:47
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("holidays_hot_deal_tour")
public class HolidaysHotDealTour extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
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


