package com.ikeu.components.web.util;

import com.ikeu.components.web.exception.BusinessException;

import java.util.Collection;

/**
 * Assertion utilities that throw BusinessException on failure.
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
