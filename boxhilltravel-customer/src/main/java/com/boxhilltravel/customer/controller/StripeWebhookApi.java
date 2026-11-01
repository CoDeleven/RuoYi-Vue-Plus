package com.boxhilltravel.customer.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.boxhilltravel.payment.service.StripeWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public Stripe webhook endpoint.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/payments/stripe")
public class StripeWebhookApi {

    private final StripeWebhookService webhookService;

    /**
     * Accepts a signed Stripe webhook payload.
     */
    @SaIgnore
    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> webhook(@RequestBody String payload,
                                          @RequestHeader(value = "Stripe-Signature", required = false) String signature) {
        webhookService.accept(payload, signature);
        return ResponseEntity.ok("{}");
    }

}
