package com.boxhilltravel.manager.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.boxhilltravel.core.domain.bo.HolidaysOrderBo;
import com.boxhilltravel.core.domain.vo.HolidaysOrderVo;
import com.boxhilltravel.manager.service.IHolidaysOrderService;
import com.boxhilltravel.payment.domain.bo.RefundOrderBo;
import com.boxhilltravel.payment.domain.vo.RefundVo;
import com.boxhilltravel.payment.service.StripeRefundService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * Order manager controller.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/boxhilltravel_manager/order")
public class HolidaysOrderController extends BaseController {

    private final IHolidaysOrderService holidaysOrderService;
    private final StripeRefundService stripeRefundService;

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

    @SaCheckPermission("boxhilltravel_manager:order:refund")
    @Log(title = "Order refund", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/{id}/refund")
    public R<RefundVo> refund(@NotNull(message = "{boxhilltravel.validation.orderId.required}") @PathVariable Long id,
                              @Valid @RequestBody RefundOrderBo bo) {
        return R.ok(stripeRefundService.refund(id, bo));
    }

    @SaCheckPermission("boxhilltravel_manager:order:query")
    @GetMapping("/{id}/refunds")
    public R<List<RefundVo>> refunds(@NotNull(message = "{boxhilltravel.validation.orderId.required}") @PathVariable Long id) {
        return R.ok(stripeRefundService.list(id));
    }

}
