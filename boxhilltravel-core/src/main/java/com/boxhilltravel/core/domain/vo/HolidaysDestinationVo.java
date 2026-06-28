package com.boxhilltravel.core.domain.vo;

import java.time.LocalDateTime;

import com.boxhilltravel.core.domain.HolidaysDestination;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;



/**
 * 目的地分类视图对象 holidays_destination
 *
 * @author Lion Li
 * @date 2026-06-27 16:41:24
 */
@Data
@AutoMapper(target = HolidaysDestination.class)
public class HolidaysDestinationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 目的地ID
     */

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
     * 封面图URLUrl
     */
    @Translation(type = TransConstant.OSS_ID_TO_URL, mapper = "image")
    private String imageUrl;
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


