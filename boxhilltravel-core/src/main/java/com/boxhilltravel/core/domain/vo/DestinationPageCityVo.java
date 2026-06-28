package com.boxhilltravel.core.domain.vo;

import lombok.Data;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;

import java.io.Serial;
import java.io.Serializable;

/**
 * City or attraction card payload for country page content.
 */
@Data
public class DestinationPageCityVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String label;

    private String name;

    private String image;

    @Translation(type = TransConstant.OSS_ID_TO_URL, mapper = "image")
    private String imageUrl;

    private String description;

}
