package com.boxhilltravel.payment;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.boxhilltravel.core.domain.HolidaysOrder;
import com.boxhilltravel.core.domain.HolidaysOrderPayment;
import com.boxhilltravel.core.mapper.HolidaysOrderMapper;
import com.boxhilltravel.core.mapper.HolidaysOrderPaymentMapper;
import com.boxhilltravel.payment.config.StripeProperties;
import com.boxhilltravel.payment.service.StripeCurrencyResolver;
import com.boxhilltravel.payment.service.StripePaymentService;
import com.boxhilltravel.payment.service.StripeRefundService;
import com.boxhilltravel.payment.service.StripeWebhookService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Regression tests for payments received after the reservation deadline. */
@Tag("dev")
class StripeExpirationRegressionTest {

    private HolidaysOrder order;
    private StripeWebhookService service;
    private final List<LambdaUpdateWrapper<HolidaysOrder>> updates = new ArrayList<>();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, "test"), HolidaysOrder.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, "test"), HolidaysOrderPayment.class);
        HolidaysOrderMapper orders = mock(HolidaysOrderMapper.class);
        HolidaysOrderPaymentMapper payments = mock(HolidaysOrderPaymentMapper.class);
        StripeCurrencyResolver currencies = mock(StripeCurrencyResolver.class);
        order = new HolidaysOrder();
        order.setId(101L);
        order.setStatus(0);
        order.setCurrency("usd");
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setPaidAmount(BigDecimal.ZERO);
        order.setPaymentDeadline(LocalDateTime.now().minusMinutes(1));
        HolidaysOrderPayment payment = new HolidaysOrderPayment();
        payment.setId(201L);
        payment.setOrderId(order.getId());
        payment.setStripeCheckoutSessionId("cs_test_expiration");
        when(orders.selectById(order.getId())).thenReturn(order);
        when(orders.selectByIdForUpdate(order.getId())).thenReturn(order);
        when(payments.selectOne(any(Wrapper.class))).thenReturn(payment);
        when(payments.selectById(payment.getId())).thenReturn(payment);
        when(currencies.resolve("usd")).thenReturn("usd");
        when(orders.update(isNull(), any(Wrapper.class))).thenAnswer(invocation -> {
            updates.add(invocation.getArgument(1));
            return 1;
        });
        service = new StripeWebhookService(new StripeProperties(), null, mock(StripePaymentService.class),
            currencies, mock(StripeRefundService.class), null, orders, payments, Clock.systemDefaultZone());
    }

    @Test
    void expiredOrderMustNotBecomeFulfillableBeforeTheCancellationScan() {
        ReflectionTestUtils.invokeMethod(service, "markPaid", checkout("paid"));
        assertFalse(changesBusinessStatusToPaid(), "An expired reservation must not become a paid business order");
    }

    @Test
    void canceledOrderMustNotBeReopenedWhileItsRefundIsPending() {
        order.setStatus(4);
        ReflectionTestUtils.invokeMethod(service, "markPaid", checkout("paid"));
        assertFalse(changesBusinessStatusToPaid(), "Refund processing must preserve the canceled business state");
    }

    @Test
    void completedCheckoutWithUnpaidStatusMustNotMarkTheOrderPaid() {
        order.setPaymentDeadline(LocalDateTime.now().plusMinutes(5));
        ReflectionTestUtils.invokeMethod(service, "markPaid", checkout("unpaid"));
        assertFalse(changesBusinessStatusToPaid(), "Delayed payment methods must wait for actual payment success");
    }

    private Map<String, Object> checkout(String paymentStatus) {
        return Map.of("id", "cs_test_expiration", "currency", "usd", "amount_total", 10000L,
            "payment_intent", "pi_test_expiration", "payment_status", paymentStatus);
    }

    private boolean changesBusinessStatusToPaid() {
        return updates.stream().anyMatch(update -> {
            update.getSqlSet();
            return update.getParamNameValuePairs().containsValue(Integer.valueOf(1));
        });
    }
}
