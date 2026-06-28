package com.boxhilltravel.customer.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.annotation.TranslationType;
import org.dromara.common.translation.config.TranslationConfig;
import org.dromara.common.translation.constant.TransConstant;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Customer destination tree node.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DestinationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String nameEn;

    private Long parentId;

    private Integer level;

    @Translation(type = TransConstant.OSS_ID_TO_URL)
    private String image;

    private String description;

    private Integer sort;

    private List<DestinationVo> children;

    private DestinationPageContentVo pageContent;

}
