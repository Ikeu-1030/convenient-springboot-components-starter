package com.ikeu.components.payment.model;

/**
 * Payment transaction status.
 */
public enum PaymentStatus {

    /** Payment succeeded. */
    SUCCESS,

    /** Order has been closed / cancelled. */
    CLOSED,

    /** Order has been fully or partially refunded. */
    REFUND,

    /** Order created but no payment received yet. */
    NOTPAY,

    /** User is in the process of paying (e.g. scanning QR, entering password). */
    USERPAYING
}
