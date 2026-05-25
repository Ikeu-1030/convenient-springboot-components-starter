package com.ikeu.components.core.utils;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeIdGeneratorTest {

    private final SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);

    @Test
    void nextId_positive() {
        assertTrue(generator.nextId() > 0);
    }

    @Test
    void nextId_uniqueOverBatch() {
        int count = 10_000;
        Set<Long> ids = new HashSet<>(count);
        for (int i = 0; i < count; i++) {
            ids.add(generator.nextId());
        }
        assertEquals(count, ids.size(), "All generated IDs must be unique");
    }

    @Test
    void nextId_monotonicallyIncreasing() {
        long prev = generator.nextId();
        for (int i = 0; i < 1000; i++) {
            long next = generator.nextId();
            assertTrue(next > prev, "IDs must be monotonically increasing");
            prev = next;
        }
    }

    @Test
    void constructor_invalidWorkerIdLow() {
        assertThrows(IllegalArgumentException.class,
                () -> new SnowflakeIdGenerator(-1, 0));
    }

    @Test
    void constructor_invalidWorkerIdHigh() {
        assertThrows(IllegalArgumentException.class,
                () -> new SnowflakeIdGenerator(32, 0));
    }

    @Test
    void constructor_invalidDatacenterIdLow() {
        assertThrows(IllegalArgumentException.class,
                () -> new SnowflakeIdGenerator(0, -1));
    }

    @Test
    void constructor_invalidDatacenterIdHigh() {
        assertThrows(IllegalArgumentException.class,
                () -> new SnowflakeIdGenerator(0, 32));
    }

    @Test
    void constructor_boundaryZero() {
        SnowflakeIdGenerator g = new SnowflakeIdGenerator(0, 0);
        assertTrue(g.nextId() > 0);
    }

    @Test
    void constructor_boundaryMax() {
        SnowflakeIdGenerator g = new SnowflakeIdGenerator(31, 31);
        assertTrue(g.nextId() > 0);
    }
}