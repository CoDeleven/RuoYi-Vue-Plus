package com.boxhilltravel.customer.domain.bo;

import lombok.Data;

import java.util.List;

/**
 * Customer tour query request.
 */
@Data
public class QueryTourBo {

    private List<Integer> travelStyleList;

    private List<Integer> collectionList;

    private List<Integer> durationList;

    private Integer pageNum;

    private Integer pageSize;

    private String sortDirection;

    private String sortField;

    private Long destinationId;

    private String keyword;

}
