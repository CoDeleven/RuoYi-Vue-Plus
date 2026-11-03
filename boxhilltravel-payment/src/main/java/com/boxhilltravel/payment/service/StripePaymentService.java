package com.boxhilltravel.payment.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.boxhilltravel.core.domain.HolidaysDeparture;
import com.boxhilltravel.core.domain.HolidaysOrder;
import com.boxhilltravel.core.domain.HolidaysOrderPayment;
import com.boxhilltravel.core.mapper.HolidaysDepartureMapper;
import com.boxhilltravel.core.mapper.HolidaysOrderMapper;
import com.boxhilltravel.core.mapper.HolidaysOrderPaymentMapper;
import com.boxhilltravel.payment.config.StripeProperties;
import com.boxhilltravel.payment.constant.PaymentStatus;
import com.boxhilltravel.payment.domain.vo.PaymentCheckoutVo;
import com.boxhilltravel.payment.domain.vo.PaymentStatusVo;
import com.boxhilltravel.payment.util.MoneyMinorUnitConverter;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.net.RequestOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

/**
 * Stripe Checkout and payment state service.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class StripePaymentService {

    private static final Integer ORDER_STATUS_PENDING_PAYMENT = 0;
    private static final Integer ORDER_STATUS_PAID = 1;
    private static final Integer ORDER_STATUS_CANCELLED = 4;

    private final StripeProperties properties;
    private final ObjectProvider<StripeClient> stripeClientProvider;
    private final StripeCurrencyResolver currencyResolver;
    private final HolidaysOrderMapper orderMapper;
    private final HolidaysOrderPaymentMapper paymentMapper;
    private final HolidaysDepartureMapper departureMapper;
    private final Clock stripeClock;

    /**
     * Creates or reuses the Checkout Session for an order.
     *
     * @param orderId order ID
     * @param userId  current user ID
     * @return Checkout Session information
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentCheckoutVo createCheckoutSession(Long orderId, Long userId) {
        HolidaysOrder order = orderMapper.selectByIdForUpdate(orderId);
        if (order == null || !userId.equals(order.getCustomerUserId())) {
            throw new ServiceException("Order not found");
        }
        validatePayable(order);
        HolidaysOrderPayment payment = getPayment(orderId);
        LocalDateTime now = LocalDateTime.now(stripeClock);
        if (order.getPaymentDeadline() == null && payment.getCheckoutExpiresAt() != null) {
            order.setPaymentDeadline(payment.getCheckoutExpiresAt());
            orderMapper.updateById(order);
            validatePayable(order);
        }
        if (payment.getCheckoutExpiresAt() != null && payment.getCheckoutExpiresAt().isAfter(now)
            && StringUtils.isNotBlank(payment.getStripeCheckoutSessionId())
            && (PaymentStatus.CHECKOUT_CREATED.equals(payment.getStatus())
            || PaymentStatus.PROCESSING.equals(payment.getStatus()))) {
            return toCheckoutVo(order, payment);
        }

        StripeClient client = requireClient();
        String currency = currencyResolver.resolve(order.getCurrency());
        validateCurrency(currency);
        long totalMinor = MoneyMinorUnitConverter.toMinor(order.getTotalAmount(), currency);
        if (totalMinor <= 0) {
            throw new ServiceException("Payment amount must be greater than zero");
        }

        LocalDateTime expiresAt = order.getPaymentDeadline() != null
            ? order.getPaymentDeadline()
            : (order.getCreateTime() == null ? now : order.getCreateTime()).plusMinutes(properties.getPaymentExpireMinutes());
        if (!expiresAt.isAfter(now)) {
            throw new ServiceException("Payment deadline has expired");
        }
        if (order.getPaymentDeadline() == null) {
            order.setPaymentDeadline(expiresAt);
            orderMapper.updateById(order);
        }

        SessionCreateParams.Builder builder = SessionCreateParams.builder()
            .setUiMode(SessionCreateParams.UiMode.HOSTED_PAGE)
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.AUTO)
            .setPhoneNumberCollection(SessionCreateParams.PhoneNumberCollection.builder()
                .setEnabled(true)
                .build())
            .setAutomaticTax(SessionCreateParams.AutomaticTax.builder()
                .setEnabled(false)
                .build())
            .setAllowPromotionCodes(false)
            .setSubmitType(SessionCreateParams.SubmitType.AUTO)
            .setIntegrationIdentifier("hosted_web_0002")
            .setOriginContext(SessionCreateParams.OriginContext.WEB)
            .setSuccessUrl(buildRedirectUrl(properties.getSuccessUrl(), order, false))
            .setCancelUrl(buildRedirectUrl(properties.getCancelUrl(), order, true))
            .setExpiresAt(checkoutExpiration(expiresAt))
            .putMetadata("orderId", String.valueOf(orderId))
            .putMetadata("paymentId", String.valueOf(payment.getId()))
            .setPaymentIntentData(SessionCreateParams.PaymentIntentData.builder()
                .putMetadata("orderId", String.valueOf(orderId)).build());

        addLineItem(builder, order.getTourName(), order.getTourAmount(), currency);
        if (order.getExtrasAmount() != null && order.getExtrasAmount().signum() > 0) {
            addLineItem(builder, "Travel extras", order.getExtrasAmount(), currency);
        }

        try {
            Session session = client.v1().checkout().sessions().create(builder.build(),
                RequestOptions.builder().setIdempotencyKey("checkout:" + payment.getId()).build());
            if (StringUtils.isBlank(session.getUrl())) {
                throw new ServiceException("Stripe did not return a Checkout URL");
            }
            payment.setPaymentMethod("STRIPE_CHECKOUT");
            payment.setStatus(PaymentStatus.CHECKOUT_CREATED);
            payment.setStripeCheckoutSessionId(session.getId());
            payment.setStripePaymentIntentId(session.getPaymentIntent());
            payment.setAmountMinor(totalMinor);
            payment.setCheckoutExpiresAt(session.getExpiresAt() == null ? expiresAt
                : LocalDateTime.ofInstant(Instant.ofEpochSecond(session.getExpiresAt()), stripeClock.getZone()));
            payment.setFailureCode(null);
            payment.setCheckoutUrl(session.getUrl());
            paymentMapper.updateById(payment);
            return toCheckoutVo(order, payment);
        } catch (StripeException ex) {
            log.error("Stripe Checkout Session creation failed, orderNo={}", order.getOrderNo(), ex);
            throw new ServiceException("Unable to create Stripe Checkout Session");
        }
    }

    /**
     * Queries payment state for a customer order.
     */
    public PaymentStatusVo queryPaymentStatus(Long orderId, Long userId) {
        HolidaysOrder order = getUserOrder(orderId, userId);
        HolidaysOrderPayment payment = getPayment(orderId);
        PaymentStatusVo vo = new PaymentStatusVo();
        vo.setOrderId(order.getId());
        vo.setOrderStatus(order.getStatus());
        vo.setPaymentStatus(payment.getStatus());
        vo.setPaidAmount(order.getPaidAmount());
        vo.setRefundedAmount(order.getRefundedAmount() == null ? BigDecimal.ZERO : order.getRefundedAmount());
        vo.setCurrency(currencyResolver.resolve(order.getCurrency()));
        vo.setPaymentDeadline(order.getPaymentDeadline());
        return vo;
    }

    /**
     * Expires one pending order and releases its reserved seats.
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean expireOrder(Long orderId) {
        HolidaysOrder order = orderMapper.selectByIdForUpdate(orderId);
        if (order == null || !ORDER_STATUS_PENDING_PAYMENT.equals(order.getStatus())) return false;
        if (order.getPaymentDeadline() == null && order.getCreateTime() != null) {
            order.setPaymentDeadline(order.getCreateTime().plusMinutes(properties.getPaymentExpireMinutes()));
            orderMapper.updateById(order);
        }
        if (order.getPaymentDeadline() == null || order.getPaymentDeadline().isAfter(LocalDateTime.now(stripeClock))) {
            return false;
        }
        int updated = orderMapper.update(null, Wrappers.lambdaUpdate(HolidaysOrder.class)
            .set(HolidaysOrder::getStatus, ORDER_STATUS_CANCELLED)
            .eq(HolidaysOrder::getId, orderId)
            .eq(HolidaysOrder::getStatus, ORDER_STATUS_PENDING_PAYMENT));
        if (updated == 0) {
            return false;
        }
        paymentMapper.update(null, Wrappers.lambdaUpdate(HolidaysOrderPayment.class)
            .set(HolidaysOrderPayment::getStatus, PaymentStatus.EXPIRED)
            .eq(HolidaysOrderPayment::getOrderId, orderId)
            .in(HolidaysOrderPayment::getStatus, PaymentStatus.PENDING, PaymentStatus.CHECKOUT_CREATED,
                PaymentStatus.PROCESSING));
        releaseSeats(order);
        return true;
    }

    /** Returns expired local sessions whose remote closure still needs to be confirmed. */
    public List<Long> findSessionsToExpire() {
        return paymentMapper.selectList(Wrappers.lambdaQuery(HolidaysOrderPayment.class)
                .eq(HolidaysOrderPayment::getStatus, PaymentStatus.EXPIRED)
                .isNotNull(HolidaysOrderPayment::getStripeCheckoutSessionId)
                .isNotNull(HolidaysOrderPayment::getCheckoutUrl)
                .orderByAsc(HolidaysOrderPayment::getUpdateTime).last("limit 50"))
            .stream().map(HolidaysOrderPayment::getId).toList();
    }

    /**
     * Closes an expired reservation's Stripe session. A failed request leaves the URL in place
     * so the recovery job can retry; expired sessions are never offered by the pay endpoint.
     */
    public void closeExpiredSession(Long paymentId) {
        HolidaysOrderPayment payment = paymentMapper.selectById(paymentId);
        if (payment == null || !PaymentStatus.EXPIRED.equals(payment.getStatus())
            || StringUtils.isBlank(payment.getCheckoutUrl()) || StringUtils.isBlank(payment.getStripeCheckoutSessionId())) {
            return;
        }
        try {
            StripeClient client = requireClient();
            Session session = client.v1().checkout().sessions().retrieve(payment.getStripeCheckoutSessionId());
            if ("open".equals(session.getStatus())) {
                session = client.v1().checkout().sessions().expire(payment.getStripeCheckoutSessionId());
            }
            if ("expired".equals(session.getStatus()) || "complete".equals(session.getStatus())) {
                paymentMapper.update(null, Wrappers.lambdaUpdate(HolidaysOrderPayment.class)
                    .set(HolidaysOrderPayment::getCheckoutUrl, null)
                    .eq(HolidaysOrderPayment::getId, paymentId)
                    .eq(HolidaysOrderPayment::getStripeCheckoutSessionId, payment.getStripeCheckoutSessionId()));
            }
        } catch (StripeException ex) {
            paymentMapper.update(null, Wrappers.lambdaUpdate(HolidaysOrderPayment.class)
                .set(HolidaysOrderPayment::getUpdateTime, LocalDateTime.now(stripeClock))
                .eq(HolidaysOrderPayment::getId, paymentId));
            log.warn("Stripe session closure will be retried, sessionId={}", payment.getStripeCheckoutSessionId());
        }
    }

    private long checkoutExpiration(LocalDateTime deadline) {
        long now = stripeClock.instant().getEpochSecond();
        long localDeadline = deadline.atZone(stripeClock.getZone()).toEpochSecond();
        // Stripe requires at least 30 minutes from session creation. Leave a minute for transit;
        // the local deadline remains authoritative and the recovery job closes the session then.
        return Math.min(now + 24 * 60 * 60, Math.max(localDeadline, now + 31 * 60));
    }

    private void addLineItem(SessionCreateParams.Builder builder, String name, BigDecimal amount, String currency) {
        if (amount == null || amount.signum() <= 0) {
            return;
        }
        long minor = MoneyMinorUnitConverter.toMinor(amount, currency);
        builder.addLineItem(SessionCreateParams.LineItem.builder()
            .setQuantity(1L)
            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency(currency)
                .setUnitAmount(minor)
                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                    .setName(StringUtils.defaultIfBlank(name, "Travel booking"))
                    .build())
                .build())
            .build());
    }

    private HolidaysOrder getUserOrder(Long orderId, Long userId) {
        HolidaysOrder order = orderMapper.selectOne(Wrappers.lambdaQuery(HolidaysOrder.class)
            .eq(HolidaysOrder::getId, orderId)
            .eq(HolidaysOrder::getCustomerUserId, userId));
        if (order == null) {
            throw new ServiceException("Order not found");
        }
        return order;
    }

    private HolidaysOrderPayment getPayment(Long orderId) {
        HolidaysOrderPayment payment = paymentMapper.selectOne(Wrappers.lambdaQuery(HolidaysOrderPayment.class)
            .eq(HolidaysOrderPayment::getOrderId, orderId)
            .orderByDesc(HolidaysOrderPayment::getId)
            .last("limit 1"));
        if (payment == null) {
            throw new ServiceException("Payment record not found");
        }
        return payment;
    }

    private void validatePayable(HolidaysOrder order) {
        if (!ORDER_STATUS_PENDING_PAYMENT.equals(order.getStatus())) {
            if (ORDER_STATUS_PAID.equals(order.getStatus())) {
                throw new ServiceException("Order has already been paid");
            }
            throw new ServiceException("Only pending payment orders can be paid");
        }
        if (order.getPaymentDeadline() != null && !order.getPaymentDeadline().isAfter(LocalDateTime.now(stripeClock))) {
            throw new ServiceException("Payment deadline has expired");
        }
    }

    private void validateCurrency(String currency) {
        if (properties.getSupportedCurrencies() == null || properties.getSupportedCurrencies().isEmpty()) {
            return;
        }
        boolean allowed = properties.getSupportedCurrencies().stream()
            .map(item -> item.toLowerCase(Locale.ROOT))
            .anyMatch(currency::equals);
        if (!allowed) {
            throw new ServiceException("Payment currency is not enabled: " + currency);
        }
    }

    private StripeClient requireClient() {
        if (!properties.isEnabled()) {
            throw new ServiceException("Stripe payment is not enabled");
        }
        StripeClient client = stripeClientProvider.getIfAvailable();
        if (client == null) {
            throw new ServiceException("Stripe payment is not configured");
        }
        return client;
    }

    private String buildRedirectUrl(String baseUrl, HolidaysOrder order, boolean cancelled) {
        if (StringUtils.isBlank(baseUrl)) {
            throw new ServiceException("Stripe redirect URL is not configured");
        }
        String separator = baseUrl.contains("?") ? "&" : "?";
        return baseUrl + separator + "orderId=" + order.getId() + "&session_id={CHECKOUT_SESSION_ID}"
            + (cancelled ? "&cancelled=1" : "");
    }

    private PaymentCheckoutVo toCheckoutVo(HolidaysOrder order, HolidaysOrderPayment payment) {
        PaymentCheckoutVo vo = new PaymentCheckoutVo();
        vo.setOrderId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setSessionId(payment.getStripeCheckoutSessionId());
        vo.setCheckoutUrl(payment.getCheckoutUrl());
        vo.setCurrency(currencyResolver.resolve(order.getCurrency()));
        vo.setAmount(order.getTotalAmount());
        vo.setPaymentStatus(payment.getStatus());
        vo.setExpiresAt(order.getPaymentDeadline() != null ? order.getPaymentDeadline() : payment.getCheckoutExpiresAt());
        return vo;
    }

    private void releaseSeats(HolidaysOrder order) {
        if (order.getDepartureId() == null || order.getTravelerCount() == null || order.getTravelerCount() <= 0) {
            return;
        }
        departureMapper.update(null, Wrappers.lambdaUpdate(HolidaysDeparture.class)
            .setSql("booked_count = GREATEST(COALESCE(booked_count, 0) - {0}, 0)", order.getTravelerCount())
            .setSql("available_count = COALESCE(available_count, 0) + {0}", order.getTravelerCount())
            .set(HolidaysDeparture::getStatus, 1)
            .eq(HolidaysDeparture::getId, order.getDepartureId())
            .in(HolidaysDeparture::getStatus, 1, 2));
    }

}
