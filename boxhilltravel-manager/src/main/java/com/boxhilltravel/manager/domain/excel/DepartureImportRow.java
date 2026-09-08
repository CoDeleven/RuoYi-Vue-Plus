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
 * "Departures" sheet row of the tour bulk import workbook.
 */
@Data
@NoArgsConstructor
public class DepartureImportRow implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "tour_code")
    private String tourCode;

    @ExcelProperty(value = "departure_date")
    private String departureDate;

    @ExcelProperty(value = "return_date")
    private String returnDate;

    @ExcelProperty(value = "departure_type", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "1=Fixed,2=Flexible")
    private Integer departureType;

    @ExcelProperty(value = "max_capacity")
    private Long maxCapacity;

    @ExcelProperty(value = "min_capacity")
    private Long minCapacity;

    @ExcelProperty(value = "base_price")
    private BigDecimal basePrice;

    @ExcelProperty(value = "sale_price")
    private BigDecimal salePrice;

    @ExcelProperty(value = "status", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "1=Open,2=Full,3=Ended,4=Cancelled")
    private Integer status;

}
