package com.boxhilltravel.core.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * Tour destination relation object holidays_tour_destination.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("holidays_tour_destination")
public class HolidaysTourDestination extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long tourId;

    private Long destinationId;

    private Integer sequence;

}
