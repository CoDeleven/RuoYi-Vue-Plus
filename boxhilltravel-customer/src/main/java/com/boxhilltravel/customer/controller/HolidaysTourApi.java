package com.boxhilltravel.customer.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.boxhilltravel.customer.domain.bo.QueryTourBo;
import com.boxhilltravel.customer.domain.vo.FeaturedTourVo;
import com.boxhilltravel.customer.domain.vo.HotDealTourVo;
import com.boxhilltravel.customer.domain.vo.TourDetailVo;
import com.boxhilltravel.customer.domain.vo.TourListItemVo;
import com.boxhilltravel.customer.service.IHolidaysTourCustomerService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
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
 * Customer tour API.
 */
@SaIgnore
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/tours")
public class HolidaysTourApi extends BaseController {

    private final IHolidaysTourCustomerService holidaysTourCustomerService;

    @PostMapping("/list")
    public R<PageResult<TourListItemVo>> list(@RequestBody(required = false) QueryTourBo bo) {
        return R.ok(holidaysTourCustomerService.queryPageList(bo));
    }

    @GetMapping("/detail/{id}")
    public R<TourDetailVo> detail(@NotNull(message = "Tour id is required") @PathVariable Long id) {
        return R.ok(holidaysTourCustomerService.queryDetail(id));
    }

    @GetMapping("/hot")
    public R<List<HotDealTourVo>> hot(@RequestParam(required = false) Integer limit) {
        return R.ok(holidaysTourCustomerService.queryHotTours(limit));
    }

    @GetMapping("/featured")
    public R<List<FeaturedTourVo>> featured(@RequestParam(required = false) Integer limit) {
        return R.ok(holidaysTourCustomerService.queryFeaturedTours(limit));
    }

}
