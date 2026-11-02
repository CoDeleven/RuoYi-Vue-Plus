package com.boxhilltravel.payment.config;

import com.stripe.StripeClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.StringUtils;

/**
 * Stripe SDK configuration.
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(StripeProperties.class)
public class StripePaymentConfiguration {

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
