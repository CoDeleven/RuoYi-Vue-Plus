package com.boxhilltravel.core.domain;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;

/**
 * 目的地分类对象 holidays_destination
 *
 * @author Lion Li
 * @date 2026-06-27 16:41:24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("holidays_destination")
public class HolidaysDestination extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 目的地ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 目的地名称
     */
    private String name;

    /**
     * 目的地英文名称
     */
    private String nameEn;

    /**
     * 父级ID，0表示顶级（大洲）
     */
    private Long parentId;

    /**
     * 层级：1大洲 2国家 3城市
     */
    private Long level;

    /**
     * 封面图URL
     */
    private String image;

    /**
     *
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态 0禁用 1启用
     */
    private Long status;

    /**
     * 删除时间
     */
    private LocalDateTime deletedAt;


}


