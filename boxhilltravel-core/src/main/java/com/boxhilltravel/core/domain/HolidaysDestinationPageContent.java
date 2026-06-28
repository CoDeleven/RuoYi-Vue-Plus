package com.boxhilltravel.core.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * Destination country page content object holidays_destination_page_content.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("holidays_destination_page_content")
public class HolidaysDestinationPageContent extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long destinationId;

    private String currencyDictValue;

    private String languageDictValue;

    private String timeZoneDictValue;

    private String visa;

    private String visaTitle;

    private String visaNote;

    private String introductionJson;

    private String highlightsJson;

    private String citiesJson;

    private String practicalInfoJson;

    /**
     * 0 draft, 1 published.
     */
    private Long status;

}
