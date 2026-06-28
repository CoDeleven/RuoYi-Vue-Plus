package com.boxhilltravel.customer.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.boxhilltravel.customer.domain.vo.FaqGroupVo;
import com.boxhilltravel.customer.service.IHolidaysFaqCustomerService;
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
 * Customer FAQ API.
 */
@SaIgnore
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/faq")
public class HolidaysFaqApi extends BaseController {

    private final IHolidaysFaqCustomerService faqCustomerService;

    @GetMapping("/list")
    public R<List<FaqGroupVo>> list(@RequestParam(required = false) Integer module) {
        return R.ok(faqCustomerService.queryFaqTree(module));
    }

}
