package com.ikeu.components.web.handler;

import com.ikeu.components.web.exception.BusinessException;
import com.ikeu.components.web.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler for REST controllers ({@code @RestControllerAdvice}).
 *
 * <h3>Handled exceptions</h3>
 * <table>
 *   <tr><th>Exception</th><th>HTTP status</th><th>Result code</th></tr>
 *   <tr><td>{@link BusinessException}</td><td>200 OK</td><td>exception.getCode()</td></tr>
 *   <tr><td>{@code MethodArgumentNotValidException}</td><td>400</td><td>400</td></tr>
 *   <tr><td>{@code HttpMessageNotReadableException}</td><td>400</td><td>400</td></tr>
 *   <tr><td>{@code Exception} (catch-all)</td><td>200 OK</td><td>500</td></tr>
 * </table>
 * <p>
 * <b>Design note:</b> {@code BusinessException} returns HTTP 200 by design — the
 * error information is carried in the JSON body ({@code code} + {@code message}),
 * not in the HTTP status line. This is a common convention in REST APIs targeting
 * frontend clients that may struggle with non-2xx responses.
 *
 * @author ikeu
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("Business exception: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validation failed: {}", msg);
        return Result.error(400, msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());
        return Result.error(400, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return Result.error(500, "Internal Server Error");
    }
}
