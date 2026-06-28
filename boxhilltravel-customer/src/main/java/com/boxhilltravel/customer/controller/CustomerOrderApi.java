package com.boxhilltravel.customer.controller;

import com.boxhilltravel.core.domain.vo.HolidaysOrderVo;
import com.boxhilltravel.customer.domain.bo.CreateOrderBo;
import com.boxhilltravel.customer.domain.vo.OrderListItemVo;
import com.boxhilltravel.customer.service.ICustomerOrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customer order API.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class CustomerOrderApi extends BaseController {

    private final ICustomerOrderService customerOrderService;

    @PostMapping
    public R<HolidaysOrderVo> create(@Valid @RequestBody CreateOrderBo bo) {
        return R.ok(customerOrderService.create(bo));
    }

    @GetMapping("/{id}")
    public R<HolidaysOrderVo> detail(@NotNull(message = "Order id is required") @PathVariable Long id) {
        return R.ok(customerOrderService.queryById(id));
    }

    @PostMapping("/{id}/pay")
    public R<HolidaysOrderVo> pay(@NotNull(message = "Order id is required") @PathVariable Long id) {
        return R.ok(customerOrderService.pay(id));
    }

    @GetMapping("/my")
    public R<PageResult<OrderListItemVo>> my(PageQuery pageQuery, @RequestParam(required = false) Integer status) {
        return R.ok(customerOrderService.queryMyOrders(pageQuery, status));
    }

}
