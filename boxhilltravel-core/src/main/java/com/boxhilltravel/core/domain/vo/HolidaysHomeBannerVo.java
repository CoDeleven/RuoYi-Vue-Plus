package com.boxhilltravel.core.domain.vo;

import com.boxhilltravel.core.domain.HolidaysHomeBanner;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 首页横幅视图对象 holidays_home_banner
 *
 * @author BoxHillTravel
 * @date 2026-09-19
 */
@Data
@AutoMapper(target = HolidaysHomeBanner.class)
public class HolidaysHomeBannerVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 横幅图片(OSS id)
     */
    private String image;

    /**
     * 横幅图片URL
     */
    @Translation(type = TransConstant.OSS_ID_TO_URL, mapper = "image")
    private String imageUrl;

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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}