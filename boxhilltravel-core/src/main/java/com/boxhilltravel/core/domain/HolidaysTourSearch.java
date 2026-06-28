package com.boxhilltravel.core.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * H5 tour search read model.
 */
@Data
@TableName("holidays_tour_search")
public class HolidaysTourSearch implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "tour_id", type = IdType.INPUT)
    private Long tourId;

    private String code;

    private String name;

    private String searchText;

    private Long status;

    private LocalDateTime updatedAt;

}
