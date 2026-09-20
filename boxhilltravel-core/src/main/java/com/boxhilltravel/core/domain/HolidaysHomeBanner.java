package com.boxhilltravel.core.domain;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 首页横幅对象 holidays_home_banner
 *
 * @author BoxHillTravel
 * @date 2026-09-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("holidays_home_banner")
public class HolidaysHomeBanner extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 横幅图片(OSS id)
     */
    private String image;

    /**
     * 替代文本
     */
    private String altText;

    /**
     * 标题
     */
    private String title;

    /**
     * 副标题
     */
    private String subtitle;

    /**
     * 跳转链接
     */
    private String linkUrl;

    /**
     * 展示顺序，数值越小越靠前
     */
    private Integer sortOrder;

    /**
     * 状态 0禁用 1启用
     */
    private Long status;

}