package com.ikeu.components.sms.exception;

/**
 * Unified SMS exception wrapping vendor-specific errors.
 */
public class SmsException extends RuntimeException {

    private final String errorCode;

    public SmsException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SmsException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
