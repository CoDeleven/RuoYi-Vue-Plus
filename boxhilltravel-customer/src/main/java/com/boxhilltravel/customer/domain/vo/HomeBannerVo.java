package com.boxhilltravel.customer.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;

import java.io.Serial;
import java.io.Serializable;

/**
 * Homepage hero banner.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HomeBannerVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @Translation(type = TransConstant.OSS_ID_TO_URL)
    private String image;

    private String altText;

    private String title;

    private String subtitle;

    private String linkUrl;

}