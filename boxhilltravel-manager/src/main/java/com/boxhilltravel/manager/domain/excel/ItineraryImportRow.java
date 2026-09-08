package com.boxhilltravel.manager.domain.excel;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fesod.sheet.annotation.ExcelProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * "Itinerary" sheet row of the tour bulk import workbook.
 */
@Data
@NoArgsConstructor
public class ItineraryImportRow implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "tour_code")
    private String tourCode;

    @ExcelProperty(value = "day_number")
    private Integer dayNumber;

    @ExcelProperty(value = "title")
    private String title;

    @ExcelProperty(value = "description")
    private String description;

    @ExcelProperty(value = "from_country")
    private String fromCountry;

    @ExcelProperty(value = "to_country")
    private String toCountry;

    @ExcelProperty(value = "breakfast")
    private String breakfast;

    @ExcelProperty(value = "lunch")
    private String lunch;

    @ExcelProperty(value = "dinner")
    private String dinner;

}
