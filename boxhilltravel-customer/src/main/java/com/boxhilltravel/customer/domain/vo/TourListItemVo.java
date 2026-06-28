package com.boxhilltravel.customer.domain.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;

/**
 * Customer tour list item.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TourListItemVo extends TourCardVo {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<String> images;

    @Translation(type = TransConstant.OSS_ID_TO_URL)
    private String mapImage;

    private Integer minAge;

    private Integer serviceLevel;

    private Integer physicalRating;

    private Integer status;

    private Long version;

    private Integer travelCollection;

    private BigDecimal rating;

    private Integer reviewCount;

}
