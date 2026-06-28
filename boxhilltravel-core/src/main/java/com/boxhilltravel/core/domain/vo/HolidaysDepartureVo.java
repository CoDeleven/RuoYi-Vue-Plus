package com.boxhilltravel.core.domain.vo;

import com.boxhilltravel.core.domain.HolidaysDeparture;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Tour departure view object.
 */
@Data
@AutoMapper(target = HolidaysDeparture.class)
public class HolidaysDepartureVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long tourId;

    private LocalDate departureDate;

    private LocalDate returnDate;

    private Long durationDays;

    private Integer departureType;

    private Long maxCapacity;

    private Long minCapacity;

    private Long bookedCount;

    private Long availableCount;

    private BigDecimal basePrice;

    private BigDecimal salePrice;

    private BigDecimal discountRate;

    private Integer status;

}
