package com.boxhilltravel.customer.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Homepage popular destination.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PopularDestinationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String nameEn;

    @Translation(type = TransConstant.OSS_ID_TO_URL)
    private String image;

    private String description;

    private String continentName;

    private String continentNameEn;

    private BigDecimal price;

    private Integer duration;

    private Integer durationDays;

    private String currency;

}
