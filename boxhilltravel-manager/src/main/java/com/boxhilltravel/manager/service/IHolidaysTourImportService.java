package com.boxhilltravel.manager.service;

import com.boxhilltravel.manager.domain.dto.TourImportRowResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Bulk tour import from the multi-sheet workbook (Tours / Destinations / Itinerary /
 * ServiceItems / Departures) so non-technical staff can create several tours at once
 * without going through every screen in the dashboard.
 */
public interface IHolidaysTourImportService {

    /**
     * Import tours from the uploaded workbook. Each row of the "Tours" sheet is
     * processed in its own transaction, so one bad tour doesn't block the rest.
     *
     * @param file uploaded .xlsx workbook
     * @return per-tour import outcome, in the order tours appear on the "Tours" sheet
     */
    List<TourImportRowResult> importTours(MultipartFile file);

}
