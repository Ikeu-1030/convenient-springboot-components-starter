package com.ikeu.components.web.util;

import com.ikeu.components.web.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AssertUtilsTest {

    @Test
    void notNull_passes() {
        AssertUtils.notNull("value", "should not throw");
    }

    @Test
    void notNull_throws() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> AssertUtils.notNull(null, "Value required"));
        assertEquals("Value required", ex.getMessage());
    }

    @Test
    void isNull_passes() {
        AssertUtils.isNull(null, "should not throw");
    }

    @Test
    void isNull_throws() {
        assertThrows(BusinessException.class,
                () -> AssertUtils.isNull("value", "Should be null"));
    }

    @Test
    void isTrue_passes() {
        AssertUtils.isTrue(1 == 1, "should not throw");
    }

    @Test
    void isTrue_throws() {
        assertThrows(BusinessException.class,
                () -> AssertUtils.isTrue(1 == 2, "Condition failed"));
    }

    @Test
    void isFalse_passes() {
        AssertUtils.isFalse(1 == 2, "should not throw");
    }

    @Test
    void isFalse_throws() {
        assertThrows(BusinessException.class,
                () -> AssertUtils.isFalse(true, "Should be false"));
    }

    @Test
    void notEmpty_string_passes() {
        AssertUtils.notEmpty("abc", "should not throw");
    }

    @Test
    void notEmpty_string_null() {
        assertThrows(BusinessException.class,
                () -> AssertUtils.notEmpty((String) null, "Empty"));
    }

    @Test
    void notEmpty_string_empty() {
        assertThrows(BusinessException.class,
                () -> AssertUtils.notEmpty("", "Empty"));
    }

    @Test
    void notBlank_passes() {
        AssertUtils.notBlank("abc", "should not throw");
    }

    @Test
    void notBlank_blankString() {
        assertThrows(BusinessException.class,
                () -> AssertUtils.notBlank("   ", "Blank"));
    }

    @Test
    void notBlank_null() {
        assertThrows(BusinessException.class,
                () -> AssertUtils.notBlank(null, "Null not allowed"));
    }

    @Test
    void notEmpty_collection_passes() {
        List<String> list = Arrays.asList("a", "b");
        AssertUtils.notEmpty(list, "should not throw");
    }

    @Test
    void notEmpty_collection_null() {
        assertThrows(BusinessException.class,
                () -> AssertUtils.notEmpty((List<?>) null, "Empty"));
    }

    @Test
    void notEmpty_collection_empty() {
        assertThrows(BusinessException.class,
                () -> AssertUtils.notEmpty(Collections.emptyList(), "Empty"));
    }
}