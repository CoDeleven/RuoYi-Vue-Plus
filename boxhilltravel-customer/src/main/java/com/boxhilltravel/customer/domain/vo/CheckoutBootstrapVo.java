package com.boxhilltravel.customer.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Checkout initial data.
 */
@Data
public class CheckoutBootstrapVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private TourDetailVo tour;

    private List<DepartureItemVo> departures;

    private DepartureItemVo selectedDeparture;

    private List<TourExtraDayOptionVo> tourExtras;

    private CheckoutQuoteVo quote;

}
