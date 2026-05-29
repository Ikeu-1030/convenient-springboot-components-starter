package com.ikeu.components.payment.model;

import lombok.Builder;
import lombok.Data;

/**
 * Unified refund request.
 */
@Data
@Builder
public class RefundRequest {

    /** Merchant-side order number to refund. */
    private String outTradeNo;

    /** Refund amount in the smallest currency unit. */
    private Long refundAmount;

    /** Total original order amount (some gateways require this for partial refunds). */
    private Long totalAmount;

    /** Reason for the refund (shown to user on some platforms). */
    private String refundReason;

    /** Optional merchant-side refund number. */
    private String outRefundNo;
}
