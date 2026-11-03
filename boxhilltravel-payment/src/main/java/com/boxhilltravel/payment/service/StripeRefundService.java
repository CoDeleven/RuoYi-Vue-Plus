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
import com.stripe.param.RefundListParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Durable refund requests. Preparation commits before Stripe is called so an ambiguous
 * network failure never loses the request or changes its idempotency key.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class StripeRefundService {

    public static final String LATE_PAYMENT_REASON = "LATE_PAYMENT_AFTER_EXPIRATION";

    private final StripeProperties properties;
    private final ObjectProvider<StripeClient> stripeClientProvider;
    private final ObjectProvider<StripeRefundService> selfProvider;
    private final StripeCurrencyResolver currencyResolver;
    private final HolidaysOrderMapper orderMapper;
    private final HolidaysOrderPaymentMapper paymentMapper;
    private final HolidaysOrderRefundMapper refundMapper;
    private final Clock stripeClock;

    /** Creates a manager refund, returning PENDING when Stripe needs to be retried. */
    public RefundVo refund(Long orderId, RefundOrderBo bo) {
        if (bo == null) throw new ServiceException("Refund request is required");
        return createRefund(orderId, bo.getAmount(), bo.getReason());
    }

    /** Persists the request in a separate transaction before contacting Stripe. */
    public RefundVo createRefund(Long orderId, BigDecimal amount, String reason) {
        Long refundId = selfProvider.getObject().prepareRefund(orderId, amount, reason);
        return selfProvider.getObject().submitRefund(refundId);
    }

    /** Serializes refund reservations so managers and recovery workers cannot over-refund. */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Long prepareRefund(Long orderId, BigDecimal amount, String reason) {
        if (!properties.isEnabled()) throw new ServiceException("Stripe payment is not enabled");
        HolidaysOrder order = orderMapper.selectByIdForUpdate(orderId);
        if (order == null) throw new ServiceException("Order not found");
        HolidaysOrderPayment payment = paymentForOrder(orderId);
        if (payment == null || StringUtils.isBlank(payment.getStripePaymentIntentId())) {
            throw new ServiceException("Stripe payment intent is not available");
        }
        List<HolidaysOrderRefund> refunds = refundMapper.selectList(Wrappers.lambdaQuery(HolidaysOrderRefund.class)
            .eq(HolidaysOrderRefund::getOrderId, orderId));
        BigDecimal succeeded = sum(refunds, RefundStatus.SUCCEEDED);
        BigDecimal pending = sum(refunds, RefundStatus.PENDING);
        BigDecimal refunded = zero(order.getRefundedAmount()).max(succeeded);
        BigDecimal available = zero(order.getPaidAmount()).subtract(refunded).subtract(pending);
        if (amount == null || amount.signum() <= 0 || amount.compareTo(available) > 0) {
            throw new ServiceException("Refund amount exceeds the refundable balance");
        }
        String currency = currencyResolver.resolve(order.getCurrency());
        HolidaysOrderRefund record = new HolidaysOrderRefund();
        record.setOrderId(orderId);
        record.setPaymentId(payment.getId());
        record.setAmount(amount);
        record.setAmountMinor(MoneyMinorUnitConverter.toMinor(amount, currency));
        record.setCurrency(currency);
        record.setReason(StringUtils.substring(StringUtils.defaultIfBlank(StringUtils.trim(reason), "CUSTOMER_REQUEST"), 0, 255));
        record.setStatus(RefundStatus.PENDING);
        record.setCreateTime(LocalDateTime.now(stripeClock));
        record.setUpdateTime(record.getCreateTime());
        refundMapper.insert(record);
        return record.getId();
    }

    /** Enqueues exactly one late-payment refund while the caller holds the order transaction. */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Long enqueueLatePaymentRefund(Long orderId) {
        HolidaysOrder order = orderMapper.selectByIdForUpdate(orderId);
        if (order == null) throw new ServiceException("Order not found");
        HolidaysOrderRefund existing = refundMapper.selectOne(Wrappers.lambdaQuery(HolidaysOrderRefund.class)
            .eq(HolidaysOrderRefund::getOrderId, orderId)
            .eq(HolidaysOrderRefund::getReason, LATE_PAYMENT_REASON)
            .in(HolidaysOrderRefund::getStatus, RefundStatus.PENDING, RefundStatus.SUCCEEDED)
            .orderByDesc(HolidaysOrderRefund::getId).last("limit 1"));
        if (existing != null) return existing.getId();
        BigDecimal remaining = zero(order.getPaidAmount()).subtract(zero(order.getRefundedAmount()));
        if (remaining.signum() <= 0) return null;
        return prepareRefund(orderId, remaining, LATE_PAYMENT_REASON);
    }

    /**
     * Submits or reconciles the same refund record under an order lock. A connection failure
     * keeps it PENDING; the recovery scheduler retries it without creating a new request.
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public RefundVo submitRefund(Long refundId) {
        HolidaysOrderRefund record = refundMapper.selectById(refundId);
        if (record == null) throw new ServiceException("Refund record not found");
        HolidaysOrder order = orderMapper.selectByIdForUpdate(record.getOrderId());
        record = refundMapper.selectById(refundId);
        if (order == null || record == null) throw new ServiceException("Refund order not found");
        if (!RefundStatus.PENDING.equals(record.getStatus())) return toVo(record);
        HolidaysOrderPayment payment = paymentMapper.selectById(record.getPaymentId());
        StripeClient client = stripeClientProvider.getIfAvailable();
        if (!properties.isEnabled() || client == null || payment == null
            || StringUtils.isBlank(payment.getStripePaymentIntentId())) {
            throw new ServiceException("Stripe refund is not configured");
        }
        try {
            Refund remote;
            if (StringUtils.isNotBlank(record.getStripeRefundId())) {
                remote = client.v1().refunds().retrieve(record.getStripeRefundId());
            } else {
                // Stripe may prune idempotency keys after 24h. Reconcile metadata first,
                // including when Stripe succeeded but our response/DB commit was lost.
                remote = findExistingRefund(client, payment.getStripePaymentIntentId(), record.getId());
                if (remote == null) {
                    RefundCreateParams params = RefundCreateParams.builder()
                        .setPaymentIntent(payment.getStripePaymentIntentId())
                        .setAmount(record.getAmountMinor())
                        .putMetadata("orderId", String.valueOf(record.getOrderId()))
                        .putMetadata("refundId", String.valueOf(record.getId()))
                        .putMetadata("reason", record.getReason()).build();
                    remote = client.v1().refunds().create(params, RequestOptions.builder()
                        .setIdempotencyKey("refund:" + record.getId()).build());
                }
            }
            if (!record.getAmountMinor().equals(remote.getAmount())
                || !record.getCurrency().equalsIgnoreCase(remote.getCurrency())
                || !payment.getStripePaymentIntentId().equals(remote.getPaymentIntent())) {
                throw new ServiceException("Stripe refund does not match the local request");
            }
            record.setStripeRefundId(remote.getId());
            record.setStatus(normalizeRefundStatus(remote.getStatus()));
            record.setUpdateTime(LocalDateTime.now(stripeClock));
            refundMapper.updateById(record);
            if (RefundStatus.SUCCEEDED.equals(record.getStatus())) {
                recalculateRefunds(order, payment);
            }
        } catch (StripeException ex) {
            // Preparation has already committed. Preserve that request even after an
            // ambiguous timeout; a later attempt uses the same refund ID and key.
            record.setUpdateTime(LocalDateTime.now(stripeClock));
            refundMapper.updateById(record);
            log.warn("Stripe refund remains pending for retry, orderId={}, refundId={}", record.getOrderId(), refundId);
        }
        return toVo(record);
    }

    /** Applies a signed refund notification without regressing a successful refund. */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void updateRefund(String stripeRefundId, String status) {
        HolidaysOrderRefund record = refundMapper.selectOne(Wrappers.lambdaQuery(HolidaysOrderRefund.class)
            .eq(HolidaysOrderRefund::getStripeRefundId, stripeRefundId));
        if (record == null) return;
        HolidaysOrder order = orderMapper.selectByIdForUpdate(record.getOrderId());
        record = refundMapper.selectById(record.getId());
        if (order == null || record == null) return;
        if (!RefundStatus.SUCCEEDED.equals(record.getStatus())) {
            record.setStatus(normalizeRefundStatus(status));
            refundMapper.updateById(record);
        }
        if (RefundStatus.SUCCEEDED.equals(record.getStatus())) {
            HolidaysOrderPayment payment = paymentMapper.selectById(record.getPaymentId());
            if (payment != null) recalculateRefunds(order, payment);
        }
    }

    /** Returns persisted requests which still need submission or remote status reconciliation. */
    public List<Long> findPendingRefunds() {
        return refundMapper.selectList(Wrappers.lambdaQuery(HolidaysOrderRefund.class)
                .eq(HolidaysOrderRefund::getStatus, RefundStatus.PENDING)
                .orderByAsc(HolidaysOrderRefund::getUpdateTime).last("limit 50"))
            .stream().map(HolidaysOrderRefund::getId).toList();
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

    public List<RefundVo> list(Long orderId) {
        return refundMapper.selectList(Wrappers.lambdaQuery(HolidaysOrderRefund.class)
                .eq(HolidaysOrderRefund::getOrderId, orderId).orderByDesc(HolidaysOrderRefund::getId))
            .stream().map(this::toVo).toList();
    }

    private Refund findExistingRefund(StripeClient client, String paymentIntent, Long refundId) throws StripeException {
        for (Refund refund : client.v1().refunds().list(RefundListParams.builder()
            .setPaymentIntent(paymentIntent).setLimit(100L).build()).autoPagingIterable()) {
            if (refund.getMetadata() != null && String.valueOf(refundId).equals(refund.getMetadata().get("refundId"))) {
                return refund;
            }
        }
        return null;
    }

    private void recalculateRefunds(HolidaysOrder order, HolidaysOrderPayment payment) {
        List<HolidaysOrderRefund> records = refundMapper.selectList(Wrappers.lambdaQuery(HolidaysOrderRefund.class)
            .eq(HolidaysOrderRefund::getOrderId, order.getId()).eq(HolidaysOrderRefund::getStatus, RefundStatus.SUCCEEDED));
        BigDecimal refunded = zero(order.getRefundedAmount()).max(sum(records, RefundStatus.SUCCEEDED));
        boolean full = refunded.compareTo(zero(order.getPaidAmount())) >= 0;
        orderMapper.update(null, Wrappers.lambdaUpdate(HolidaysOrder.class)
            .set(HolidaysOrder::getRefundedAmount, refunded)
            .set(full, HolidaysOrder::getStatus, 5).eq(HolidaysOrder::getId, order.getId()));
        paymentMapper.update(null, Wrappers.lambdaUpdate(HolidaysOrderPayment.class)
            .set(HolidaysOrderPayment::getStatus, full ? PaymentStatus.REFUNDED : PaymentStatus.PARTIALLY_REFUNDED)
            .eq(HolidaysOrderPayment::getId, payment.getId()));
    }

    private HolidaysOrderPayment paymentForOrder(Long orderId) {
        return paymentMapper.selectOne(Wrappers.lambdaQuery(HolidaysOrderPayment.class)
            .eq(HolidaysOrderPayment::getOrderId, orderId).orderByDesc(HolidaysOrderPayment::getId).last("limit 1"));
    }

    private BigDecimal sum(List<HolidaysOrderRefund> refunds, String status) {
        return refunds.stream().filter(refund -> status.equals(refund.getStatus()))
            .map(HolidaysOrderRefund::getAmount).filter(java.util.Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal zero(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String normalizeRefundStatus(String status) {
        if ("succeeded".equalsIgnoreCase(status)) return RefundStatus.SUCCEEDED;
        if ("failed".equalsIgnoreCase(status)) return RefundStatus.FAILED;
        if ("canceled".equalsIgnoreCase(status)) return RefundStatus.CANCELED;
        return RefundStatus.PENDING;
    }
}
