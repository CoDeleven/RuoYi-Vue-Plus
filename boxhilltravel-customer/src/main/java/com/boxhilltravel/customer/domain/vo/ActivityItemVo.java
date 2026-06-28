package com.boxhilltravel.customer.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * Itinerary activity item.
 */
@Data
public class ActivityItemVo {

    private String title;

    private List<String> subtitle;

    private String description;

    private Boolean showInPreview;

    private String iconPath = "M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5";

}
