package com.ikeu.components.payment.model;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Unified payment order request.
 */
@Data
@Builder
public class PaymentRequest {

    /** Merchant-side order number (must be unique). */
    private String outTradeNo;

    /** Total amount in the smallest currency unit (fen for CNY, cents for USD). */
    private Long totalAmount;

    /** Order / product description shown to the user. */
    private String subject;

    /** Callback URL for payment result notification. */
    private String notifyUrl;

    /** Optional extension parameters (vendor-specific). */
    @Builder.Default
    private Map<String, String> optional = new HashMap<>();
}
