package com.boxhilltravel.payment.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.boxhilltravel.core.domain.HolidaysOrder;
import com.boxhilltravel.core.domain.HolidaysOrderPayment;
import com.boxhilltravel.core.domain.StripeWebhookEvent;
import com.boxhilltravel.core.mapper.HolidaysOrderMapper;
import com.boxhilltravel.core.mapper.HolidaysOrderPaymentMapper;
import com.boxhilltravel.core.mapper.StripeWebhookEventMapper;
import com.boxhilltravel.payment.config.StripeProperties;
import com.boxhilltravel.payment.constant.PaymentStatus;
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
import org.springframework.transaction.annotation.Isolation;

import java.math.BigDecimal;
import java.time.Clock;
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
    private final Clock stripeClock;

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
        record.setReceivedAt(LocalDateTime.now(stripeClock));
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
        processAcceptedEvent(recordId);
    }

    /** Runs the inbox transaction before dispatching any remote refund request. */
    public void processAcceptedEvent(Long recordId) {
        Long refundId;
        try {
            refundId = selfProvider.getObject().processEvent(recordId);
        } catch (Exception ex) {
            log.error("Stripe webhook processing failed, recordId={}", recordId, ex);
            eventMapper.update(null, Wrappers.lambdaUpdate(StripeWebhookEvent.class)
                .set(StripeWebhookEvent::getProcessStatus, WebhookStatus.FAILED)
                .set(StripeWebhookEvent::getProcessedAt, LocalDateTime.now(stripeClock))
                .set(StripeWebhookEvent::getErrorMessage, StringUtils.substring(ex.getMessage(), 0, 1000))
                .eq(StripeWebhookEvent::getId, recordId)
                .ne(StripeWebhookEvent::getProcessStatus, WebhookStatus.SUCCEEDED));
            return;
        }
        if (refundId != null) {
            try {
                refundService.submitRefund(refundId);
            } catch (Exception ex) {
                log.error("Persisted Stripe refund will be retried, refundId={}", refundId, ex);
            }
        }
    }

    /**
     * Processes domain changes and the inbox acknowledgement atomically. The public method
     * is invoked through the Spring proxy; private transactional handlers do not start transactions.
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Long processEvent(Long recordId) {
        StripeWebhookEvent record = eventMapper.selectOne(Wrappers.lambdaQuery(StripeWebhookEvent.class)
            .eq(StripeWebhookEvent::getId, recordId).last("FOR UPDATE"));
        if (record == null || WebhookStatus.SUCCEEDED.equals(record.getProcessStatus())) return null;
        Map<String, Object> root = JsonUtils.parseObject(record.getPayload(), Map.class);
        Map<String, Object> data = asMap(root.get("data"));
        Map<String, Object> object = asMap(data.get("object"));
        Long refundId = null;
        switch (record.getEventType()) {
            case "checkout.session.completed", "checkout.session.async_payment_succeeded" ->
                refundId = markPaid(object);
            case "checkout.session.async_payment_failed", "payment_intent.payment_failed" ->
                markFailed(object);
            case "checkout.session.expired" -> expire(object);
            case "refund.updated" -> updateRefund(object);
            case "charge.refunded" -> updateChargeRefund(object);
            default -> log.debug("Ignoring unsupported Stripe event type={}", record.getEventType());
        }
        eventMapper.update(null, Wrappers.lambdaUpdate(StripeWebhookEvent.class)
            .set(StripeWebhookEvent::getProcessStatus, WebhookStatus.SUCCEEDED)
            .set(StripeWebhookEvent::getProcessedAt, LocalDateTime.now(stripeClock))
            .set(StripeWebhookEvent::getErrorMessage, null).eq(StripeWebhookEvent::getId, recordId));
        return refundId;
    }

    /** Returns failed or unfinished inbox entries, including deliveries accepted before a restart. */
    public List<Long> findEventsToRetry() {
        return eventMapper.selectList(Wrappers.lambdaQuery(StripeWebhookEvent.class)
                .in(StripeWebhookEvent::getProcessStatus, WebhookStatus.PENDING, WebhookStatus.FAILED, WebhookStatus.PROCESSING)
                .orderByAsc(StripeWebhookEvent::getProcessedAt).last("limit 50"))
            .stream().map(StripeWebhookEvent::getId).toList();
    }

    private Long markPaid(Map<String, Object> object) {
        Long orderId = metadataOrderId(object);
        HolidaysOrderPayment payment = paymentForWebhookObject(object);
        if (orderId == null && payment != null) {
            orderId = payment.getOrderId();
        }
        if (orderId == null) {
            throw new ServiceException("Stripe Checkout Session is not linked to a local order");
        }
        HolidaysOrder order = orderMapper.selectByIdForUpdate(orderId);
        if (payment == null) payment = paymentForOrder(orderId);
        else payment = paymentMapper.selectById(payment.getId());
        if (order == null || payment == null) {
            throw new ServiceException("Stripe payment record is not linked to a local order");
        }
        if (!orderId.equals(payment.getOrderId())) {
            throw new ServiceException("Stripe payment is linked to a different order");
        }
        if (!"paid".equals(string(object.get("payment_status")))) {
            // Checkout can complete before an asynchronous payment method collects funds.
            if (Integer.valueOf(0).equals(order.getStatus()) && payment.getPaidTime() == null) {
                paymentMapper.update(null, Wrappers.lambdaUpdate(HolidaysOrderPayment.class)
                    .set(HolidaysOrderPayment::getStatus, PaymentStatus.PROCESSING)
                    .eq(HolidaysOrderPayment::getId, payment.getId())
                    .in(HolidaysOrderPayment::getStatus, PaymentStatus.PENDING, PaymentStatus.CHECKOUT_CREATED));
            }
            return null;
        }
        String currency = MoneyMinorUnitConverter.normalizeCurrency(String.valueOf(object.get("currency")));
        String orderCurrency = currencyResolver.resolve(order.getCurrency());
        long amountMinor = number(object.get("amount_total"));
        long expectedMinor = MoneyMinorUnitConverter.toMinor(order.getTotalAmount(), orderCurrency);
        if (amountMinor != expectedMinor || !currency.equalsIgnoreCase(orderCurrency)) {
            throw new ServiceException("Stripe payment amount or currency does not match order");
        }
        String paymentIntent = string(object.get("payment_intent"));
        if (StringUtils.isBlank(paymentIntent)) throw new ServiceException("Stripe payment intent is missing");
        if (Integer.valueOf(5).equals(order.getStatus())) return null;
        if (order.getPaidAmount() != null && order.getPaidAmount().signum() > 0) {
            // Duplicate or out-of-order success events must not reset refund/payment state.
            return Integer.valueOf(4).equals(order.getStatus()) ? refundService.enqueueLatePaymentRefund(orderId) : null;
        }
        if (!Integer.valueOf(0).equals(order.getStatus()) && !Integer.valueOf(4).equals(order.getStatus())) return null;
        if (order.getPaymentDeadline() == null && order.getCreateTime() != null) {
            order.setPaymentDeadline(order.getCreateTime().plusMinutes(properties.getPaymentExpireMinutes()));
            orderMapper.updateById(order);
        }
        boolean late = Integer.valueOf(4).equals(order.getStatus())
            || (order.getPaymentDeadline() != null && !order.getPaymentDeadline().isAfter(LocalDateTime.now(stripeClock)));
        if (late && Integer.valueOf(0).equals(order.getStatus())) {
            paymentService.expireOrder(orderId);
        }
        orderMapper.update(null, Wrappers.lambdaUpdate(HolidaysOrder.class)
            .set(!late, HolidaysOrder::getStatus, 1)
            .set(HolidaysOrder::getPaidAmount, order.getTotalAmount())
            .eq(HolidaysOrder::getId, orderId)
            .in(HolidaysOrder::getStatus, 0, 4));
        paymentMapper.update(null, Wrappers.lambdaUpdate(HolidaysOrderPayment.class)
            .set(HolidaysOrderPayment::getStatus, late ? PaymentStatus.EXPIRED : PaymentStatus.PAID)
            .set(HolidaysOrderPayment::getStripeCheckoutSessionId, string(object.get("id")))
            .set(HolidaysOrderPayment::getStripePaymentIntentId, paymentIntent)
            .set(HolidaysOrderPayment::getPaidTime, LocalDateTime.now(stripeClock))
            .eq(HolidaysOrderPayment::getId, payment.getId()));
        return late ? refundService.enqueueLatePaymentRefund(orderId) : null;
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
            .notIn(HolidaysOrderPayment::getStatus, PaymentStatus.PAID, PaymentStatus.REFUNDED,
                PaymentStatus.PARTIALLY_REFUNDED, PaymentStatus.EXPIRED));
    }

    private void expire(Map<String, Object> object) {
        Long orderId = metadataOrderId(object);
        if (orderId == null) {
            HolidaysOrderPayment payment = paymentForWebhookObject(object);
            orderId = payment == null ? null : payment.getOrderId();
        }
        if (orderId != null) {
            paymentService.expireOrder(orderId);
        }
    }

    private void updateRefund(Map<String, Object> object) {
        refundService.updateRefund(string(object.get("id")), string(object.get("status")));
    }

    private void updateChargeRefund(Map<String, Object> object) {
        String paymentIntent = string(object.get("payment_intent"));
        HolidaysOrderPayment payment = paymentMapper.selectOne(Wrappers.lambdaQuery(HolidaysOrderPayment.class)
            .eq(HolidaysOrderPayment::getStripePaymentIntentId, paymentIntent));
        if (payment == null) {
            return;
        }
        long amountRefunded = number(object.get("amount_refunded"));
        HolidaysOrder order = orderMapper.selectByIdForUpdate(payment.getOrderId());
        if (order == null) {
            return;
        }
        String currency = currencyResolver.resolve(order.getCurrency());
        int scale = java.util.Currency.getInstance(currency.toUpperCase()).getDefaultFractionDigits();
        BigDecimal refunded = BigDecimal.valueOf(amountRefunded).movePointLeft(scale);
        if (refunded.signum() <= 0) return;
        if (order.getRefundedAmount() != null) refunded = refunded.max(order.getRefundedAmount());
        order.setRefundedAmount(refunded);
        orderMapper.updateById(order);
        updatePaymentRefundStatus(order, payment, refunded);
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
                .in(HolidaysOrder::getStatus, 1, 2, 3, 4));
        }
    }

    private HolidaysOrderPayment paymentForOrder(Long orderId) {
        return paymentMapper.selectOne(Wrappers.lambdaQuery(HolidaysOrderPayment.class)
            .eq(HolidaysOrderPayment::getOrderId, orderId)
            .orderByDesc(HolidaysOrderPayment::getId)
            .last("limit 1"));
    }

    private HolidaysOrderPayment paymentForWebhookObject(Map<String, Object> object) {
        String sessionId = string(object.get("id"));
        if (StringUtils.isNotBlank(sessionId)) {
            HolidaysOrderPayment payment = paymentMapper.selectOne(Wrappers.lambdaQuery(HolidaysOrderPayment.class)
                .eq(HolidaysOrderPayment::getStripeCheckoutSessionId, sessionId)
                .orderByDesc(HolidaysOrderPayment::getId)
                .last("limit 1"));
            if (payment != null) return payment;
        }
        String paymentIntent = string(object.get("payment_intent"));
        if (StringUtils.isBlank(paymentIntent)) {
            paymentIntent = string(object.get("id"));
        }
        return paymentMapper.selectOne(Wrappers.lambdaQuery(HolidaysOrderPayment.class)
            .eq(HolidaysOrderPayment::getStripePaymentIntentId, paymentIntent)
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

}
