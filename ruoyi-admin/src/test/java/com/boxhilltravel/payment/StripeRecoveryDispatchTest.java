package com.boxhilltravel.payment;

import com.boxhilltravel.payment.config.StripePaymentConfiguration;
import com.boxhilltravel.payment.service.PaymentRecoveryScheduler;
import com.boxhilltravel.payment.service.StripePaymentService;
import com.boxhilltravel.payment.service.StripeRefundService;
import com.boxhilltravel.payment.service.StripeWebhookService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.MapPropertySource;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Verifies the actual async proxy/executor rather than relying on direct scheduler calls. */
@Tag("dev")
class StripeRecoveryDispatchTest {

    @Test
    void slowNetworkRecoveryDoesNotBlockTheSchedulerOrEnqueueTheSameWorkTwice() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(1);
        AtomicReference<String> workerName = new AtomicReference<>();
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("stripe-test",
                Map.of("stripe.enabled", "false")));
            context.register(Config.class);
            context.refresh();
            StripePaymentService payments = context.getBean(StripePaymentService.class);
            when(payments.findSessionsToExpire()).thenReturn(List.of(201L));
            doAnswer(invocation -> {
                workerName.set(Thread.currentThread().getName());
                started.countDown();
                try {
                    assertTrue(release.await(5, TimeUnit.SECONDS));
                } finally {
                    finished.countDown();
                }
                return null;
            }).when(payments).closeExpiredSession(201L);
            PaymentRecoveryScheduler scheduler = context.getBean(PaymentRecoveryScheduler.class);
            try {
                assertTimeoutPreemptively(java.time.Duration.ofSeconds(2), scheduler::recover);
                assertTrue(started.await(2, TimeUnit.SECONDS));
                assertTrue(workerName.get().startsWith("stripe-recovery-"));
                scheduler.recover();
                verify(payments, times(1)).closeExpiredSession(201L);
            } finally {
                release.countDown();
                assertTrue(finished.await(2, TimeUnit.SECONDS));
            }
        }
    }

    @Configuration
    @EnableAsync(proxyTargetClass = true)
    @Import(StripePaymentConfiguration.class)
    static class Config {
        @Bean
        StripePaymentService payments() { return mock(StripePaymentService.class); }
        @Bean
        StripeRefundService refunds() { return mock(StripeRefundService.class); }
        @Bean
        StripeWebhookService webhooks() { return mock(StripeWebhookService.class); }
        @Bean
        PaymentRecoveryScheduler recovery(StripePaymentService payments, StripeRefundService refunds,
            StripeWebhookService webhooks, ObjectProvider<PaymentRecoveryScheduler> self) {
            return new PaymentRecoveryScheduler(payments, refunds, webhooks, self);
        }
    }
}
