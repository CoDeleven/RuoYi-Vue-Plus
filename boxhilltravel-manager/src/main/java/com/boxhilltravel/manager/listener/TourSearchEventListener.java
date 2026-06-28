package com.boxhilltravel.manager.listener;

import com.boxhilltravel.manager.event.TourSearchRebuildEvent;
import com.boxhilltravel.manager.service.IHolidaysTourSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Rebuilds H5 tour search rows after manager write transactions commit.
 */
@RequiredArgsConstructor
@Component
public class TourSearchEventListener {

    private final IHolidaysTourSearchService tourSearchService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTourSearchRebuild(TourSearchRebuildEvent event) {
        if (event == null || event.tourId() == null) {
            return;
        }
        if (event.deleteOnly()) {
            tourSearchService.deleteTourSearch(event.tourId());
            return;
        }
        tourSearchService.rebuildTourSearch(event.tourId());
    }

}
