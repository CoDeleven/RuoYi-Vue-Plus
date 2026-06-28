package com.boxhilltravel.manager.service;

/**
 * H5 tour search read model service.
 */
public interface IHolidaysTourSearchService {

    /**
     * Rebuild one tour search row.
     *
     * @param tourId tour id
     */
    void rebuildTourSearch(Long tourId);

    /**
     * Delete one tour search row.
     *
     * @param tourId tour id
     */
    void deleteTourSearch(Long tourId);

}
