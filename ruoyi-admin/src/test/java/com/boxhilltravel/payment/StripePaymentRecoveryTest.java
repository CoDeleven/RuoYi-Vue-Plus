package com.boxhilltravel.payment;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import com.boxhilltravel.core.domain.HolidaysDeparture;
import com.boxhilltravel.core.domain.HolidaysOrder;
import com.boxhilltravel.core.domain.HolidaysOrderPayment;
import com.boxhilltravel.core.domain.HolidaysOrderRefund;
import com.boxhilltravel.core.domain.StripeWebhookEvent;
import com.boxhilltravel.core.mapper.HolidaysDepartureMapper;
import com.boxhilltravel.core.mapper.HolidaysOrderMapper;
import com.boxhilltravel.core.mapper.HolidaysOrderPaymentMapper;
import com.boxhilltravel.core.mapper.HolidaysOrderRefundMapper;
import com.boxhilltravel.core.mapper.StripeWebhookEventMapper;
import com.boxhilltravel.payment.config.StripeProperties;
import com.boxhilltravel.payment.constant.PaymentStatus;
import com.boxhilltravel.payment.constant.RefundStatus;
import com.boxhilltravel.payment.constant.WebhookStatus;
import com.boxhilltravel.payment.domain.vo.PaymentCheckoutVo;
import com.boxhilltravel.payment.service.PaymentRecoveryScheduler;
import com.boxhilltravel.payment.service.PaymentReservationScheduler;
import com.boxhilltravel.payment.service.StripeCurrencyResolver;
import com.boxhilltravel.payment.service.StripePaymentService;
import com.boxhilltravel.payment.service.StripeRefundService;
import com.boxhilltravel.payment.service.StripeWebhookService;
import com.stripe.StripeClient;
import com.stripe.exception.ApiConnectionException;
import com.stripe.model.Refund;
import com.stripe.model.StripeCollection;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.RefundListParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mybatis.spring.annotation.MapperScan;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tools.jackson.databind.json.JsonMapper;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Exercises real mapper SQL, row locks, Spring transactions, and recovery against an isolated H2 database. */
@Tag("dev")
class StripePaymentRecoveryTest {

    private static final Instant NOW = Instant.parse("2026-11-03T04:00:00Z");
    private static final LocalDateTime LOCAL_NOW = LocalDateTime.ofInstant(NOW, ZoneOffset.UTC);
    private static final String SESSION_ID = "cs_test_expiration";
    private static final String INTENT_ID = "pi_test_expiration";
    private static final Class<?>[] ENTITIES = { HolidaysOrder.class, HolidaysOrderPayment.class,
        HolidaysOrderRefund.class, HolidaysDeparture.class, StripeWebhookEvent.class };
    private static AnnotationConfigApplicationContext context;
    private JdbcTemplate db;
    private HolidaysOrderMapper orders;
    private HolidaysOrderPaymentMapper payments;
    private HolidaysOrderRefundMapper refunds;
    private StripeWebhookEventMapper events;
    private StripePaymentService paymentService;
    private StripeRefundService refundService;
    private StripeWebhookService webhookService;
    private StripeClient stripe;
    private Clock clock;
    private final List<Refund> remoteRefunds = new CopyOnWriteArrayList<>();
    private final AtomicBoolean failBeforeCreation = new AtomicBoolean();
    private final AtomicBoolean loseCreationResponse = new AtomicBoolean();
    private String remoteRefundStatus = "succeeded";

    @BeforeAll
    static void startContext() {
        context = new AnnotationConfigApplicationContext(Config.class);
        JdbcTemplate db = new JdbcTemplate(context.getBean(DataSource.class));
        for (Class<?> entity : ENTITIES) {
            TableInfo table = TableInfoHelper.getTableInfo(entity);
            List<String> columns = new ArrayList<>();
            columns.add(table.getKeyColumn() + " BIGINT AUTO_INCREMENT PRIMARY KEY");
            table.getFieldList().forEach(field -> columns.add(field.getColumn() + " " + sqlType(field.getPropertyType())));
            db.execute("CREATE TABLE " + table.getTableName() + " (" + String.join(",", columns) + ")");
        }
        db.execute("CREATE UNIQUE INDEX uk_test_event_id ON stripe_webhook_event(event_id)");
        db.execute("CREATE UNIQUE INDEX uk_test_refund_id ON holidays_order_refund(stripe_refund_id)");
    }

    @AfterAll
    static void closeContext() {
        if (context != null) context.close();
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        db = new JdbcTemplate(context.getBean(DataSource.class));
        for (Class<?> entity : ENTITIES) db.execute("TRUNCATE TABLE " + TableInfoHelper.getTableInfo(entity).getTableName());
        orders = context.getBean(HolidaysOrderMapper.class);
        payments = context.getBean(HolidaysOrderPaymentMapper.class);
        refunds = context.getBean(HolidaysOrderRefundMapper.class);
        events = context.getBean(StripeWebhookEventMapper.class);
        paymentService = context.getBean(StripePaymentService.class);
        refundService = context.getBean(StripeRefundService.class);
        webhookService = context.getBean(StripeWebhookService.class);
        stripe = context.getBean(StripeClient.class);
        clock = context.getBean(Clock.class);
        reset(stripe);
        when(clock.instant()).thenReturn(NOW);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        StripeCollection<Refund> collection = mock(StripeCollection.class);
        when(collection.autoPagingIterable()).thenAnswer(ignored -> List.copyOf(remoteRefunds));
        when(stripe.v1().refunds().list(any(RefundListParams.class))).thenReturn(collection);
        when(stripe.v1().refunds().create(any(RefundCreateParams.class), any(RequestOptions.class))).thenAnswer(invocation -> {
            if (failBeforeCreation.getAndSet(false)) throw new ApiConnectionException("Simulated connection failure");
            RefundCreateParams params = invocation.getArgument(0);
            Map<String, String> metadata = (Map<String, String>) params.getMetadata();
            Refund refund = new Refund();
            refund.setId("re_test_" + metadata.get("refundId"));
            refund.setPaymentIntent(params.getPaymentIntent());
            refund.setAmount(params.getAmount());
            refund.setCurrency("usd");
            refund.setStatus(remoteRefundStatus);
            refund.setMetadata(metadata);
            remoteRefunds.add(refund);
            if (loseCreationResponse.getAndSet(false)) throw new ApiConnectionException("Simulated lost response");
            return refund;
        });
        when(stripe.v1().refunds().retrieve(anyString())).thenAnswer(invocation -> remoteRefunds.stream()
            .filter(refund -> refund.getId().equals(invocation.getArgument(0))).findFirst().orElseThrow());
        when(stripe.v1().checkout().sessions().retrieve(SESSION_ID)).thenReturn(session("open"));
        when(stripe.v1().checkout().sessions().expire(SESSION_ID)).thenReturn(session("expired"));
        when(stripe.v1().checkout().sessions().create(any(SessionCreateParams.class), any(RequestOptions.class)))
            .thenAnswer(invocation -> {
                Session result = session("open");
                result.setExpiresAt(((SessionCreateParams) invocation.getArgument(0)).getExpiresAt());
                return result;
            });
        HolidaysDeparture departure = new HolidaysDeparture();
        departure.setId(301L);
        departure.setStatus(1);
        departure.setBookedCount(1L);
        departure.setAvailableCount(9L);
        context.getBean(HolidaysDepartureMapper.class).insert(departure);
        HolidaysOrder order = new HolidaysOrder();
        order.setId(101L);
        order.setOrderNo("BH_TEST_EXPIRATION");
        order.setCustomerUserId(55L);
        order.setTourName("Test tour");
        order.setDepartureId(departure.getId());
        order.setTravelerCount(1);
        order.setStatus(0);
        order.setCurrency("usd");
        order.setTourAmount(new BigDecimal("100.00"));
        order.setExtrasAmount(BigDecimal.ZERO);
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setPaidAmount(BigDecimal.ZERO);
        order.setRefundedAmount(BigDecimal.ZERO);
        order.setCreateTime(LOCAL_NOW.minusMinutes(31));
        order.setPaymentDeadline(LOCAL_NOW.minusMinutes(1));
        orders.insert(order);
        HolidaysOrderPayment payment = new HolidaysOrderPayment();
        payment.setId(201L);
        payment.setOrderId(order.getId());
        payment.setStatus(PaymentStatus.CHECKOUT_CREATED);
        payment.setStripeCheckoutSessionId(SESSION_ID);
        payment.setStripePaymentIntentId(INTENT_ID);
        payment.setCurrency("usd");
        payment.setAmount(order.getTotalAmount());
        payment.setAmountMinor(10000L);
        payment.setCheckoutUrl("https://checkout.stripe.com/c/pay_test_fixture");
        payment.setCheckoutExpiresAt(LOCAL_NOW.plusHours(1));
        payments.insert(payment);
    }

    @Test
    void latePaymentBeforeCancellationScanIsRefundedAndSeatsAreReleasedOnce() {
        webhookService.processAcceptedEvent(event("checkout.session.completed", checkout("paid")));
        assertEquals(5, orders.selectById(101L).getStatus());
        assertEquals(PaymentStatus.REFUNDED, payments.selectById(201L).getStatus());
        assertEquals(1, refunds.selectCount(null));
        assertEquals(10, availableSeats());
        assertFalse(paymentService.expireOrder(101L));
        assertEquals(10, availableSeats());
    }

    @Test
    void networkFailureKeepsDurableRefundAndRetriesTheSameIdempotencyKey() throws Exception {
        failBeforeCreation.set(true);
        Long eventId = event("checkout.session.completed", checkout("paid"));
        webhookService.processAcceptedEvent(eventId);
        HolidaysOrderRefund pending = onlyRefund();
        assertEquals(4, orders.selectById(101L).getStatus());
        assertEquals(RefundStatus.PENDING, pending.getStatus());
        assertEquals(WebhookStatus.SUCCEEDED, events.selectById(eventId).getProcessStatus());
        assertEquals(10, availableSeats());
        context.getBean(PaymentRecoveryScheduler.class).recover();
        assertEquals(pending.getId(), onlyRefund().getId());
        assertEquals(RefundStatus.SUCCEEDED, onlyRefund().getStatus());
        assertEquals(5, orders.selectById(101L).getStatus());
        ArgumentCaptor<RequestOptions> options = ArgumentCaptor.forClass(RequestOptions.class);
        verify(stripe.v1().refunds(), times(2)).create(any(RefundCreateParams.class), options.capture());
        assertEquals(options.getAllValues().get(0).getIdempotencyKey(), options.getAllValues().get(1).getIdempotencyKey());
    }

    @Test
    void lostResponseIsReconciledEvenAfterTheStripeIdempotencyRetentionWindow() throws Exception {
        loseCreationResponse.set(true);
        webhookService.processAcceptedEvent(event("checkout.session.completed", checkout("paid")));
        assertEquals(RefundStatus.PENDING, onlyRefund().getStatus());
        when(clock.instant()).thenReturn(NOW.plusSeconds(3 * 24 * 60 * 60));
        refundService.submitRefund(onlyRefund().getId());
        assertEquals(1, remoteRefunds.size());
        assertEquals(RefundStatus.SUCCEEDED, onlyRefund().getStatus());
        verify(stripe.v1().refunds(), times(1)).create(any(RefundCreateParams.class), any(RequestOptions.class));
    }

    @Test
    void aLostDatabaseUpdateAfterStripeRefundsIsReconciledWithoutRefundingTwice() throws Exception {
        db.execute("ALTER TABLE holidays_order_refund ADD CONSTRAINT pending_only_test CHECK (status='PENDING')");
        try {
            webhookService.processAcceptedEvent(event("checkout.session.completed", checkout("paid")));
            assertEquals(1, remoteRefunds.size());
            assertEquals(RefundStatus.PENDING, onlyRefund().getStatus());
            assertNull(onlyRefund().getStripeRefundId());
            assertEquals(4, orders.selectById(101L).getStatus());
        } finally {
            db.execute("ALTER TABLE holidays_order_refund DROP CONSTRAINT pending_only_test");
        }
        context.getBean(PaymentRecoveryScheduler.class).recover();
        assertEquals(5, orders.selectById(101L).getStatus());
        assertEquals(RefundStatus.SUCCEEDED, onlyRefund().getStatus());
        verify(stripe.v1().refunds(), times(1)).create(any(RefundCreateParams.class), any(RequestOptions.class));
    }

    @Test
    void canceledOrderStaysCanceledUntilRefundSucceeds() {
        db.update("UPDATE holidays_order SET status=4 WHERE id=101");
        failBeforeCreation.set(true);
        webhookService.processAcceptedEvent(event("checkout.session.completed", checkout("paid")));
        assertEquals(4, orders.selectById(101L).getStatus());
        assertEquals(PaymentStatus.EXPIRED, payments.selectById(201L).getStatus());
        assertEquals(RefundStatus.PENDING, onlyRefund().getStatus());
        assertEquals(9, availableSeats(), "Already canceled orders must not release seats a second time");
    }

    @Test
    void pendingStripeRefundIsPolledWithoutCreatingAnotherRefundWhenItsWebhookIsMissing() throws Exception {
        remoteRefundStatus = "pending";
        webhookService.processAcceptedEvent(event("checkout.session.completed", checkout("paid")));
        assertEquals(4, orders.selectById(101L).getStatus());
        assertEquals(RefundStatus.PENDING, onlyRefund().getStatus());
        remoteRefunds.getFirst().setStatus("succeeded");
        context.getBean(PaymentRecoveryScheduler.class).recover();
        assertEquals(5, orders.selectById(101L).getStatus());
        assertEquals(RefundStatus.SUCCEEDED, onlyRefund().getStatus());
        verify(stripe.v1().refunds(), times(1)).create(any(RefundCreateParams.class), any(RequestOptions.class));
        verify(stripe.v1().refunds(), times(1)).retrieve(onlyRefund().getStripeRefundId());
    }

    @Test
    void olderRefundNotificationsCannotRegressACompletedRefund() {
        webhookService.processAcceptedEvent(event("checkout.session.completed", checkout("paid")));
        webhookService.processAcceptedEvent(event("refund.updated", Map.of("id", onlyRefund().getStripeRefundId(), "status", "pending")));
        webhookService.processAcceptedEvent(event("charge.refunded", Map.of("payment_intent", INTENT_ID, "amount_refunded", 2000L)));
        assertEquals(RefundStatus.SUCCEEDED, onlyRefund().getStatus());
        assertEquals(PaymentStatus.REFUNDED, payments.selectById(201L).getStatus());
        assertEquals(5, orders.selectById(101L).getStatus());
        assertEquals(0, new BigDecimal("100.00").compareTo(orders.selectById(101L).getRefundedAmount()));
    }

    @Test
    void unpaidCompletionWaitsForAsyncSuccessAndRefundsIfTheDeadlineHasPassed() {
        db.update("UPDATE holidays_order SET payment_deadline=? WHERE id=101", LOCAL_NOW.plusMinutes(5));
        webhookService.processAcceptedEvent(event("checkout.session.completed", checkout("unpaid")));
        assertEquals(0, orders.selectById(101L).getStatus());
        assertEquals(PaymentStatus.PROCESSING, payments.selectById(201L).getStatus());
        assertEquals(0, refunds.selectCount(null));
        when(clock.instant()).thenReturn(NOW.plusSeconds(6 * 60));
        webhookService.processAcceptedEvent(event("checkout.session.async_payment_succeeded", checkout("paid")));
        assertEquals(5, orders.selectById(101L).getStatus());
        assertEquals(1, remoteRefunds.size());
    }

    @Test
    void duplicateSuccessAfterTheDeadlineDoesNotRefundAnOrderAlreadyPaidOnTime() {
        db.update("UPDATE holidays_order SET payment_deadline=? WHERE id=101", LOCAL_NOW.plusMinutes(5));
        Long eventId = event("checkout.session.completed", checkout("paid"));
        webhookService.processAcceptedEvent(eventId);
        when(clock.instant()).thenReturn(NOW.plusSeconds(6 * 60));
        webhookService.processAcceptedEvent(eventId);
        webhookService.processAcceptedEvent(event("checkout.session.async_payment_succeeded", checkout("paid")));
        assertEquals(1, orders.selectById(101L).getStatus());
        assertEquals(PaymentStatus.PAID, payments.selectById(201L).getStatus());
        assertEquals(9, availableSeats());
        assertEquals(0, refunds.selectCount(null));
    }

    @Test
    void databaseFailureRollsBackCancellationAndRefundPreparationAndTheInboxCanRetry() {
        Long eventId = event("checkout.session.completed", checkout("paid"));
        db.execute("ALTER TABLE holidays_order_refund ADD CONSTRAINT reject_test_refund CHECK (amount < 0)");
        try {
            webhookService.processAcceptedEvent(eventId);
            assertEquals(WebhookStatus.FAILED, events.selectById(eventId).getProcessStatus());
            assertEquals(0, orders.selectById(101L).getStatus());
            assertEquals(0, orders.selectById(101L).getPaidAmount().signum());
            assertEquals(9, availableSeats());
            assertEquals(0, refunds.selectCount(null));
        } finally {
            db.execute("ALTER TABLE holidays_order_refund DROP CONSTRAINT reject_test_refund");
        }
        context.getBean(PaymentRecoveryScheduler.class).recover();
        assertEquals(WebhookStatus.SUCCEEDED, events.selectById(eventId).getProcessStatus());
        assertEquals(5, orders.selectById(101L).getStatus());
        assertEquals(10, availableSeats());
    }

    @Test
    void concurrentSuccessEventsOnlyCreateOneRefundAndReleaseSeatsOnce() throws Exception {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < 8; i++) ids.add(event("checkout.session.completed", checkout("paid")));
        try (var executor = Executors.newFixedThreadPool(4)) {
            var tasks = ids.stream().map(id -> executor.submit(() -> webhookService.processAcceptedEvent(id))).toList();
            for (var task : tasks) task.get(15, TimeUnit.SECONDS);
        }
        assertEquals(1, refunds.selectCount(null));
        assertEquals(1, remoteRefunds.size());
        assertEquals(10, availableSeats());
        assertEquals(5, orders.selectById(101L).getStatus());
    }

    @Test
    void checkoutUsesAValidStripeExpiryButKeepsTheOriginalReservationDeadline() throws Exception {
        db.update("UPDATE holidays_order SET payment_deadline=? WHERE id=101", LOCAL_NOW.plusMinutes(5));
        db.update("UPDATE holidays_order_payment SET status='PENDING', stripe_checkout_session_id=NULL WHERE id=201");
        PaymentCheckoutVo checkout = paymentService.createCheckoutSession(101L, 55L);
        ArgumentCaptor<SessionCreateParams> params = ArgumentCaptor.forClass(SessionCreateParams.class);
        verify(stripe.v1().checkout().sessions()).create(params.capture(), any(RequestOptions.class));
        assertTrue(params.getValue().getExpiresAt() >= NOW.getEpochSecond() + 30 * 60);
        assertTrue(params.getValue().getExpiresAt() <= NOW.getEpochSecond() + 24 * 60 * 60);
        assertEquals("101", params.getValue().getMetadata().get("orderId"));
        assertEquals(LOCAL_NOW.plusMinutes(5), checkout.getExpiresAt());
        paymentService.createCheckoutSession(101L, 55L);
        verify(stripe.v1().checkout().sessions(), times(1)).create(any(SessionCreateParams.class), any(RequestOptions.class));
    }

    @Test
    void failedSessionClosureIsRetriedAndOnlyClearedAfterRemoteClosure() throws Exception {
        paymentService.expireOrder(101L);
        when(stripe.v1().checkout().sessions().retrieve(SESSION_ID))
            .thenThrow(new ApiConnectionException("Simulated disconnection")).thenReturn(session("open"));
        paymentService.closeExpiredSession(201L);
        assertNotNull(payments.selectById(201L).getCheckoutUrl());
        assertEquals(List.of(201L), paymentService.findSessionsToExpire());
        paymentService.closeExpiredSession(201L);
        assertNull(payments.selectById(201L).getCheckoutUrl());
        assertTrue(paymentService.findSessionsToExpire().isEmpty());
        assertEquals(10, availableSeats());
    }

    @Test
    void alreadyCompletedSessionIsNotExpiredAgain() throws Exception {
        paymentService.expireOrder(101L);
        when(stripe.v1().checkout().sessions().retrieve(SESSION_ID)).thenReturn(session("complete"));
        paymentService.closeExpiredSession(201L);
        verify(stripe.v1().checkout().sessions(), never()).expire(anyString());
        assertNull(payments.selectById(201L).getCheckoutUrl());
    }

    @Test
    void legacyOrdersWithoutDeadlineAreCanceledByTheirCreationTime() {
        db.update("UPDATE holidays_order SET payment_deadline=NULL WHERE id=101");
        context.getBean(PaymentReservationScheduler.class).expireOrders();
        assertEquals(4, orders.selectById(101L).getStatus());
        assertEquals(LOCAL_NOW.minusMinutes(1), orders.selectById(101L).getPaymentDeadline());
        assertEquals(10, availableSeats());
    }

    @Test
    void aPendingRefundReservesItsBalanceAgainstAnotherManagerRequest() {
        db.update("UPDATE holidays_order SET status=1, paid_amount=100 WHERE id=101");
        Long id = refundService.prepareRefund(101L, new BigDecimal("60.00"), "First request");
        assertThrows(org.dromara.common.core.exception.ServiceException.class,
            () -> refundService.prepareRefund(101L, new BigDecimal("50.00"), "Second request"));
        assertEquals(RefundStatus.PENDING, refunds.selectById(id).getStatus());
        assertEquals(1, refunds.selectCount(null));
    }

    private Long event(String type, Map<String, Object> object) {
        StripeWebhookEvent event = new StripeWebhookEvent();
        event.setEventId("evt_test_" + UUID.randomUUID());
        event.setEventType(type);
        event.setPayload(JsonUtils.toJsonString(Map.of("data", Map.of("object", object))));
        event.setProcessStatus(WebhookStatus.PENDING);
        event.setReceivedAt(LOCAL_NOW);
        events.insert(event);
        return event.getId();
    }

    private Map<String, Object> checkout(String status) {
        return Map.of("id", SESSION_ID, "payment_intent", INTENT_ID, "currency", "usd",
            "amount_total", 10000L, "payment_status", status);
    }

    private HolidaysOrderRefund onlyRefund() {
        List<HolidaysOrderRefund> records = refunds.selectList(null);
        assertEquals(1, records.size());
        return records.getFirst();
    }

    private int availableSeats() {
        return context.getBean(HolidaysDepartureMapper.class).selectById(301L).getAvailableCount().intValue();
    }

    private Session session(String status) {
        Session session = new Session();
        session.setId(SESSION_ID);
        session.setStatus(status);
        session.setUrl("https://checkout.stripe.com/c/pay_test_fixture");
        session.setPaymentIntent(INTENT_ID);
        return session;
    }

    private static String sqlType(Class<?> type) {
        if (type == Long.class) return "BIGINT";
        if (type == Integer.class) return "INT";
        if (type == BigDecimal.class) return "DECIMAL(18,2)";
        if (type == LocalDateTime.class) return "TIMESTAMP";
        if (type == LocalDate.class) return "DATE";
        if (type == Boolean.class) return "BOOLEAN";
        return "VARCHAR(4000)";
    }

    @Configuration
    @EnableTransactionManagement
    @MapperScan("com.boxhilltravel.core.mapper")
    static class Config {
        @Bean
        static SpringUtils springUtils() { return new SpringUtils(); }
        @Bean
        JsonMapper jsonMapper() { return JsonMapper.builder().build(); }
        @Bean
        DataSource dataSource() { return new DriverManagerDataSource("jdbc:h2:mem:stripe_recovery;MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000", "sa", ""); }
        @Bean
        PlatformTransactionManager transactionManager(DataSource source) { return new DataSourceTransactionManager(source); }
        @Bean
        SqlSessionFactory sqlSessionFactory(DataSource source) throws Exception {
            MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
            factory.setDataSource(source);
            MybatisConfiguration configuration = new MybatisConfiguration();
            configuration.setMapUnderscoreToCamelCase(true);
            factory.setConfiguration(configuration);
            return factory.getObject();
        }
        @Bean
        Clock clock() { return mock(Clock.class); }
        @Bean
        StripeClient stripeClient() { return mock(StripeClient.class, RETURNS_DEEP_STUBS); }
        @Bean
        StripeProperties properties() {
            StripeProperties properties = new StripeProperties();
            properties.setEnabled(true);
            properties.setSuccessUrl("https://example.test/checkout/success");
            properties.setCancelUrl("https://example.test/checkout/success");
            return properties;
        }
        @Bean
        StripeCurrencyResolver currencyResolver() {
            StripeCurrencyResolver resolver = mock(StripeCurrencyResolver.class);
            when(resolver.resolve("usd")).thenReturn("usd");
            return resolver;
        }
        @Bean
        StripePaymentService paymentService(StripeProperties properties, ObjectProvider<StripeClient> client, StripeCurrencyResolver currencies,
            HolidaysOrderMapper orders, HolidaysOrderPaymentMapper payments, HolidaysDepartureMapper departures, Clock clock) {
            return new StripePaymentService(properties, client, currencies, orders, payments, departures, clock);
        }
        @Bean
        StripeRefundService refundService(StripeProperties properties, ObjectProvider<StripeClient> client, ObjectProvider<StripeRefundService> self,
            StripeCurrencyResolver currencies, HolidaysOrderMapper orders, HolidaysOrderPaymentMapper payments, HolidaysOrderRefundMapper refunds, Clock clock) {
            return new StripeRefundService(properties, client, self, currencies, orders, payments, refunds, clock);
        }
        @Bean
        StripeWebhookService webhookService(StripeProperties properties, ObjectProvider<StripeWebhookService> self, StripePaymentService payments,
            StripeCurrencyResolver currencies, StripeRefundService refunds, StripeWebhookEventMapper events, HolidaysOrderMapper orders,
            HolidaysOrderPaymentMapper paymentMapper, Clock clock) {
            return new StripeWebhookService(properties, self, payments, currencies, refunds, events, orders, paymentMapper, clock);
        }
        @Bean
        PaymentReservationScheduler reservationScheduler(HolidaysOrderMapper orders, StripePaymentService payments, StripeProperties properties, Clock clock) {
            return new PaymentReservationScheduler(orders, payments, properties, clock);
        }
        @Bean
        PaymentRecoveryScheduler recoveryScheduler(StripePaymentService payments, StripeRefundService refunds, StripeWebhookService webhooks,
            ObjectProvider<PaymentRecoveryScheduler> self) {
            return new PaymentRecoveryScheduler(payments, refunds, webhooks, self);
        }
    }
}
