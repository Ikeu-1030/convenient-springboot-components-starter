package com.ikeu.components.web.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void success_defaultCodeAndMessage() {
        Result<String> result = Result.success("data");
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("data", result.getData());
    }

    @Test
    void success_customMessage() {
        Result<Integer> result = Result.success("OK", 42);
        assertEquals(200, result.getCode());
        assertEquals("OK", result.getMessage());
        assertEquals(42, result.getData());
    }

    @Test
    void error_withCodeAndMessage() {
        Result<Void> result = Result.error(400, "Bad request");
        assertEquals(400, result.getCode());
        assertEquals("Bad request", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void error_defaultCode() {
        Result<Void> result = Result.error("Something wrong");
        assertEquals(500, result.getCode());
        assertEquals("Something wrong", result.getMessage());
    }

    @Test
    void of_nonNullData() {
        Result<String> result = Result.of("exists");
        assertEquals(200, result.getCode());
        assertEquals("exists", result.getData());
    }

    @Test
    void of_nullData() {
        Result<Void> result = Result.of(null);
        assertEquals(500, result.getCode());
        assertEquals("No data", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void isSuccess_true() {
        assertTrue(Result.success("ok").isSuccess());
    }

    @Test
    void isSuccess_false() {
        assertFalse(Result.error(400, "bad").isSuccess());
    }

    @Test
    void builder_allFields() {
        Result<String> result = Result.<String>builder()
                .code(201).message("Created").data("entity").build();
        assertEquals(201, result.getCode());
        assertEquals("Created", result.getMessage());
        assertEquals("entity", result.getData());
    }
}