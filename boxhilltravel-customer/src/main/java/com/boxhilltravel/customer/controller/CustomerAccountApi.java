package com.boxhilltravel.customer.controller;

import com.boxhilltravel.customer.domain.bo.UpdateCustomerProfileBo;
import com.boxhilltravel.customer.domain.vo.CustomerAccountProfileVo;
import com.boxhilltravel.customer.service.ICustomerAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customer account API.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/account")
public class CustomerAccountApi extends BaseController {

    private final ICustomerAccountService customerAccountService;

    @GetMapping("/profile")
    public R<CustomerAccountProfileVo> profile() {
        return R.ok(customerAccountService.getProfile());
    }

    @PutMapping("/profile")
    public R<CustomerAccountProfileVo> updateProfile(@Valid @RequestBody UpdateCustomerProfileBo bo) {
        return R.ok(customerAccountService.updateProfile(bo));
    }

}
