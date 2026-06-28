package com.boxhilltravel.manager.event;

/**
 * Event for rebuilding or deleting a tour search read model row.
 *
 * @param tourId tour id
 * @param deleteOnly whether to delete the search row only
 */
public record TourSearchRebuildEvent(Long tourId, boolean deleteOnly) {

    public static TourSearchRebuildEvent rebuild(Long tourId) {
        return new TourSearchRebuildEvent(tourId, false);
    }

    public static TourSearchRebuildEvent delete(Long tourId) {
        return new TourSearchRebuildEvent(tourId, true);
    }

}
