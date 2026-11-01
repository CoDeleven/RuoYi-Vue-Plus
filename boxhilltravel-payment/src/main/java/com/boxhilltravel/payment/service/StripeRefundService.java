package com.boxhilltravel.payment.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.boxhilltravel.core.domain.HolidaysOrder;
import com.boxhilltravel.core.domain.HolidaysOrderPayment;
import com.boxhilltravel.core.domain.HolidaysOrderRefund;
import com.boxhilltravel.core.mapper.HolidaysOrderMapper;
import com.boxhilltravel.core.mapper.HolidaysOrderPaymentMapper;
import com.boxhilltravel.core.mapper.HolidaysOrderRefundMapper;
import com.boxhilltravel.payment.config.StripeProperties;
import com.boxhilltravel.payment.constant.PaymentStatus;
import com.boxhilltravel.payment.constant.RefundStatus;
import com.boxhilltravel.payment.domain.bo.RefundOrderBo;
import com.boxhilltravel.payment.domain.vo.RefundVo;
import com.boxhilltravel.payment.util.MoneyMinorUnitConverter;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.net.RequestOptions;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Stripe refund service.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class StripeRefundService {

    private final StripeProperties properties;
    private final ObjectProvider<StripeClient> stripeClientProvider;
    private final StripeCurrencyResolver currencyResolver;
    private final HolidaysOrderMapper orderMapper;
    private final HolidaysOrderPaymentMapper paymentMapper;
    private final HolidaysOrderRefundMapper refundMapper;

    /**
     * Creates a refund request for a manager.
     */
    @Transactional(rollbackFor = Exception.class)
    public RefundVo refund(Long orderId, RefundOrderBo bo) {
        if (bo == null) {
            throw new ServiceException("Refund request is required");
        }
        return createRefund(orderId, bo.getAmount(), bo.getReason());
    }

    /**
     * Creates a refund request for an internal late-payment recovery.
     */
    @Transactional(rollbackFor = Exception.class)
    public RefundVo createRefund(Long orderId, BigDecimal amount, String reason) {
        if (!properties.isEnabled()) {
            throw new ServiceException("Stripe payment is not enabled");
        }
        HolidaysOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("Order not found");
        }
        HolidaysOrderPayment payment = paymentMapper.selectOne(Wrappers.lambdaQuery(HolidaysOrderPayment.class)
            .eq(HolidaysOrderPayment::getOrderId, orderId)
            .orderByDesc(HolidaysOrderPayment::getId)
            .last("limit 1"));
        if (payment == null || StringUtils.isBlank(payment.getStripePaymentIntentId())) {
            throw new ServiceException("Stripe payment intent is not available");
        }
        BigDecimal paidAmount = order.getPaidAmount() == null ? BigDecimal.ZERO : order.getPaidAmount();
        BigDecimal refundedAmount = order.getRefundedAmount() == null ? BigDecimal.ZERO : order.getRefundedAmount();
        BigDecimal remaining = paidAmount.subtract(refundedAmount);
        if (amount == null || amount.signum() <= 0 || amount.compareTo(remaining) > 0) {
            throw new ServiceException("Refund amount exceeds the refundable balance");
        }

        String currency = currencyResolver.resolve(order.getCurrency());
        long amountMinor = MoneyMinorUnitConverter.toMinor(amount, currency);
        HolidaysOrderRefund refundRecord = new HolidaysOrderRefund();
        refundRecord.setOrderId(orderId);
        refundRecord.setPaymentId(payment.getId());
        refundRecord.setAmount(amount);
        refundRecord.setAmountMinor(amountMinor);
        refundRecord.setCurrency(currency);
        refundRecord.setReason(StringUtils.trim(reason));
        refundRecord.setStatus(RefundStatus.PENDING);
        refundMapper.insert(refundRecord);

        StripeClient client = stripeClientProvider.getIfAvailable();
        if (client == null) {
            throw new ServiceException("Stripe payment is not configured");
        }
        RefundCreateParams params = RefundCreateParams.builder()
            .setPaymentIntent(payment.getStripePaymentIntentId())
            .setAmount(amountMinor)
            .putMetadata("orderId", String.valueOf(orderId))
            .putMetadata("refundId", String.valueOf(refundRecord.getId()))
            .putMetadata("reason", StringUtils.defaultIfBlank(reason, "CUSTOMER_REQUEST"))
            .build();
        try {
            Refund refund = client.v1().refunds().create(params,
                RequestOptions.builder().setIdempotencyKey("refund:" + refundRecord.getId()).build());
            refundRecord.setStripeRefundId(refund.getId());
            refundRecord.setStatus(normalizeRefundStatus(refund.getStatus()));
            refundMapper.updateById(refundRecord);
            return toVo(refundRecord);
        } catch (StripeException ex) {
            refundRecord.setStatus(RefundStatus.FAILED);
            refundMapper.updateById(refundRecord);
            log.error("Stripe refund failed, orderId={}, refundId={}", orderId, refundRecord.getId(), ex);
            throw new ServiceException("Unable to create Stripe refund");
        }
    }

    public RefundVo toVo(HolidaysOrderRefund refund) {
        RefundVo vo = new RefundVo();
        vo.setId(refund.getId());
        vo.setOrderId(refund.getOrderId());
        vo.setStripeRefundId(refund.getStripeRefundId());
        vo.setAmount(refund.getAmount());
        vo.setCurrency(refund.getCurrency());
        vo.setStatus(refund.getStatus());
        vo.setReason(refund.getReason());
        vo.setCreateTime(refund.getCreateTime());
        return vo;
    }

    /**
     * Lists refunds for an order.
     */
    public List<RefundVo> list(Long orderId) {
        return refundMapper.selectList(Wrappers.lambdaQuery(HolidaysOrderRefund.class)
                .eq(HolidaysOrderRefund::getOrderId, orderId)
                .orderByDesc(HolidaysOrderRefund::getId))
            .stream().map(this::toVo).toList();
    }

    private String normalizeRefundStatus(String status) {
        if ("succeeded".equalsIgnoreCase(status)) {
            return RefundStatus.SUCCEEDED;
        }
        if ("failed".equalsIgnoreCase(status)) {
            return RefundStatus.FAILED;
        }
        if ("canceled".equalsIgnoreCase(status)) {
            return RefundStatus.CANCELED;
        }
        return RefundStatus.PENDING;
    }

}
