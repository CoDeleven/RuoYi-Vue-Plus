package com.boxhilltravel.customer.domain.vo;

import com.boxhilltravel.core.domain.vo.DestinationPageCityVo;
import com.boxhilltravel.core.domain.vo.DestinationPageHighlightVo;
import com.boxhilltravel.core.domain.vo.DestinationPagePracticalInfoVo;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Published country page content for the customer destination detail API.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DestinationPageContentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String currencyDictValue;

    private String languageDictValue;

    private String timeZoneDictValue;

    private String visa;

    private String visaTitle;

    private String visaNote;

    private List<String> introduction;

    private List<DestinationPageHighlightVo> highlights;

    private List<DestinationPageCityVo> cities;

    private List<DestinationPagePracticalInfoVo> practicalInfo;

}
