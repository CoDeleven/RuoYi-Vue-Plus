package com.boxhilltravel.customer.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * Checkout quote request.
 */
@Data
public class CheckoutQuoteBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Tour id is required")
    private Long tourId;

    @NotNull(message = "Departure id is required")
    private Long departureId;

    @NotNull(message = "Traveler count is required")
    @Min(value = 1, message = "Traveler count must be at least 1")
    @Max(value = 10, message = "Traveler count cannot exceed 10")
    private Integer travelerCount;

    @Valid
    private List<SelectedExtraBo> tourExtras;

    private TravelExtrasBo travelExtras;

    @Data
    public static class SelectedExtraBo implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private Long refId;

    }

    @Data
    public static class TravelExtrasBo implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private LocalDate arriveDate;

        private LocalDate departDate;

    }

}
