package com.boxhilltravel.core.domain.bo;

import com.boxhilltravel.core.domain.HolidaysOrder;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Order query business object.
 */
@Data
@AutoMapper(target = HolidaysOrder.class, reverseConvertGenerate = false)
public class HolidaysOrderBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String orderNo;

    private String customerKeyword;

    private String tourName;

    private Integer status;

    private Map<String, Object> params = new HashMap<>();

}
