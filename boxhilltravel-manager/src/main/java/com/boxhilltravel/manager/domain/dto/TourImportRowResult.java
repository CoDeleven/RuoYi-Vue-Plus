package com.boxhilltravel.manager.domain.dto;

import lombok.Data;

/**
 * Outcome of importing one row of the "Tours" sheet (and everything linked to it
 * via tour_code on the other sheets).
 */
@Data
public class TourImportRowResult {

    /**
     * 1-based row number on the "Tours" sheet (header excluded), for user-facing messages.
     */
    private int rowNumber;

    private String tourCode;

    private boolean success;

    private String message;

    public static TourImportRowResult ok(int rowNumber, String tourCode) {
        TourImportRowResult result = new TourImportRowResult();
        result.setRowNumber(rowNumber);
        result.setTourCode(tourCode);
        result.setSuccess(true);
        result.setMessage("Imported successfully");
        return result;
    }

    public static TourImportRowResult fail(int rowNumber, String tourCode, String message) {
        TourImportRowResult result = new TourImportRowResult();
        result.setRowNumber(rowNumber);
        result.setTourCode(tourCode);
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }

}
