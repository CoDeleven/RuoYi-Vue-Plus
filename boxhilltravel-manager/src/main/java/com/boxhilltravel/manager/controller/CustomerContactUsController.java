package com.boxhilltravel.manager.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.boxhilltravel.core.domain.bo.CustomerContactUsBo;
import com.boxhilltravel.core.domain.vo.CustomerContactUsVo;
import com.boxhilltravel.manager.service.ICustomerContactUsManageService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Customer contact us manager controller.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/boxhilltravel_manager/contact_us")
public class CustomerContactUsController extends BaseController {

    private final ICustomerContactUsManageService contactUsManageService;

    @SaCheckPermission("boxhilltravel_manager:contact_us:list")
    @GetMapping("/list")
    public R<PageResult<CustomerContactUsVo>> list(CustomerContactUsBo bo, PageQuery pageQuery) {
        return R.ok(contactUsManageService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("boxhilltravel_manager:contact_us:query")
    @GetMapping("/{id}")
    public R<CustomerContactUsVo> getInfo(@NotNull(message = "Contact us record id is required") @PathVariable Long id) {
        return R.ok(contactUsManageService.queryById(id));
    }

    @SaCheckPermission("boxhilltravel_manager:contact_us:edit")
    @Log(title = "Contact Us", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@Validated @RequestBody ContactUsStatusBo bo) {
        return toAjax(contactUsManageService.updateReadStatus(bo.getId(), bo.getReadStatus()));
    }

    @SaCheckPermission("boxhilltravel_manager:contact_us:remove")
    @Log(title = "Contact Us", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "Contact us record ids are required") @PathVariable Long[] ids) {
        return toAjax(contactUsManageService.deleteWithValidByIds(List.of(ids), true));
    }

    @Data
    public static class ContactUsStatusBo {
        @NotNull(message = "Contact us record id is required")
        private Long id;

        @NotNull(message = "Contact us read status is required")
        private Integer readStatus;
    }
}
