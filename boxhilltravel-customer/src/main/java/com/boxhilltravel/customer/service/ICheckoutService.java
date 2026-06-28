package com.boxhilltravel.customer.service;

import com.boxhilltravel.customer.domain.bo.CheckoutQuoteBo;
import com.boxhilltravel.customer.domain.vo.CheckoutBootstrapVo;
import com.boxhilltravel.customer.domain.vo.CheckoutQuoteVo;

/**
 * Customer checkout service.
 */
public interface ICheckoutService {

    CheckoutBootstrapVo bootstrap(Long tourId, Long departureId, Integer travelerCount);

    CheckoutQuoteVo quote(CheckoutQuoteBo bo);

}
