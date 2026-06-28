package com.boxhilltravel.customer.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Shared customer tour card fields.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TourCardVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String code;

    private String name;

    private String description;

    @Translation(type = TransConstant.OSS_ID_TO_URL)
    private String coverImage;

    private Integer durationDays;

    private Integer travelStyle;

    private BigDecimal basePrice;

    private BigDecimal salePrice;

    private BigDecimal singleSupplement;

    private String currency;

    private Integer tripType;

    private String upcomingDepartureDate;

    private List<String> destinations;

}
