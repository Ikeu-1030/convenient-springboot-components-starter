package com.ikeu.components.redis.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Redis-based distributed lock using SET NX EX for atomic acquisition
 * and Lua scripting for safe release (verifies value ownership before deleting).
 *
 * <p>Usage:
 * <pre>{@code
 * String lockValue = UUID.randomUUID().toString();
 * try {
 *     if (lock.tryLock("order:123", lockValue, Duration.ofSeconds(30))) {
 *         // critical section
 *     }
 * } finally {
 *     lock.unlock("order:123", lockValue);
 * }
 * }</pre>
 */
public class RedisDistributedLock {

    private static final Logger log = LoggerFactory.getLogger(RedisDistributedLock.class);

    private static final String UNLOCK_SCRIPT = """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            else
                return 0
            end
            """;

    private static final RedisScript<Long> UNLOCK_REDIS_SCRIPT =
            RedisScript.of(UNLOCK_SCRIPT, Long.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final String keyPrefix;

    /**
     * @param redisTemplate RedisTemplate for string operations
     * @param keyPrefix     prefix prepended to all lock keys
     */
    public RedisDistributedLock(RedisTemplate<String, String> redisTemplate, String keyPrefix) {
        this.redisTemplate = redisTemplate;
        this.keyPrefix = (keyPrefix != null && !keyPrefix.isEmpty()) ? keyPrefix : "lock:";
    }

    /**
     * Try to acquire the lock immediately. Returns false if already held.
     *
     * @param key     lock identifier (keyPrefix is prepended)
     * @param value   unique value (e.g. UUID) used to verify ownership on unlock
     * @param timeout maximum hold duration before automatic expiry
     * @return true if lock acquired
     */
    public boolean tryLock(String key, String value, Duration timeout) {
        if (key == null || value == null || timeout == null) {
            throw new IllegalArgumentException("key, value, and timeout must not be null");
        }
        String fullKey = keyPrefix + key;
        Boolean result = redisTemplate.opsForValue().setIfAbsent(fullKey, value, timeout);
        return Boolean.TRUE.equals(result);
    }

    /**
     * Block until the lock is acquired, polling every 100ms.
     *
     * @param key     lock identifier
     * @param value   unique value for ownership verification
     * @param timeout maximum hold duration
     */
    public void lock(String key, String value, Duration timeout) {
        while (!tryLock(key, value, timeout)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Lock acquisition interrupted for key: {}", key);
                break;
            }
        }
    }

    /**
     * Release the lock atomically. Only releases if the value matches
     * (prevents accidentally releasing another thread's lock).
     *
     * @param key   lock identifier
     * @param value the same unique value used when acquiring
     */
    public void unlock(String key, String value) {
        if (key == null || value == null) {
            return;
        }
        String fullKey = keyPrefix + key;
        List<String> keys = Collections.singletonList(fullKey);
        Long result = redisTemplate.execute(UNLOCK_REDIS_SCRIPT, keys, value);
        if (result != null && result == 0) {
            log.debug("Unlock skipped for key {}: value mismatch or key expired", key);
        }
    }
}
