package com.boxhilltravel.customer.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Checkout tour extra activity display item.
 */
@Data
public class TourExtraOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long itineraryId;

    private Integer dayNo;

    private String title;

    private List<String> subtitle;

    private String description;

    private String iconPath;

}
