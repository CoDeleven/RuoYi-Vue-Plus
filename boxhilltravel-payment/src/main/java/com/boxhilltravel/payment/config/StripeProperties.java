package com.boxhilltravel.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Stripe integration properties.
 */
@Data
@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {

    private boolean enabled;

    private String secretKey;

    private String webhookSecret;

    private String successUrl;

    private String cancelUrl;

    private int paymentExpireMinutes = 30;

    private List<String> supportedCurrencies = new ArrayList<>();

    private int maxNetworkRetries = 2;

    private int connectTimeoutMs = 30_000;

    private int readTimeoutMs = 80_000;

}
