package com.ikeu.components.web.exception;

import lombok.Getter;

/**
 * Custom {@link RuntimeException} carrying an integer error code for business logic errors.
 * <p>
 * Caught by {@link com.ikeu.components.web.handler.GlobalExceptionHandler} which returns
 * HTTP 200 with the error details in the JSON body (by design — errors are expressed in
 * the body, not the HTTP status line).
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * throw new BusinessException(404, "User not found");
 * throw new BusinessException(400, "Invalid parameter: " + fieldName);
 * throw new BusinessException("Something went wrong");  // default code 500
 * }</pre>
 *
 * @author ikeu
 * @since 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
