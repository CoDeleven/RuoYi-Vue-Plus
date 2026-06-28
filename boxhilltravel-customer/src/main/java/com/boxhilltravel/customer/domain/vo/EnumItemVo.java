package com.boxhilltravel.customer.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Enum config item.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnumItemVo {

    private String code;

    private String label;

    private String desc;

}
