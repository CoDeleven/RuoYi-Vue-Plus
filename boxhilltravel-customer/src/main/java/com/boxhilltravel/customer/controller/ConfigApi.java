package com.boxhilltravel.customer.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.boxhilltravel.customer.domain.vo.AppConfigVo;
import com.boxhilltravel.customer.service.IAppConfigService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customer config API.
 */
@SaIgnore
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/config")
public class ConfigApi extends BaseController {

    private final IAppConfigService appConfigService;

    @GetMapping("/list")
    public R<AppConfigVo> getConfig() {
        return R.ok(appConfigService.getAppConfig());
    }

}
