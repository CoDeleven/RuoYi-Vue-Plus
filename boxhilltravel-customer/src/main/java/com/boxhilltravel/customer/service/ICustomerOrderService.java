package com.boxhilltravel.customer.service;

import com.boxhilltravel.core.domain.vo.HolidaysOrderVo;
import com.boxhilltravel.customer.domain.bo.CreateOrderBo;
import com.boxhilltravel.customer.domain.vo.OrderListItemVo;
import com.boxhilltravel.payment.domain.vo.PaymentCheckoutVo;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;

/**
 * Customer order service.
 */
public interface ICustomerOrderService {

    HolidaysOrderVo create(CreateOrderBo bo);

    HolidaysOrderVo queryById(Long id);

    PaymentCheckoutVo pay(Long id);

    PageResult<OrderListItemVo> queryMyOrders(PageQuery pageQuery, Integer status);

}
