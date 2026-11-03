package com.boxhilltravel.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;

/** Recovers durable Stripe work without blocking local seat expiration on network I/O. */
@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(prefix = "stripe", name = "enabled", havingValue = "true")
public class PaymentRecoveryScheduler {

    private final StripePaymentService paymentService;
    private final StripeRefundService refundService;
    private final StripeWebhookService webhookService;
    private final ObjectProvider<PaymentRecoveryScheduler> selfProvider;
    private final Set<String> inFlight = ConcurrentHashMap.newKeySet();

    /** Each batch is bounded and interleaved so failing session closures cannot starve refunds. */
    @Scheduled(fixedDelayString = "${stripe.recovery-scan-ms:60000}")
    public void recover() {
        List<Long> sessions = paymentService.findSessionsToExpire();
        List<Long> refunds = refundService.findPendingRefunds();
        List<Long> events = webhookService.findEventsToRetry();
        PaymentRecoveryScheduler self = selfProvider.getObject();
        int size = Math.max(sessions.size(), Math.max(refunds.size(), events.size()));
        for (int index = 0; index < size; index++) {
            try {
                if (index < refunds.size()) {
                    Long id = refunds.get(index);
                    dispatch("refund:" + id, () -> self.submitRefund(id));
                }
                if (index < sessions.size()) {
                    Long id = sessions.get(index);
                    dispatch("session:" + id, () -> self.closeSession(id));
                }
                if (index < events.size()) {
                    Long id = events.get(index);
                    dispatch("event:" + id, () -> self.retryEvent(id));
                }
            } catch (RejectedExecutionException ex) {
                log.warn("Stripe recovery queue is full; persisted work will be picked up on the next scan");
                return;
            }
        }
    }

    @Async("stripeRecoveryExecutor")
    public void submitRefund(Long id) {
        try {
            refundService.submitRefund(id);
        } catch (Exception ex) {
            log.error("Stripe refund recovery failed, refundId={}", id, ex);
        } finally {
            inFlight.remove("refund:" + id);
        }
    }

    @Async("stripeRecoveryExecutor")
    public void closeSession(Long id) {
        try {
            paymentService.closeExpiredSession(id);
        } catch (Exception ex) {
            log.error("Stripe session closure recovery failed, paymentId={}", id, ex);
        } finally {
            inFlight.remove("session:" + id);
        }
    }

    @Async("stripeRecoveryExecutor")
    public void retryEvent(Long id) {
        try {
            webhookService.processAcceptedEvent(id);
        } finally {
            inFlight.remove("event:" + id);
        }
    }

    private void dispatch(String key, Runnable task) {
        if (!inFlight.add(key)) return;
        try {
            task.run();
        } catch (RejectedExecutionException ex) {
            inFlight.remove(key);
            throw ex;
        }
    }
}
