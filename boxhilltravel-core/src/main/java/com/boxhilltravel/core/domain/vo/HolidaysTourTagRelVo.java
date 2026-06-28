package com.boxhilltravel.core.domain.vo;

import com.boxhilltravel.core.domain.HolidaysTourTagRel;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Tour tag relation view object.
 */
@Data
@AutoMapper(target = HolidaysTourTagRel.class)
public class HolidaysTourTagRelVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long tourId;

    private String dictCode;

}
