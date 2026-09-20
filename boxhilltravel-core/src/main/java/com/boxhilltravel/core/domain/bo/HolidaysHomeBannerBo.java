package com.boxhilltravel.core.domain.bo;

import com.boxhilltravel.core.domain.HolidaysHomeBanner;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 首页横幅业务对象 holidays_home_banner
 *
 * @author BoxHillTravel
 * @date 2026-09-19
 */
@Data
@AutoMapper(target = HolidaysHomeBanner.class, reverseConvertGenerate = false)
public class HolidaysHomeBannerBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 横幅图片(OSS id)
     */
    @NotBlank(message = "横幅图片不能为空", groups = { AddGroup.class, EditGroup.class })
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
    @NotNull(message = "展示顺序不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer sortOrder;

    /**
     * 状态 0禁用 1启用
     */
    @NotNull(message = "状态不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long status;

}