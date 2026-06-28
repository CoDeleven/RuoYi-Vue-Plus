package com.boxhilltravel.customer.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sort config item.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SortedItemVo {

    private String code;

    private String label;

    private String desc;

}
