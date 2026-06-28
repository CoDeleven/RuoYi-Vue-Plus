package com.boxhilltravel.customer.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * FAQ group with items.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FaqGroupVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private Integer module;

    private String icon;

    private Integer sortOrder;

    private List<FaqItemVo> items;

}
