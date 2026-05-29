package com.ikeu.components.payment.model;

import lombok.Builder;
import lombok.Data;

/**
 * Unified payment result returned by the payment gateway.
 */
@Data
@Builder
public class PaymentResult {

    /** Platform-side transaction number. */
    private String tradeNo;

    /** Merchant-side order number (echoed from request). */
    private String outTradeNo;

    /** Current payment status. */
    private PaymentStatus status;

    /**
     * Raw response data from the payment gateway (JSON).
     * Useful for debugging or extracting vendor-specific fields.
     */
    private String rawData;

    /** Payment URL or QR code string (for native / H5 payment). */
    private String payUrl;

    public boolean isSuccess() {
        return status == PaymentStatus.SUCCESS;
    }
}
