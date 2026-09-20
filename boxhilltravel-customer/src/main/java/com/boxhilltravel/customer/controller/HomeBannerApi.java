package com.boxhilltravel.customer.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.boxhilltravel.customer.domain.vo.HomeBannerVo;
import com.boxhilltravel.customer.service.IHolidaysHomeBannerCustomerService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Customer homepage API.
 */
@SaIgnore
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/home")
public class HomeBannerApi extends BaseController {

    private final IHolidaysHomeBannerCustomerService homeBannerCustomerService;

    @GetMapping("/banners")
    public R<List<HomeBannerVo>> banners(@RequestParam(required = false) Integer limit) {
        return R.ok(homeBannerCustomerService.queryEnabledBanners(limit));
    }

}