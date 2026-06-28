package com.boxhilltravel.customer.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.boxhilltravel.customer.domain.vo.DepartureItemVo;
import com.boxhilltravel.customer.service.IHolidaysDepartureCustomerService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Customer departure API.
 */
@SaIgnore
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/departures")
public class HolidaysDepartureApi {

    private final IHolidaysDepartureCustomerService departureCustomerService;

    @GetMapping("/list")
    public R<List<DepartureItemVo>> list(@NotNull(message = "Tour id is required") @RequestParam Long tourId,
                                         @RequestParam(required = false) String month) {
        return R.ok(departureCustomerService.queryDepartureListByTourId(tourId, month));
    }

}
