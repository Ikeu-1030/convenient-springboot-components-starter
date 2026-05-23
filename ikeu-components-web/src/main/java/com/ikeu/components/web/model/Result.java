package com.ikeu.components.web.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response wrapper.
 *
 * @param <T> data type
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
