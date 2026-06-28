package com.boxhilltravel.core.domain.query;

import lombok.Data;

import java.util.List;

/**
 * Customer-side tour list SQL query.
 */
@Data
public class CustomerTourQuery {

    private List<Integer> travelStyleList;

    private List<Integer> collectionList;

    private Integer minDurationDays;

    private Integer maxDurationDays;

    private List<Long> destinationIds;

    private String sortDirection;

    private String sortField;

    private String keyword;

}
