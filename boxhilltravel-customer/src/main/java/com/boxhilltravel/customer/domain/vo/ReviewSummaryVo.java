package com.boxhilltravel.customer.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * Customer-facing review page with rating summary.
 */
@Data
public class ReviewSummaryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Collection<ReviewItemVo> rows;

    private Long total;

    private Double averageRating;

    private Map<Integer, Long> ratingCounts;

}
