package com.ikeu.components.web.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response wrapper with {@code code}, {@code message}, and {@code data}.
 * <p>
 * Uses {@code @JsonInclude(NON_NULL)} — null data is omitted from serialized JSON.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * // Success
 * Result.success(user);                         // code=200, message="success"
 * Result.success(201, "Created", newUser);
 *
 * // Error
 * Result.error(400, "Bad request");             // code=400
 * Result.error(401, "Unauthorized");
 * Result.error(500, "Internal error");
 *
 * // Conditional (auto-judge null)
 * Result.of(data);  // data != null → success; data == null → 404 error
 * }</pre>
 *
 * <h3>Standard codes</h3>
 * <table>
 *   <tr><td>200</td><td>Success</td></tr>
 *   <tr><td>400</td><td>Bad request / validation error</td></tr>
 *   <tr><td>401</td><td>Unauthorized</td></tr>
 *   <tr><td>403</td><td>Forbidden</td></tr>
 *   <tr><td>404</td><td>Not found</td></tr>
 *   <tr><td>500</td><td>Internal server error</td></tr>
 * </table>
 *
 * @param <T> data type
 * @author ikeu
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    public static final int CODE_SUCCESS = 200;
    public static final int CODE_ERROR = 500;
    public static final int CODE_BAD_REQUEST = 400;

    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(CODE_SUCCESS)
                .message("success")
                .data(data)
                .build();
    }

    public static <T> Result<T> success(String message, T data) {
        return Result.<T>builder()
                .code(CODE_SUCCESS)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> Result<T> error(int code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    public static <T> Result<T> error(String message) {
        return Result.<T>builder()
                .code(CODE_ERROR)
                .message(message)
                .build();
    }

    /**
     * Auto-judge: if data is null, return error; otherwise return success.
     */
    public static <T> Result<T> of(T data) {
        return data != null ? success(data) : error("No data");
    }

    public boolean isSuccess() {
        return code == CODE_SUCCESS;
    }
}
