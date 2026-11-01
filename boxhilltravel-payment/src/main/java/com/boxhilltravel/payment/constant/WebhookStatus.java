package com.boxhilltravel.payment.constant;

/**
 * Webhook inbox processing statuses.
 */
public final class WebhookStatus {

    public static final String PENDING = "PENDING";
    public static final String PROCESSING = "PROCESSING";
    public static final String SUCCEEDED = "SUCCEEDED";
    public static final String FAILED = "FAILED";

    private WebhookStatus() {
    }
}
