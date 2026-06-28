package com.boxhilltravel.customer.controller;

import com.boxhilltravel.customer.domain.bo.CheckoutQuoteBo;
import com.boxhilltravel.customer.domain.vo.CheckoutBootstrapVo;
import com.boxhilltravel.customer.domain.vo.CheckoutQuoteVo;
import com.boxhilltravel.customer.service.ICheckoutService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customer checkout API.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutApi extends BaseController {

    private final ICheckoutService checkoutService;

    @GetMapping("/bootstrap")
    public R<CheckoutBootstrapVo> bootstrap(@NotNull(message = "Tour id is required") @RequestParam Long tourId,
                                            @RequestParam(required = false) Long departureId,
                                            @RequestParam(required = false) Integer travelerCount) {
        return R.ok(checkoutService.bootstrap(tourId, departureId, travelerCount));
    }

    @PostMapping("/quote")
    public R<CheckoutQuoteVo> quote(@Valid @RequestBody CheckoutQuoteBo bo) {
        return R.ok(checkoutService.quote(bo));
    }

}
