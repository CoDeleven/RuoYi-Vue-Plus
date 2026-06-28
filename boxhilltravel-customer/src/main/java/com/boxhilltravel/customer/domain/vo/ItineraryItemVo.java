package com.boxhilltravel.customer.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Itinerary day item.
 */
@Data
public class ItineraryItemVo {

    @JsonProperty("day")
    private Integer dayNo;

    private String title;

    private String description;

    private List<String> highlights;

    private List<String> meals;

    private String accommodation;

    private List<ActivityItemVo> activities;

}
