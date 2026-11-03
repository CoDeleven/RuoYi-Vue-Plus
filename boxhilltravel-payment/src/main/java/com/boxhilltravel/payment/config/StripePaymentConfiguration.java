package com.boxhilltravel.payment.config;

import com.stripe.StripeClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;

import java.time.Clock;

/**
 * Stripe SDK configuration.
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(StripeProperties.class)
public class StripePaymentConfiguration {

    @Bean
    public Clock stripeClock() {
        return Clock.systemDefaultZone();
    }

    /** Keeps slow Stripe recovery requests off the reservation expiration scheduler. */
    @Bean(value = "stripeRecoveryExecutor", defaultCandidate = false)
    public ThreadPoolTaskExecutor stripeRecoveryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("stripe-recovery-");
        return executor;
    }

    @Bean
    @ConditionalOnProperty(prefix = "stripe", name = "enabled", havingValue = "true")
    public StripeClient stripeClient(StripeProperties properties) {
        if (!StringUtils.hasText(properties.getSecretKey())) {
            throw new IllegalStateException("stripe.secret-key must be configured when Stripe is enabled");
        }
        return StripeClient.builder()
            .setApiKey(properties.getSecretKey())
            .setMaxNetworkRetries(properties.getMaxNetworkRetries())
            .setConnectTimeout(properties.getConnectTimeoutMs())
            .setReadTimeout(properties.getReadTimeoutMs())
            .build();
    }

}
