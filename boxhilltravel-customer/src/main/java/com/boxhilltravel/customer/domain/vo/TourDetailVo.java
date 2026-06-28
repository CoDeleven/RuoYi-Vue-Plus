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
 * Customer tour detail.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TourDetailVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String code;

    private String name;

    private String description;

    private List<String> destinations;

    private Integer durationDays;

    private Integer travelStyle;

    private Integer serviceLevel;

    private Integer physicalRating;

    private BigDecimal basePrice;

    private BigDecimal salePrice;

    private BigDecimal singleSupplement;

    private String currency;

    @Translation(type = TransConstant.OSS_ID_TO_URL)
    private String coverImage;

    @Translation(type = TransConstant.OSS_ID_TO_URL)
    private String mapImage;

    private String notes;

    private String latestArrivalTime;

    private String earliestDepartureTime;

    private List<String> includedItems;

    private List<String> excludedItems;

    private Integer minAge;

    private Integer status;

    private Long version;

    private Integer travelCollection;

    private Integer tripType;

    private String upcomingDepartureDate;

    private List<ItineraryItemVo> itineraryList;

}
