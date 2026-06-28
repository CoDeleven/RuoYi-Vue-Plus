package com.boxhilltravel.customer.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.boxhilltravel.customer.domain.bo.CustomerSocialLoginBo;
import com.boxhilltravel.customer.domain.vo.CustomerLoginVo;
import com.boxhilltravel.customer.service.ICustomerAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customer authentication API.
 */
@SaIgnore
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class CustomerAuthApi {

    private final ICustomerAuthService customerAuthService;

    @GetMapping("/social/binding/{source}")
    public R<String> socialBinding(@PathVariable String source) {
        return R.data(customerAuthService.socialBindingUrl(source));
    }

    @PostMapping("/social/login")
    public R<CustomerLoginVo> socialLogin(@Valid @RequestBody CustomerSocialLoginBo bo) {
        return R.ok(customerAuthService.socialLogin(bo));
    }

}
