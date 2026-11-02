-- Stripe payment migration for MySQL 8+
ALTER TABLE holidays_order
    ADD COLUMN IF NOT EXISTS refunded_amount DECIMAL(18, 2) NOT NULL DEFAULT 0 AFTER paid_amount,
    ADD COLUMN IF NOT EXISTS payment_deadline DATETIME NULL AFTER refunded_amount;

ALTER TABLE holidays_order_payment
    ADD COLUMN IF NOT EXISTS stripe_checkout_session_id VARCHAR(255) NULL AFTER payment_method,
    ADD COLUMN IF NOT EXISTS checkout_url VARCHAR(1000) NULL AFTER stripe_checkout_session_id,
    ADD COLUMN IF NOT EXISTS stripe_payment_intent_id VARCHAR(255) NULL AFTER checkout_url,
    ADD COLUMN IF NOT EXISTS amount_minor BIGINT NULL AFTER amount,
    ADD COLUMN IF NOT EXISTS checkout_expires_at DATETIME NULL AFTER amount_minor,
    ADD COLUMN IF NOT EXISTS failure_code VARCHAR(255) NULL AFTER checkout_expires_at;

ALTER TABLE holidays_order_payment
    ADD UNIQUE KEY uk_holidays_order_payment_checkout_session (stripe_checkout_session_id),
    ADD UNIQUE KEY uk_holidays_order_payment_payment_intent (stripe_payment_intent_id);

CREATE TABLE IF NOT EXISTS holidays_order_refund (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    payment_id BIGINT NOT NULL,
    stripe_refund_id VARCHAR(255) NULL,
    amount DECIMAL(18, 2) NOT NULL,
    amount_minor BIGINT NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(32) NOT NULL,
    reason VARCHAR(255) NULL,
    create_dept BIGINT NULL,
    create_by BIGINT NULL,
    create_time DATETIME NULL,
    update_by BIGINT NULL,
    update_time DATETIME NULL,
    UNIQUE KEY uk_holidays_order_refund_stripe_id (stripe_refund_id),
    KEY idx_holidays_order_refund_order_id (order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS stripe_webhook_event (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    event_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    object_id VARCHAR(255) NULL,
    payload LONGTEXT NOT NULL,
    process_status VARCHAR(32) NOT NULL,
    error_message VARCHAR(1000) NULL,
    received_at DATETIME NOT NULL,
    processed_at DATETIME NULL,
    create_dept BIGINT NULL,
    create_by BIGINT NULL,
    create_time DATETIME NULL,
    update_by BIGINT NULL,
    update_time DATETIME NULL,
    UNIQUE KEY uk_stripe_webhook_event_event_id (event_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
