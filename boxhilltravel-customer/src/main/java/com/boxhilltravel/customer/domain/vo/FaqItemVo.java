package com.boxhilltravel.customer.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * FAQ item.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FaqItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long groupId;

    private Integer module;

    private String question;

    private String answer;

    private Integer sortOrder;

}
