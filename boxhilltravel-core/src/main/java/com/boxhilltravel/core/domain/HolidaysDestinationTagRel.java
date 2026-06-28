package com.boxhilltravel.core.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * Destination tag relation object holidays_destination_tag_rel.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("holidays_destination_tag_rel")
public class HolidaysDestinationTagRel extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long destinationId;

    private String dictValue;

    private Integer sortOrder;

}
