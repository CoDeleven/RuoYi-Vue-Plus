package com.boxhilltravel.customer.controller;

import com.boxhilltravel.customer.domain.vo.TourListItemVo;
import com.boxhilltravel.customer.service.ICustomerWishlistService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Customer wishlist API.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/wishlist")
public class CustomerWishlistApi extends BaseController {

    private final ICustomerWishlistService customerWishlistService;

    @GetMapping("/list")
    public R<List<TourListItemVo>> list() {
        return R.ok(customerWishlistService.list());
    }

    @GetMapping("/ids")
    public R<List<Long>> ids() {
        return R.ok(customerWishlistService.ids());
    }

    @GetMapping("/status/{tourId}")
    public R<Boolean> status(@NotNull(message = "Tour id is required") @PathVariable Long tourId) {
        return R.ok(customerWishlistService.status(tourId));
    }

    @PostMapping("/{tourId}")
    public R<Void> add(@NotNull(message = "Tour id is required") @PathVariable Long tourId) {
        customerWishlistService.add(tourId);
        return R.ok();
    }

    @DeleteMapping("/{tourId}")
    public R<Void> remove(@NotNull(message = "Tour id is required") @PathVariable Long tourId) {
        customerWishlistService.remove(tourId);
        return R.ok();
    }

}
