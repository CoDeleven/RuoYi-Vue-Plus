package com.boxhilltravel.manager.domain.excel;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fesod.sheet.annotation.ExcelProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * "Reviews" sheet row of the tour bulk import workbook - seeded reviews entered by the
 * team, not real customer submissions (holidays_review, source=manager).
 */
@Data
@NoArgsConstructor
public class ReviewImportRow implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "tour_code")
    private String tourCode;

    @ExcelProperty(value = "nickname")
    private String nickname;

    @ExcelProperty(value = "rating")
    private Integer rating;

    @ExcelProperty(value = "title")
    private String title;

    @ExcelProperty(value = "content")
    private String content;

    @ExcelProperty(value = "featured")
    private String featured;

}
