package com.boxhilltravel.core.domain.vo;

import com.boxhilltravel.core.domain.HolidaysTourDestination;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Tour destination relation view object.
 */
@Data
@AutoMapper(target = HolidaysTourDestination.class)
public class HolidaysTourDestinationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long tourId;

    private Long destinationId;

    private Integer sequence;

}
