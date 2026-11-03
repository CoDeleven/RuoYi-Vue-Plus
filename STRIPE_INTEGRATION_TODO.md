# Stripe Integration TODO

This file is the single source of truth for the remaining Stripe setup before testing and production launch.

## Values to Replace

The Checkout Session call does not contain sample Stripe Price IDs. It creates one-time line items from the server-calculated order amount and currency. The following deployment values still need to be supplied:

**Files containing placeholders:**

- [ruoyi-admin/src/main/resources/application.yml](ruoyi-admin/src/main/resources/application.yml)

| Field | Current value | What to set |
|---|---|---|
| `stripe.enabled` | `${STRIPE_ENABLED:false}` | `true` in the environment where Stripe should be active. |
| `stripe.secret-key` | `${STRIPE_SECRET_KEY:}` | A Stripe test key for testing and a restricted live API key for production. |
| `stripe.webhook-secret` | `${STRIPE_WEBHOOK_SECRET:}` | The `whsec_...` signing secret for the configured webhook endpoint. |
| `stripe.success-url` | `http://localhost:3000/checkout/success` | The public H5 success page URL. |
| `stripe.cancel-url` | `http://localhost:3000/checkout/success` | The public H5 return/cancel page URL. |

## Configured Parameters

The existing Checkout Session call is in:

- [boxhilltravel-payment/src/main/java/com/boxhilltravel/payment/service/StripePaymentService.java](boxhilltravel-payment/src/main/java/com/boxhilltravel/payment/service/StripePaymentService.java)

| Parameter | Value |
|---|---|
| `ui_mode` | `hosted_page` |
| `mode` | `payment` (one-time payment) |
| `billing_address_collection` | `auto` |
| `phone_number_collection.enabled` | `true` |
| `automatic_tax.enabled` | `false` |
| `allow_promotion_codes` | `false` |
| `submit_type` | `auto` |
| `integration_identifier` | `hosted_web_0002` |
| `origin_context` | `web` |
| `success_url` | Built from `stripe.success-url`, with `orderId` and `{CHECKOUT_SESSION_ID}` appended |
| `cancel_url` | Built from `stripe.cancel-url`, with `orderId`, `{CHECKOUT_SESSION_ID}`, and `cancelled=1` appended |
| `line_items` | Server-calculated dynamic `price_data` using the order amount and currency |

`payment_method_collection` is intentionally omitted because the Session uses `payment` mode; Stripe collects the payment method for this mode.

## Setup and Next Steps

1. Run [script/sql/boxhilltravel_stripe_payment.sql](script/sql/boxhilltravel_stripe_payment.sql) against the application database.
2. In Stripe Test mode, configure a webhook endpoint at `/api/v1/payments/stripe/webhook` and subscribe to the Checkout, payment failure, expiration, and refund events handled by `StripeWebhookService`.
3. Put the endpoint signing secret in `STRIPE_WEBHOOK_SECRET`. The endpoint must receive the raw request body so signature verification can succeed. For local development, run `stripe listen` with `127.0.0.1:8088` and use the `whsec_...` printed by that same listener process.
4. Set the public H5 URL in `STRIPE_SUCCESS_URL` and `STRIPE_CANCEL_URL`, then set `STRIPE_ENABLED=true`.
5. Start the backend and H5, create an order, and use **Pay securely with Stripe** to redirect to the Stripe-hosted Checkout page.
6. Use Stripe test card `4242 4242 4242 4242`, any future expiry date, and any CVC in Test mode.
7. Confirm that the webhook changes the order to paid, and test cancellation, expiration, payment failure, partial refund, and full refund.

The tour table stores values from the `holidays_currency_unit` dictionary (for example, `1`), while Stripe requires a three-letter ISO code. The payment module resolves the dictionary value to its ISO label (for example, `1` → `usd`) before creating a Checkout Session or refund.

### Integration consistency check

Checkout Sessions include `orderId` and `paymentId` metadata; PaymentIntents include `orderId`. Existing Sessions remain supported through the stored Checkout Session ID and PaymentIntent fallback lookups.

### Reservation expiration and refund recovery

- `STRIPE_PAYMENT_EXPIRE_MINUTES` defaults to **30 minutes from order creation**. Retrying checkout does not extend the deadline. Legacy orders with a missing deadline use their creation time plus this duration.
- `STRIPE_EXPIRATION_SCAN_MS` defaults to **60000**. The reservation task cancels overdue unpaid orders and releases seats once. It does not wait for a Stripe network request.
- `STRIPE_RECOVERY_SCAN_MS` defaults to **60000**. A separate bounded worker pool closes expired local Checkout Sessions, retries unfinished webhook events, and submits/reconciles pending refunds. Restart the backend after deploying these changes.
- Stripe requires a new Session expiration to be at least 30 minutes away. The Session request uses a valid expiration with transit allowance; the original local deadline remains authoritative. At that local deadline the recovery worker actively expires the Session. Network failures or the scan interval can leave a short window where the Stripe page still accepts payment; a paid webhook for an expired/canceled reservation queues a full refund and never makes the canceled order fulfillable.
- Only `payment_status=paid` settles an order. An unpaid `checkout.session.completed` waits for asynchronous payment success.
- Late-payment refunds are durably recorded with reason `LATE_PAYMENT_AFTER_EXPIRATION` in the same transaction as local payment/cancellation changes. Network/API errors preserve `PENDING` and retry the same refund record and idempotency key. Remote metadata is reconciled before creating a refund, including after Stripe's idempotency key retention window. Pending refunds reserve their amount against additional manager requests.
- A successful refund updates the aggregate and order state even if the refund webhook is delayed. Stripe-confirmed `FAILED`/`CANCELED` refunds remain visible for administrative handling; technical retries do not silently create replacement refunds after an explicit failure.
- A new SQL migration is not required for these recovery changes; they reuse the existing refund/event tables from the Stripe migration above. Deploy the updated backend and keep the required payment/refund webhook forwarding active.

### Automated regression checks

Use Java 21. Install the reactor dependencies with `mvn -pl ruoyi-admin -am -Dmaven.test.skip=true install`, then run:

```shell
mvn -pl ruoyi-admin -Dmaven.test.skip=false -Dtest=StripeExpirationRegressionTest,StripePaymentRecoveryTest,StripeRecoveryDispatchTest test
```

The tests use an isolated in-memory H2 database and a mocked Stripe client. They cover deadline races, cancellation, single seat release, concurrent/duplicate events, unpaid asynchronous payments, transaction rollback, network failures, lost responses after 24 hours, session closure retry, pending refund reservations, refund reconciliation without a webhook, and the actual async recovery executor. No real Stripe refund is submitted by these tests.

## Flow Overview

The backend recalculates the order amount and currency, creates or reuses a hosted Checkout Session, and returns its URL to H5. Stripe sends the payment result to the webhook endpoint. Order fulfillment and refund state are updated from webhook events rather than from the browser return page.

## Resources

- https://docs.stripe.com/payments/checkout
- https://docs.stripe.com/api/checkout/sessions/create
- https://docs.stripe.com/webhooks
- https://support.stripe.com
