package com.ikeu.components.web.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResultTest {

    @Test
    void builder_allFields() {
        List<String> records = Arrays.asList("a", "b");
        PageResult<String> result = PageResult.<String>builder()
                .total(100).current(2).pages(10).records(records).build();
        assertEquals(100, result.getTotal());
        assertEquals(2, result.getCurrent());
        assertEquals(10, result.getPages());
        assertEquals(2, result.getRecords().size());
    }

    @Test
    void of_nullPage_returnsEmptyDefaults() {
        List<String> records = Arrays.asList("x", "y");
        PageResult<String> result = PageResult.of(null, records);
        assertEquals(0, result.getTotal());
        assertEquals(1, result.getCurrent());
        assertEquals(0, result.getPages());
        assertEquals(2, result.getRecords().size());
    }

    @Test
    void of_nullRecords_returnsEmptyList() {
        PageResult<String> result = PageResult.of(new Object(), null);
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    void noArgsConstructor_defaults() {
        PageResult<String> result = new PageResult<>();
        assertEquals(0, result.getTotal());
        assertNull(result.getRecords());
    }

    @Test
    void allArgsConstructor() {
        List<Integer> records = Collections.singletonList(1);
        PageResult<Integer> result = new PageResult<>(50, 1, 5, records);
        assertEquals(50, result.getTotal());
        assertEquals(1, result.getCurrent());
        assertEquals(5, result.getPages());
        assertSame(records, result.getRecords());
    }
}