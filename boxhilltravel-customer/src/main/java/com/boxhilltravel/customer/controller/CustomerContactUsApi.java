package com.boxhilltravel.customer.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.boxhilltravel.core.domain.bo.CustomerContactUsBo;
import com.boxhilltravel.customer.service.ICustomerContactUsService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customer contact us API.
 */
@SaIgnore
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/contact-us")
public class CustomerContactUsApi extends BaseController {

    private final ICustomerContactUsService contactUsService;

    @RepeatSubmit
    @PostMapping
    public R<Void> submit(@Validated(AddGroup.class) @RequestBody CustomerContactUsBo bo) {
        return toAjax(contactUsService.submit(bo, ServletUtils.getClientIP()));
    }
}
