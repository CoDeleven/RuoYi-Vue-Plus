package com.boxhilltravel.payment.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.boxhilltravel.core.domain.HolidaysOrder;
import com.boxhilltravel.core.mapper.HolidaysOrderMapper;
import com.boxhilltravel.payment.config.StripeProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Releases seats held by unpaid orders after the payment deadline.
 */
@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(prefix = "stripe", name = "enabled", havingValue = "true")
public class PaymentReservationScheduler {

    private final HolidaysOrderMapper orderMapper;
    private final StripePaymentService paymentService;
    private final StripeProperties properties;
    private final Clock stripeClock;

    @Scheduled(fixedDelayString = "${stripe.expiration-scan-ms:60000}")
    public void expireOrders() {
        LocalDateTime now = LocalDateTime.now(stripeClock);
        orderMapper.selectList(Wrappers.lambdaQuery(HolidaysOrder.class)
                .eq(HolidaysOrder::getStatus, 0)
                .and(query -> query.le(HolidaysOrder::getPaymentDeadline, now)
                    .or(legacy -> legacy.isNull(HolidaysOrder::getPaymentDeadline)
                        .le(HolidaysOrder::getCreateTime, now.minusMinutes(properties.getPaymentExpireMinutes()))))
                .orderByAsc(HolidaysOrder::getCreateTime)
                .last("limit 200"))
            .forEach(order -> {
                try {
                    paymentService.expireOrder(order.getId());
                } catch (Exception ex) {
                    log.error("Failed to expire unpaid order, orderId={}", order.getId(), ex);
                }
            });
    }

}
