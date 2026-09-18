package com.boxhilltravel.core.domain.bo;

import com.boxhilltravel.core.domain.HolidaysDestinationPageContent;
import com.boxhilltravel.core.domain.vo.DestinationPageCityVo;
import com.boxhilltravel.core.domain.vo.DestinationPageHighlightVo;
import com.boxhilltravel.core.domain.vo.DestinationPagePracticalInfoVo;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Destination country page content business object.
 */
@Data
@AutoMapper(target = HolidaysDestinationPageContent.class, reverseConvertGenerate = false)
public class HolidaysDestinationPageContentBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "{boxhilltravel.validation.id.required}", groups = { EditGroup.class })
    private Long id;

    @NotNull(message = "{boxhilltravel.validation.destination.required}", groups = { AddGroup.class, EditGroup.class })
    private Long destinationId;

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

    /**
     * 0 draft, 1 published.
     */
    private Long status;

    private String destinationName;

}
