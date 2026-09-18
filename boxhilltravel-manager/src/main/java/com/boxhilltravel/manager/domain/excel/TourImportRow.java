package com.boxhilltravel.manager.domain.excel;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * "Tours" sheet row of the tour bulk import workbook.
 */
@Data
@NoArgsConstructor
public class TourImportRow implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "tour_code")
    private String tourCode;

    @ExcelProperty(value = "name")
    private String name;

    @ExcelProperty(value = "description")
    private String description;

    @ExcelProperty(value = "duration_days")
    private Integer durationDays;

    @ExcelProperty(value = "travel_style", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "holidays_tour_travel_style")
    private Long travelStyle;

    @ExcelProperty(value = "service_level", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "holidays_tour_service_level")
    private Long serviceLevel;

    @ExcelProperty(value = "physical_rating", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "holidays_tour_physical_rating")
    private Long physicalRating;

    @ExcelProperty(value = "trip_type", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "holidays_tour_trip_type")
    private Long tripType;

    @ExcelProperty(value = "min_age")
    private Long minAge;

    @ExcelProperty(value = "base_price")
    private BigDecimal basePrice;

    @ExcelProperty(value = "sale_price")
    private BigDecimal salePrice;

    @ExcelProperty(value = "single_supplement")
    private BigDecimal singleSupplement;

    @ExcelProperty(value = "currency", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "holidays_currency_unit")
    private String currency;

    @ExcelProperty(value = "notes")
    private String notes;

    @ExcelProperty(value = "latest_arrival_time")
    private String latestArrivalTime;

    @ExcelProperty(value = "earliest_departure_time")
    private String earliestDepartureTime;

    @ExcelProperty(value = "status", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "0=Draft,1=Published,2=Off-shelf")
    private Long status;

    @ExcelProperty(value = "seo_title")
    private String seoTitle;

    @ExcelProperty(value = "seo_description")
    private String seoDescription;

    @ExcelProperty(value = "seo_keywords")
    private String seoKeywords;

    @ExcelProperty(value = "collection_tag", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "holidays_tour_collection")
    private String collectionTag;

}
