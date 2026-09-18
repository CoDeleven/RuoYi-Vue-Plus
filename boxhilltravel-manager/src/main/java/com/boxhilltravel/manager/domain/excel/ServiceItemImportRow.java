package com.boxhilltravel.manager.domain.excel;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;

import java.io.Serial;
import java.io.Serializable;

/**
 * "ServiceItems" sheet row of the tour bulk import workbook.
 */
@Data
@NoArgsConstructor
public class ServiceItemImportRow implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "tour_code")
    private String tourCode;

    @ExcelProperty(value = "sort_order")
    private Integer sortOrder;

    @ExcelProperty(value = "type", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "1=Included,2=Excluded")
    private Long type;

    @ExcelProperty(value = "content")
    private String content;

}
