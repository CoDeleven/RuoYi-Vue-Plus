package com.boxhilltravel.core.domain;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;
import java.util.List;

/**
 * 线路管理对象 holidays_tour
 *
 * @author CoDeleven
 * @date 2026-06-26 18:26:30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("holidays_tour")
public class HolidaysTour extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 线路ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 线路代码(唯一)
     */
    private String code;

    /**
     * 线路名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 行程天数
     */
    private Integer durationDays;

    /**
     * 旅行风格
     */
    private Long travelStyle;

    /**
     * 服务等级
     */
    private Long serviceLevel;

    /**
     * 线路强度 1-5
     */
    private Long physicalRating;

    /**
     * 线路类型
     */
    private Long tripType;

    /**
     * CollectionTag
     */
    @TableField(exist = false)
    private String collectionTag;

    /**
     * 国家名称关键字
     */
    @TableField(exist = false)
    private String countryName;

    /**
     * 途径国家
     */
    @TableField(exist = false)
    private List<String> countryNames;

    /**
     * 最小年龄
     */
    private Long minAge;

    /**
     * 基础价格
     */
    private BigDecimal basePrice;

    /**
     * 销售价格
     */
    private BigDecimal salePrice;

    /**
     * Single Supplement
     */
    private BigDecimal singleSupplement;

    /**
     * 货币
     */
    private String currency;

    /**
     * 封面图URL
     */
    private String coverImage;

    /**
     * 地图URL
     */
    private String mapImage;

    /**
     * ????
     */
    private String notes;

    /**
     * 最晚到达时间
     */
    private String latestArrivalTime;

    /**
     * 最早离开时间
     */
    private String earliestDepartureTime;

    /**
     * 状态 0草稿 1上架 2下架
     */
    private Long status;

    /**
     * 版本号(乐观锁)
     */
    @Version
    private Integer version;

    /**
     * SEO标题
     */
    private String seoTitle;

    /**
     * SEO描述
     */
    private String seoDescription;

    /**
     * SEO关键词
     */
    private String seoKeywords;

    /**
     * 删除时间
     */
    private LocalDateTime deletedAt;


}


