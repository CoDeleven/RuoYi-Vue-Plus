package com.boxhilltravel.core.domain.vo;

import com.boxhilltravel.core.domain.HolidaysDestinationTagRel;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Destination tag relation view object.
 */
@Data
@AutoMapper(target = HolidaysDestinationTagRel.class)
public class HolidaysDestinationTagRelVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long destinationId;

    private String dictValue;

    private Integer sortOrder;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String destinationName;

    private String destinationNameEn;

    private Long destinationLevel;

    private String destinationImage;

    @Translation(type = TransConstant.OSS_ID_TO_URL, mapper = "destinationImage")
    private String destinationImageUrl;

    private String destinationDescription;

    private Long regionId;

    private String regionName;

    private String regionNameEn;

    private String dictLabel;

    private Integer dictSort;

}
