package com.ikeu.components.core.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * Snowflake distributed ID generator — 64-bit unique, roughly time-sorted IDs.
 * <p>
 * Thread-safe via {@code synchronized} on {@code nextId()}. Handles clock drift
 * by blocking up to 10ms; if the clock is still behind, throws
 * {@link IllegalStateException}.
 *
 * <pre>
 * Structure (64 bits):
 * 1  bit  - unused (sign bit, always 0)
 * 41 bits - timestamp (ms since 2024-01-01T00:00:00Z)
 * 5  bits - datacenterId (0–31)
 * 5  bits - workerId (0–31)
 * 12 bits - sequence number (0–4095 per ms)
 * </pre>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);
 * long id = generator.nextId();  // e.g. 174567890123456789
 * }</pre>
 *
 * <h3>Caveats</h3>
 * <ul>
 *   <li>datacenterId and workerId must be unique across all nodes in a deployment</li>
 *   <li>The host's clock must not be set backwards while the generator is running</li>
 * </ul>
 *
 * @author ikeu
 * @since 1.0.0
 */
@Slf4j
public class SnowflakeIdGenerator {

    /** Custom epoch (2024-01-01 00:00:00 UTC). */
    private static final long EPOCH = 1704067200000L;

    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long SEQUENCE_BITS = 12L;

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private final long workerId;
    private final long datacenterId;

    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException(
                    "workerId must be between 0 and " + MAX_WORKER_ID);
        }
        if (datacenterId < 0 || datacenterId > MAX_DATACENTER_ID) {
            throw new IllegalArgumentException(
                    "datacenterId must be between 0 and " + MAX_DATACENTER_ID);
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * Generate the next unique ID. Blocks if the clock moves backwards to prevent duplicates.
     */
    public synchronized long nextId() {
        long timestamp = currentTimeMillis();

        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            log.warn("Clock moved backwards by {}ms. Blocking until caught up.", offset);
            try {
                Thread.sleep(offset);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted during clock-drift wait", e);
            }
            timestamp = currentTimeMillis();
            if (timestamp < lastTimestamp) {
                throw new IllegalStateException(
                        "Clock still behind after waiting. Generated IDs may collide.");
            }
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimeMillis();
        }
        return timestamp;
    }

    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
