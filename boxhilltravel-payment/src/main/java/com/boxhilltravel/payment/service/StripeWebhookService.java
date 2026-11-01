package com.boxhilltravel.payment.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.boxhilltravel.core.domain.HolidaysOrder;
import com.boxhilltravel.core.domain.HolidaysOrderPayment;
import com.boxhilltravel.core.domain.HolidaysOrderRefund;
import com.boxhilltravel.core.domain.StripeWebhookEvent;
import com.boxhilltravel.core.mapper.HolidaysOrderMapper;
import com.boxhilltravel.core.mapper.HolidaysOrderPaymentMapper;
import com.boxhilltravel.core.mapper.HolidaysOrderRefundMapper;
import com.boxhilltravel.core.mapper.StripeWebhookEventMapper;
import com.boxhilltravel.payment.config.StripeProperties;
import com.boxhilltravel.payment.constant.PaymentStatus;
import com.boxhilltravel.payment.constant.RefundStatus;
import com.boxhilltravel.payment.constant.WebhookStatus;
import com.boxhilltravel.payment.util.MoneyMinorUnitConverter;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Stripe webhook verification and asynchronous processing.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class StripeWebhookService {

    private final StripeProperties properties;
    private final ObjectProvider<StripeWebhookService> selfProvider;
    private final StripePaymentService paymentService;
    private final StripeCurrencyResolver currencyResolver;
    private final StripeRefundService refundService;
    private final StripeWebhookEventMapper eventMapper;
    private final HolidaysOrderMapper orderMapper;
    private final HolidaysOrderPaymentMapper paymentMapper;
    private final HolidaysOrderRefundMapper refundMapper;

    /**
     * Verifies and accepts a Stripe webhook payload.
     */
    public void accept(String payload, String signature) {
        if (StringUtils.isBlank(properties.getWebhookSecret())) {
            throw new ServiceException("Stripe webhook secret is not configured");
        }
        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, properties.getWebhookSecret());
        } catch (SignatureVerificationException | IllegalArgumentException ex) {
            throw new ServiceException("Invalid Stripe webhook signature");
        }
        StripeWebhookEvent existing = eventMapper.selectOne(Wrappers.lambdaQuery(StripeWebhookEvent.class)
            .eq(StripeWebhookEvent::getEventId, event.getId()));
        if (existing != null) {
            return;
        }
        StripeWebhookEvent record = new StripeWebhookEvent();
        record.setEventId(event.getId());
        record.setEventType(event.getType());
        String rawObject = event.getDataObjectDeserializer().getRawJson();
        Map<String, Object> rawObjectMap = JsonUtils.parseObject(rawObject, Map.class);
        record.setObjectId(rawObjectMap == null ? null : String.valueOf(rawObjectMap.get("id")));
        record.setPayload(payload);
        record.setProcessStatus(WebhookStatus.PENDING);
        record.setReceivedAt(LocalDateTime.now());
        try {
            eventMapper.insert(record);
        } catch (Exception ex) {
            // A concurrent delivery can win the unique event_id insert.
            if (eventMapper.selectOne(Wrappers.lambdaQuery(StripeWebhookEvent.class)
                .eq(StripeWebhookEvent::getEventId, event.getId())) == null) {
                throw ex;
            }
            return;
        }
        selfProvider.getObject().processAsync(record.getId());
    }

    /**
     * Processes a previously accepted event.
     */
    @Async
    public void processAsync(Long recordId) {
        StripeWebhookEvent record = eventMapper.selectById(recordId);
        if (record == null) {
            return;
        }
        try {
            eventMapper.update(null, Wrappers.lambdaUpdate(StripeWebhookEvent.class)
                .set(StripeWebhookEvent::getProcessStatus, WebhookStatus.PROCESSING)
                .eq(StripeWebhookEvent::getId, recordId));
            process(record);
            eventMapper.update(null, Wrappers.lambdaUpdate(StripeWebhookEvent.class)
                .set(StripeWebhookEvent::getProcessStatus, WebhookStatus.SUCCEEDED)
                .set(StripeWebhookEvent::getProcessedAt, LocalDateTime.now())
                .set(StripeWebhookEvent::getErrorMessage, null)
                .eq(StripeWebhookEvent::getId, recordId));
        } catch (Exception ex) {
            log.error("Stripe webhook processing failed, eventId={}", record.getEventId(), ex);
            eventMapper.update(null, Wrappers.lambdaUpdate(StripeWebhookEvent.class)
                .set(StripeWebhookEvent::getProcessStatus, WebhookStatus.FAILED)
                .set(StripeWebhookEvent::getErrorMessage, StringUtils.substring(ex.getMessage(), 0, 1000))
                .eq(StripeWebhookEvent::getId, recordId));
        }
    }

    private void process(StripeWebhookEvent record) {
        Map<String, Object> root = JsonUtils.parseObject(record.getPayload(), Map.class);
        Map<String, Object> data = asMap(root.get("data"));
        Map<String, Object> object = asMap(data.get("object"));
        switch (record.getEventType()) {
            case "checkout.session.completed", "checkout.session.async_payment_succeeded" ->
                markPaid(object);
            case "checkout.session.async_payment_failed", "payment_intent.payment_failed" ->
                markFailed(object);
            case "checkout.session.expired" -> expire(object);
            case "refund.updated" -> updateRefund(object);
            case "charge.refunded" -> updateChargeRefund(object);
            default -> log.debug("Ignoring unsupported Stripe event type={}", record.getEventType());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    private void markPaid(Map<String, Object> object) {
        Long orderId = metadataOrderId(object);
        if (orderId == null) {
            return;
        }
        HolidaysOrder order = orderMapper.selectById(orderId);
        HolidaysOrderPayment payment = paymentForOrder(orderId);
        if (order == null || payment == null) {
            return;
        }
        String currency = MoneyMinorUnitConverter.normalizeCurrency(String.valueOf(object.get("currency")));
        String orderCurrency = currencyResolver.resolve(order.getCurrency());
        long amountMinor = number(object.get("amount_total"));
        long expectedMinor = MoneyMinorUnitConverter.toMinor(order.getTotalAmount(), orderCurrency);
        if (amountMinor != expectedMinor || !currency.equalsIgnoreCase(orderCurrency)) {
            throw new ServiceException("Stripe payment amount or currency does not match order");
        }
        String paymentIntent = string(object.get("payment_intent"));
        orderMapper.update(null, Wrappers.lambdaUpdate(HolidaysOrder.class)
            .set(HolidaysOrder::getStatus, 1)
            .set(HolidaysOrder::getPaidAmount, order.getTotalAmount())
            .eq(HolidaysOrder::getId, orderId)
            .in(HolidaysOrder::getStatus, 0, 4));
        paymentMapper.update(null, Wrappers.lambdaUpdate(HolidaysOrderPayment.class)
            .set(HolidaysOrderPayment::getStatus, PaymentStatus.PAID)
            .set(HolidaysOrderPayment::getStripeCheckoutSessionId, string(object.get("id")))
            .set(HolidaysOrderPayment::getStripePaymentIntentId, paymentIntent)
            .set(HolidaysOrderPayment::getPaidTime, LocalDateTime.now())
            .eq(HolidaysOrderPayment::getId, payment.getId()));
        if (order.getStatus() == 4) {
            try {
                refundService.createRefund(orderId, order.getTotalAmount(), "LATE_PAYMENT_AFTER_EXPIRATION");
            } catch (Exception ex) {
                log.error("Unable to automatically refund late Stripe payment, orderId={}", orderId, ex);
            }
        }
    }

    private void markFailed(Map<String, Object> object) {
        Long orderId = metadataOrderId(object);
        if (orderId == null) {
            String paymentIntent = string(object.get("payment_intent"));
            if (StringUtils.isBlank(paymentIntent)) {
                paymentIntent = string(object.get("id"));
            }
            HolidaysOrderPayment payment = paymentMapper.selectOne(Wrappers.lambdaQuery(HolidaysOrderPayment.class)
                .eq(HolidaysOrderPayment::getStripePaymentIntentId, paymentIntent));
            orderId = payment == null ? null : payment.getOrderId();
        }
        if (orderId == null) {
            return;
        }
        paymentMapper.update(null, Wrappers.lambdaUpdate(HolidaysOrderPayment.class)
            .set(HolidaysOrderPayment::getStatus, PaymentStatus.PAYMENT_FAILED)
            .set(HolidaysOrderPayment::getFailureCode, string(object.get("last_payment_error")))
            .eq(HolidaysOrderPayment::getOrderId, orderId)
            .notIn(HolidaysOrderPayment::getStatus, PaymentStatus.PAID, PaymentStatus.REFUNDED));
    }

    private void expire(Map<String, Object> object) {
        Long orderId = metadataOrderId(object);
        if (orderId != null) {
            paymentService.expireOrder(orderId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    private void updateRefund(Map<String, Object> object) {
        String refundId = string(object.get("id"));
        HolidaysOrderRefund refund = refundMapper.selectOne(Wrappers.lambdaQuery(HolidaysOrderRefund.class)
            .eq(HolidaysOrderRefund::getStripeRefundId, refundId));
        if (refund == null) {
            return;
        }
        refund.setStatus(normalizeRefundStatus(string(object.get("status"))));
        refundMapper.updateById(refund);
        if (RefundStatus.SUCCEEDED.equals(refund.getStatus())) {
            recalculateRefunds(refund.getOrderId());
        }
    }

    private void updateChargeRefund(Map<String, Object> object) {
        String paymentIntent = string(object.get("payment_intent"));
        HolidaysOrderPayment payment = paymentMapper.selectOne(Wrappers.lambdaQuery(HolidaysOrderPayment.class)
            .eq(HolidaysOrderPayment::getStripePaymentIntentId, paymentIntent));
        if (payment == null) {
            return;
        }
        long amountRefunded = number(object.get("amount_refunded"));
        HolidaysOrder order = orderMapper.selectById(payment.getOrderId());
        if (order == null) {
            return;
        }
        String currency = currencyResolver.resolve(order.getCurrency());
        int scale = java.util.Currency.getInstance(currency.toUpperCase()).getDefaultFractionDigits();
        BigDecimal refunded = BigDecimal.valueOf(amountRefunded).movePointLeft(scale);
        order.setRefundedAmount(refunded);
        orderMapper.updateById(order);
        updatePaymentRefundStatus(order, payment, refunded);
    }

    private void recalculateRefunds(Long orderId) {
        HolidaysOrder order = orderMapper.selectById(orderId);
        HolidaysOrderPayment payment = paymentForOrder(orderId);
        if (order == null || payment == null) {
            return;
        }
        List<HolidaysOrderRefund> refunds = refundMapper.selectList(Wrappers.lambdaQuery(HolidaysOrderRefund.class)
            .eq(HolidaysOrderRefund::getOrderId, orderId)
            .eq(HolidaysOrderRefund::getStatus, RefundStatus.SUCCEEDED));
        BigDecimal total = refunds.stream().map(HolidaysOrderRefund::getAmount)
            .filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setRefundedAmount(total);
        orderMapper.updateById(order);
        updatePaymentRefundStatus(order, payment, total);
    }

    private void updatePaymentRefundStatus(HolidaysOrder order, HolidaysOrderPayment payment, BigDecimal refunded) {
        BigDecimal paid = order.getPaidAmount() == null ? BigDecimal.ZERO : order.getPaidAmount();
        String status = refunded.compareTo(paid) >= 0 ? PaymentStatus.REFUNDED : PaymentStatus.PARTIALLY_REFUNDED;
        paymentMapper.update(null, Wrappers.lambdaUpdate(HolidaysOrderPayment.class)
            .set(HolidaysOrderPayment::getStatus, status)
            .eq(HolidaysOrderPayment::getId, payment.getId()));
        if (PaymentStatus.REFUNDED.equals(status)) {
            orderMapper.update(null, Wrappers.lambdaUpdate(HolidaysOrder.class)
                .set(HolidaysOrder::getStatus, 5)
                .eq(HolidaysOrder::getId, order.getId())
                .in(HolidaysOrder::getStatus, 1, 2, 3));
        }
    }

    private HolidaysOrderPayment paymentForOrder(Long orderId) {
        return paymentMapper.selectOne(Wrappers.lambdaQuery(HolidaysOrderPayment.class)
            .eq(HolidaysOrderPayment::getOrderId, orderId)
            .orderByDesc(HolidaysOrderPayment::getId)
            .last("limit 1"));
    }

    private Long metadataOrderId(Map<String, Object> object) {
        Map<String, Object> metadata = asMap(object.get("metadata"));
        String value = string(metadata.get("orderId"));
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    private String string(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private long number(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return value == null ? 0 : Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0;
        }
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
