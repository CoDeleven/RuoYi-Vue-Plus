package com.boxhilltravel.customer.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Checkout tour extra day display item.
 */
@Data
public class TourExtraDayOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Integer dayNo;

    private String title;

    private String description;

    private List<TourExtraOptionVo> activities;

}
