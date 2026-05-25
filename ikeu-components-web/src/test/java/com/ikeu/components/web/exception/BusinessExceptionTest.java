package com.ikeu.components.web.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void constructor_messageOnly_defaultCode500() {
        BusinessException ex = new BusinessException("Something wrong");
        assertEquals("Something wrong", ex.getMessage());
        assertEquals(500, ex.getCode());
        assertNull(ex.getCause());
    }

    @Test
    void constructor_codeAndMessage() {
        BusinessException ex = new BusinessException(404, "Not found");
        assertEquals(404, ex.getCode());
        assertEquals("Not found", ex.getMessage());
    }

    @Test
    void constructor_codeMessageCause() {
        Throwable cause = new RuntimeException("root");
        BusinessException ex = new BusinessException(400, "Invalid", cause);
        assertEquals(400, ex.getCode());
        assertEquals("Invalid", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void isRuntimeException() {
        BusinessException ex = new BusinessException("msg");
        assertTrue(ex instanceof RuntimeException);
    }
}