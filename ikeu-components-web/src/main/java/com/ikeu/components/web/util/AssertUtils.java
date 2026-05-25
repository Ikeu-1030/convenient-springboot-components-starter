package com.ikeu.components.web.util;

import com.ikeu.components.web.exception.BusinessException;

import java.util.Collection;

/**
 * Assertion utilities — throw {@link BusinessException} on failure.
 * <p>
 * Use for parameter validation and business rule enforcement in service layers.
 * All methods use the single-argument constructor ({@code code = 500}).
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * AssertUtils.notNull(userId, "User ID must not be null");
 * AssertUtils.isTrue(amount > 0, "Amount must be positive");
 * AssertUtils.notEmpty(list, "List must not be empty");
 * AssertUtils.notBlank(name, "Name must not be blank");
 * AssertUtils.isFalse(deleted, "Record already deleted");
 * }</pre>
 *
 * @author ikeu
 * @since 1.0.0
 */
public final class AssertUtils {

    private AssertUtils() {
    }

    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new BusinessException(message);
        }
    }

    public static void isNull(Object obj, String message) {
        if (obj != null) {
            throw new BusinessException(message);
        }
    }

    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new BusinessException(message);
        }
    }

    public static void isFalse(boolean condition, String message) {
        if (condition) {
            throw new BusinessException(message);
        }
    }

    public static void notEmpty(String str, String message) {
        if (str == null || str.isEmpty()) {
            throw new BusinessException(message);
        }
    }

    public static void notBlank(String str, String message) {
        if (str == null || str.isBlank()) {
            throw new BusinessException(message);
        }
    }

    public static void notEmpty(Collection<?> col, String message) {
        if (col == null || col.isEmpty()) {
            throw new BusinessException(message);
        }
    }
}
