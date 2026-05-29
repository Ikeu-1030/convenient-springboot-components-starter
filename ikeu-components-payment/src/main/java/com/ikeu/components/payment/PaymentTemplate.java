package com.ikeu.components.payment;

import com.ikeu.components.payment.exception.PaymentException;
import com.ikeu.components.payment.model.PaymentRequest;
import com.ikeu.components.payment.model.PaymentResult;
import com.ikeu.components.payment.model.RefundRequest;

/**
 * Unified payment gateway abstraction.
 * <p>
 * Implementations wrap vendor-specific SDKs (WeChat Pay, Alipay) and translate
 * their exceptions into {@link PaymentException}.
 */
public interface PaymentTemplate {

    /**
     * Create a unified order (native scan, H5, JSAPI, etc.).
     * Returns a {@link PaymentResult} whose {@code payUrl} contains the
     * QR code URL or payment redirect URL.
     */
    PaymentResult unifiedOrder(PaymentRequest request);

    /** Query order status by merchant order number. */
    PaymentResult queryOrder(String outTradeNo);

    /** Close / cancel an unpaid order. */
    boolean closeOrder(String outTradeNo);

    /** Submit a refund for a paid order. */
    PaymentResult refund(RefundRequest request);

    /**
     * Verify the signature of a payment callback (notify) body.
     *
     * @param callbackData raw callback payload (JSON / form data)
     * @param signature    signature header value from the callback
     * @return true if the signature is valid
     */
    boolean verifyCallback(String callbackData, String signature);
}
