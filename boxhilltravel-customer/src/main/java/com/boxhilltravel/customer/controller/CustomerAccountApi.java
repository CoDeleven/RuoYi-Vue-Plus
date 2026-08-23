package com.boxhilltravel.customer.controller;

import com.boxhilltravel.customer.domain.bo.CustomerEmailCodeBo;
import com.boxhilltravel.customer.domain.bo.CustomerSocialBindingBo;
import com.boxhilltravel.customer.domain.bo.UpdateCustomerEmailBo;
import com.boxhilltravel.customer.domain.bo.UpdateCustomerPasswordBo;
import com.boxhilltravel.customer.domain.bo.UpdateCustomerProfileBo;
import com.boxhilltravel.customer.domain.vo.CustomerAccountProfileVo;
import com.boxhilltravel.customer.domain.vo.CustomerAccountSecurityVo;
import com.boxhilltravel.customer.service.ICustomerAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/security")
    public R<CustomerAccountSecurityVo> security() {
        return R.ok(customerAccountService.getSecurity());
    }

    @PostMapping("/email/code")
    public R<Void> sendEmailCode(@Valid @RequestBody CustomerEmailCodeBo bo) {
        customerAccountService.sendEmailCode(bo);
        return R.ok();
    }

    @PutMapping("/email")
    public R<Void> updateEmail(@Valid @RequestBody UpdateCustomerEmailBo bo) {
        customerAccountService.updateEmail(bo);
        return R.ok();
    }

    @PutMapping("/password")
    public R<Void> updatePassword(@Valid @RequestBody UpdateCustomerPasswordBo bo) {
        customerAccountService.updatePassword(bo);
        return R.ok();
    }

    @GetMapping("/social/binding/{source}")
    public R<String> socialBinding(@PathVariable String source) {
        return R.data(customerAccountService.socialBindingUrl(source));
    }

    @PostMapping("/social/binding")
    public R<Void> bindSocial(@Valid @RequestBody CustomerSocialBindingBo bo) {
        customerAccountService.bindSocial(bo);
        return R.ok();
    }

    @DeleteMapping("/social/{source}")
    public R<Void> unbindSocial(@PathVariable String source) {
        customerAccountService.unbindSocial(source);
        return R.ok();
    }
}
