package com.boxhilltravel.customer.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.boxhilltravel.customer.domain.bo.CreateReviewBo;
import com.boxhilltravel.customer.domain.vo.ReviewItemVo;
import com.boxhilltravel.customer.domain.vo.ReviewSummaryVo;
import com.boxhilltravel.customer.service.IHolidaysReviewCustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Customer review API.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reviews")
public class HolidaysReviewApi extends BaseController {

    private final IHolidaysReviewCustomerService holidaysReviewCustomerService;

    @SaIgnore
    @GetMapping("/tour/{tourId}")
    public R<ReviewSummaryVo> tourReviews(@NotNull(message = "Tour id is required") @PathVariable Long tourId, PageQuery pageQuery) {
        return R.ok(holidaysReviewCustomerService.queryTourReviews(tourId, pageQuery));
    }

    @SaIgnore
    @GetMapping("/home")
    public R<List<ReviewItemVo>> home(@RequestParam(required = false) Integer limit) {
        return R.ok(holidaysReviewCustomerService.queryHomeReviews(limit));
    }

    @PostMapping
    public R<ReviewItemVo> create(@Valid @RequestBody CreateReviewBo bo) {
        return R.ok(holidaysReviewCustomerService.create(bo));
    }

}
