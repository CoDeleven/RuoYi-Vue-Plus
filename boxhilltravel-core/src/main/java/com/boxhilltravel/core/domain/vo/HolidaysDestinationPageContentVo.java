package com.boxhilltravel.core.domain.vo;

import com.boxhilltravel.core.domain.HolidaysDestinationPageContent;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Destination country page content view object.
 */
@Data
@AutoMapper(target = HolidaysDestinationPageContent.class)
public class HolidaysDestinationPageContentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long destinationId;

    private String currencyDictValue;

    private String languageDictValue;

    private String timeZoneDictValue;

    private String visa;

    private String visaTitle;

    private String visaNote;

    @JsonIgnore
    private String introductionJson;

    @JsonIgnore
    private String highlightsJson;

    @JsonIgnore
    private String citiesJson;

    @JsonIgnore
    private String practicalInfoJson;

    private List<String> introduction;

    private List<DestinationPageHighlightVo> highlights;

    private List<DestinationPageCityVo> cities;

    private List<DestinationPagePracticalInfoVo> practicalInfo;

    /**
     * 0 draft, 1 published.
     */
    private Long status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String remark;

    private String destinationName;

    private String destinationNameEn;

    private String destinationImage;

    @Translation(type = TransConstant.OSS_ID_TO_URL, mapper = "destinationImage")
    private String destinationImageUrl;

    private Long regionId;

    private String regionName;

    private String regionNameEn;

    private Integer completeness;

    private List<String> missingItems;

}
