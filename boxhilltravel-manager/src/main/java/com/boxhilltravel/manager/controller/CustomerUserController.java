package com.boxhilltravel.manager.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.boxhilltravel.manager.domain.bo.CustomerUserBo;
import com.boxhilltravel.manager.domain.vo.CustomerUserDetailVo;
import com.boxhilltravel.manager.domain.vo.CustomerUserVo;
import com.boxhilltravel.manager.service.ICustomerUserManageService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customer user manager controller.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/boxhilltravel_manager/customer_user")
public class CustomerUserController extends BaseController {

    private final ICustomerUserManageService customerUserManageService;

    @SaCheckPermission("boxhilltravel_manager:customer_user:list")
    @GetMapping("/list")
    public R<PageResult<CustomerUserVo>> list(CustomerUserBo bo, PageQuery pageQuery) {
        return R.ok(customerUserManageService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("boxhilltravel_manager:customer_user:query")
    @GetMapping("/{userId}")
    public R<CustomerUserDetailVo> getInfo(@NotNull(message = "{boxhilltravel.validation.customerUserId.required}") @PathVariable Long userId) {
        return R.ok(customerUserManageService.queryById(userId));
    }

    @SaCheckPermission("boxhilltravel_manager:customer_user:status")
    @Log(title = "Customer user status", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody CustomerUserBo bo) {
        return toAjax(customerUserManageService.updateStatus(bo.getUserId(), bo.getStatus()));
    }

}
