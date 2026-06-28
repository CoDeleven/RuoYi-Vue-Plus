package com.boxhilltravel.core.domain.vo;

import com.boxhilltravel.core.domain.HolidaysTourSearch;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * H5 tour search read model view object.
 */
@Data
@AutoMapper(target = HolidaysTourSearch.class)
public class HolidaysTourSearchVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long tourId;

    private String code;

    private String name;

    private String searchText;

    private Long status;

    private LocalDateTime updatedAt;

}
