package com.boxhilltravel.manager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.boxhilltravel.core.domain.HolidaysOrder;
import com.boxhilltravel.core.domain.HolidaysOrderExtra;
import com.boxhilltravel.core.domain.HolidaysOrderPayment;
import com.boxhilltravel.core.domain.HolidaysOrderTraveler;
import com.boxhilltravel.core.domain.bo.HolidaysOrderBo;
import com.boxhilltravel.core.domain.vo.HolidaysOrderExtraVo;
import com.boxhilltravel.core.domain.vo.HolidaysOrderPaymentVo;
import com.boxhilltravel.core.domain.vo.HolidaysOrderTravelerVo;
import com.boxhilltravel.core.domain.vo.HolidaysOrderVo;
import com.boxhilltravel.core.mapper.HolidaysOrderExtraMapper;
import com.boxhilltravel.core.mapper.HolidaysOrderMapper;
import com.boxhilltravel.core.mapper.HolidaysOrderPaymentMapper;
import com.boxhilltravel.core.mapper.HolidaysOrderTravelerMapper;
import com.boxhilltravel.manager.service.IHolidaysOrderService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Order manager service implementation.
 */
@RequiredArgsConstructor
@Service
public class HolidaysOrderServiceImpl implements IHolidaysOrderService {

    private static final Integer ORDER_STATUS_PAID = 1;
    private static final Integer ORDER_STATUS_CONFIRMED = 2;
    private static final Integer ORDER_STATUS_COMPLETED = 3;
    private static final Integer ORDER_STATUS_CANCELLED = 4;
    private static final Integer ORDER_STATUS_REFUNDED = 5;

    private final HolidaysOrderMapper orderMapper;
    private final HolidaysOrderTravelerMapper travelerMapper;
    private final HolidaysOrderExtraMapper extraMapper;
    private final HolidaysOrderPaymentMapper paymentMapper;

    @Override
    public PageResult<HolidaysOrderVo> queryPageList(HolidaysOrderBo bo, PageQuery pageQuery) {
        Page<HolidaysOrderVo> page = orderMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    @Override
    public HolidaysOrderVo queryById(Long id) {
        HolidaysOrderVo vo = orderMapper.selectVoById(id);
        if (vo == null) {
            throw new ServiceException("Order not found");
        }
        fillChildren(vo);
        return vo;
    }

    @Override
    public Boolean markCompleted(Long id) {
        HolidaysOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new ServiceException("Order not found");
        }
        Integer status = order.getStatus();
        if (ORDER_STATUS_COMPLETED.equals(status)) {
            throw new ServiceException("Order is already completed");
        }
        if (ORDER_STATUS_CANCELLED.equals(status) || ORDER_STATUS_REFUNDED.equals(status)) {
            throw new ServiceException("Cancelled or refunded orders cannot be marked as completed");
        }
        if (!ORDER_STATUS_PAID.equals(status) && !ORDER_STATUS_CONFIRMED.equals(status)) {
            throw new ServiceException("Only paid or confirmed orders can be marked as completed");
        }
        int updated = orderMapper.update(null, Wrappers.lambdaUpdate(HolidaysOrder.class)
            .eq(HolidaysOrder::getId, id)
            .eq(HolidaysOrder::getStatus, status)
            .set(HolidaysOrder::getStatus, ORDER_STATUS_COMPLETED));
        if (updated == 0) {
            throw new ServiceException("Order status has changed");
        }
        return true;
    }

    private LambdaQueryWrapper<HolidaysOrder> buildQueryWrapper(HolidaysOrderBo bo) {
        HolidaysOrderBo query = bo == null ? new HolidaysOrderBo() : bo;
        LambdaQueryWrapper<HolidaysOrder> wrapper = Wrappers.lambdaQuery(HolidaysOrder.class)
            .like(StringUtils.isNotBlank(query.getOrderNo()), HolidaysOrder::getOrderNo, query.getOrderNo())
            .like(StringUtils.isNotBlank(query.getTourName()), HolidaysOrder::getTourName, query.getTourName())
            .eq(query.getStatus() != null, HolidaysOrder::getStatus, query.getStatus());
        if (StringUtils.isNotBlank(query.getCustomerKeyword())) {
            wrapper.and(w -> w.like(HolidaysOrder::getCustomerUsername, query.getCustomerKeyword())
                .or()
                .like(HolidaysOrder::getCustomerEmail, query.getCustomerKeyword()));
        }
        Map<String, Object> params = query.getParams();
        if (params != null) {
            Object beginCreateTime = params.get("beginCreateTime");
            Object endCreateTime = params.get("endCreateTime");
            if (beginCreateTime != null && StringUtils.isNotBlank(beginCreateTime.toString())) {
                wrapper.ge(HolidaysOrder::getCreateTime, beginCreateTime);
            }
            if (endCreateTime != null && StringUtils.isNotBlank(endCreateTime.toString())) {
                wrapper.le(HolidaysOrder::getCreateTime, endCreateTime);
            }
        }
        return wrapper.orderByDesc(HolidaysOrder::getCreateTime).orderByDesc(HolidaysOrder::getId);
    }

    private void fillChildren(HolidaysOrderVo vo) {
        List<HolidaysOrderTravelerVo> travelers = travelerMapper.selectVoList(Wrappers.lambdaQuery(HolidaysOrderTraveler.class)
            .eq(HolidaysOrderTraveler::getOrderId, vo.getId())
            .orderByAsc(HolidaysOrderTraveler::getTravelerNo)
            .orderByAsc(HolidaysOrderTraveler::getId));
        List<HolidaysOrderExtraVo> extras = extraMapper.selectVoList(Wrappers.lambdaQuery(HolidaysOrderExtra.class)
            .eq(HolidaysOrderExtra::getOrderId, vo.getId())
            .orderByAsc(HolidaysOrderExtra::getId));
        HolidaysOrderPaymentVo payment = paymentMapper.selectVoOne(Wrappers.lambdaQuery(HolidaysOrderPayment.class)
            .eq(HolidaysOrderPayment::getOrderId, vo.getId())
            .orderByAsc(HolidaysOrderPayment::getId)
            .last("limit 1"));
        vo.setTravelers(travelers);
        vo.setExtras(extras);
        vo.setPayment(payment);
    }

}
