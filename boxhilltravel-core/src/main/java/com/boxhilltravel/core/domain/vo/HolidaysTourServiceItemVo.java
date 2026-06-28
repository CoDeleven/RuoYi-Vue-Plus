package com.boxhilltravel.core.domain.vo;

import com.boxhilltravel.core.domain.HolidaysTourServiceItem;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;



/**
 * 线路服务项视图对象 holidays_tour_service_item
 *
 * @author Lion Li
 * @date 2026-06-28 21:44:19
 */
@Data
@AutoMapper(target = HolidaysTourServiceItem.class)
public class HolidaysTourServiceItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
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


