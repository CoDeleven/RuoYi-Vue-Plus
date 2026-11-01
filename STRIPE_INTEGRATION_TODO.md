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
3. Put the endpoint signing secret in `STRIPE_WEBHOOK_SECRET`. The endpoint must receive the raw request body so signature verification can succeed.
4. Set the public H5 URL in `STRIPE_SUCCESS_URL` and `STRIPE_CANCEL_URL`, then set `STRIPE_ENABLED=true`.
5. Start the backend and H5, create an order, and use **Pay securely with Stripe** to redirect to the Stripe-hosted Checkout page.
6. Use Stripe test card `4242 4242 4242 4242`, any future expiry date, and any CVC in Test mode.
7. Confirm that the webhook changes the order to paid, and test cancellation, expiration, payment failure, partial refund, and full refund.

### Integration consistency check

The Scenario A field list removed the existing `client_reference_id`, `expires_at`, and Checkout Session `metadata` parameters. The current webhook implementation uses Session metadata to resolve `orderId`, so before end-to-end testing it must be changed to resolve the order from the stored Checkout Session ID, or the metadata parameter must be restored. This task intentionally left surrounding webhook code unchanged per the Scenario A constraints.

## Flow Overview

The backend recalculates the order amount and currency, creates or reuses a hosted Checkout Session, and returns its URL to H5. Stripe sends the payment result to the webhook endpoint. Order fulfillment and refund state are updated from webhook events rather than from the browser return page.

## Resources

- https://docs.stripe.com/payments/checkout
- https://docs.stripe.com/api/checkout/sessions/create
- https://docs.stripe.com/webhooks
- https://support.stripe.com
