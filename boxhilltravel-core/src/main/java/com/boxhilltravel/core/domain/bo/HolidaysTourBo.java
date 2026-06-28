package com.boxhilltravel.core.domain.bo;

import com.boxhilltravel.core.domain.HolidaysTour;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 线路管理业务对象 holidays_tour
 *
 * @author CoDeleven
 * @date 2026-06-26 18:26:30
 */
@Data
@AutoMapper(target = HolidaysTour.class, reverseConvertGenerate = false)
public class HolidaysTourBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 线路ID
     */
    @NotNull(message = "线路ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 线路代码(唯一)
     */
    @NotBlank(message = "线路代码(唯一)不能为空", groups = { AddGroup.class, EditGroup.class })
    private String code;

    /**
     * 线路名称
     */
    @NotBlank(message = "线路名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 行程天数
     */
    @NotNull(message = "行程天数不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer durationDays;

    /**
     * 旅行风格
     */
    @NotNull(message = "旅行风格不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long travelStyle;

    /**
     * 服务等级
     */
    @NotNull(message = "服务等级不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long serviceLevel;

    /**
     * 线路强度 1-5
     */
    @NotNull(message = "线路强度 1-5不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long physicalRating;

    /**
     * 线路类型
     */
    @NotNull(message = "线路类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long tripType;

    /**
     * CollectionTag
     */
    private String collectionTag;

    /**
     * 国家名称关键字
     */
    private String countryName;

    /**
     * 最小年龄
     */
    private Long minAge;

    /**
     * 基础价格
     */
    @NotNull(message = "基础价格不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal basePrice;

    /**
     * 销售价格
     */
    @NotNull(message = "销售价格不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal salePrice;

    /**
     * Single Supplement
     */
    @DecimalMin(value = "0.00", message = "Single Supplement不能小于0", groups = { AddGroup.class, EditGroup.class })
    @Digits(integer = 10, fraction = 2, message = "Single Supplement最多保留两位小数", groups = { AddGroup.class, EditGroup.class })
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
    @NotNull(message = "状态 0草稿 1上架 2下架不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long status;

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

    /**
     * 查询参数
     */
    private Map<String, Object> params = new HashMap<>();

}

