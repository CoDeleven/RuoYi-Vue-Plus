package com.boxhilltravel.manager.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.boxhilltravel.core.domain.bo.HolidaysOrderBo;
import com.boxhilltravel.core.domain.vo.HolidaysOrderVo;
import com.boxhilltravel.manager.service.IHolidaysOrderService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Order manager controller.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/boxhilltravel_manager/order")
public class HolidaysOrderController extends BaseController {

    private final IHolidaysOrderService holidaysOrderService;

    @SaCheckPermission("boxhilltravel_manager:order:list")
    @GetMapping("/list")
    public R<PageResult<HolidaysOrderVo>> list(HolidaysOrderBo bo, PageQuery pageQuery) {
        return R.ok(holidaysOrderService.queryPageList(bo, pageQuery));
    }

    @SaCheckPermission("boxhilltravel_manager:order:query")
    @GetMapping("/{id}")
    public R<HolidaysOrderVo> getInfo(@NotNull(message = "{boxhilltravel.validation.orderId.required}") @PathVariable Long id) {
        return R.ok(holidaysOrderService.queryById(id));
    }

    @SaCheckPermission("boxhilltravel_manager:order:edit")
    @Log(title = "Order mark completed", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}/markCompleted")
    public R<Void> markCompleted(@NotNull(message = "{boxhilltravel.validation.orderId.required}") @PathVariable Long id) {
        return toAjax(holidaysOrderService.markCompleted(id));
    }

}
