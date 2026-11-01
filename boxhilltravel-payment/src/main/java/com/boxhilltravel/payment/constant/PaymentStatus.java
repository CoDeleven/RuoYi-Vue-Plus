package com.boxhilltravel.payment.constant;

/**
 * Payment status constants persisted in holidays_order_payment.
 */
public final class PaymentStatus {

    public static final String PENDING = "PENDING";
    public static final String CHECKOUT_CREATED = "CHECKOUT_CREATED";
    public static final String PROCESSING = "PROCESSING";
    public static final String PAID = "PAID";
    public static final String PAYMENT_FAILED = "PAYMENT_FAILED";
    public static final String EXPIRED = "EXPIRED";
    public static final String PARTIALLY_REFUNDED = "PARTIALLY_REFUNDED";
    public static final String REFUNDED = "REFUNDED";

    private PaymentStatus() {
    }
}
