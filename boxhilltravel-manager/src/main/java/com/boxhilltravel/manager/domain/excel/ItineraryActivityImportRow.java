package com.boxhilltravel.manager.domain.excel;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fesod.sheet.annotation.ExcelProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * "ItineraryActivities" sheet row of the tour bulk import workbook - sub-bullets shown
 * within one day of the itinerary (holidays_tour_itinerary_activity).
 */
@Data
@NoArgsConstructor
public class ItineraryActivityImportRow implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "tour_code")
    private String tourCode;

    @ExcelProperty(value = "day_number")
    private Integer dayNumber;

    @ExcelProperty(value = "sort_order")
    private Integer sortOrder;

    @ExcelProperty(value = "title")
    private String title;

    @ExcelProperty(value = "description")
    private String description;

    @ExcelProperty(value = "subtitle")
    private String subtitle;

    @ExcelProperty(value = "show_in_preview")
    private String showInPreview;

}
