package com.ikeu.components.redis.lock;

import com.ikeu.components.redis.util.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * High-level lock and cache-protection helper.
 *
 * <p>Combines {@link RedisDistributedLock} and {@link RedisUtils} to provide:
 * <ul>
 *   <li><b>Functional auto-lock</b> — execute a block under a distributed lock</li>
 *   <li><b>Cache-penetration protection</b> — cache a null marker when DB returns null</li>
 *   <li><b>Cache-breakdown protection</b> — mutex lock so only one thread rebuilds a hot key</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * // Auto-lock
 * String result = lockHelper.execute("order:123", Duration.ofSeconds(30),
 *         () -> doCriticalWork());
 *
 * // Cache penetration protection
 * User user = lockHelper.getWithPenetrationProtection("user:1", User.class,
 *         () -> userMapper.selectById(1L),
 *         Duration.ofMinutes(10), Duration.ofSeconds(30));
 *
 * // Cache breakdown protection
 * User user = lockHelper.getWithBreakdownProtection("user:1", User.class,
 *         () -> userMapper.selectById(1L), Duration.ofMinutes(10));
 * }</pre>
 */
public class RedisLockHelper {

    private static final Logger log = LoggerFactory.getLogger(RedisLockHelper.class);

    /** Prefix for mutex lock keys used in breakdown protection. */
    private static final String MUTEX_PREFIX = "mutex:";

    private final RedisDistributedLock distributedLock;
    private final RedisUtils redisUtils;

    public RedisLockHelper(RedisDistributedLock distributedLock, RedisUtils redisUtils) {
        this.distributedLock = distributedLock;
        this.redisUtils = redisUtils;
    }

    // ── Functional auto-lock ────────────────────────────

    /**
     * Execute a task under a distributed lock. The lock is automatically
     * acquired before the task and released after (in {@code finally}).
     *
     * @param key     lock identifier
     * @param timeout max lock hold duration
     * @param task    the task to execute
     * @return the task's result, or null if the lock could not be acquired
     */
    public <T> T execute(String key, Duration timeout, Supplier<T> task) {
        String lockValue = UUID.randomUUID().toString();
        if (!distributedLock.tryLock(key, lockValue, timeout)) {
            log.debug("Failed to acquire lock for key: {}, skipping execution", key);
            return null;
        }
        try {
            return task.get();
        } finally {
            distributedLock.unlock(key, lockValue);
        }
    }

    /**
     * Execute a runnable under a distributed lock.
     *
     * @return true if the lock was acquired and the task executed
     */
    public boolean execute(String key, Duration timeout, Runnable task) {
        return execute(key, timeout, () -> {
            task.run();
            return Boolean.TRUE;
        }) != null;
    }

    // ── Cache penetration protection ────────────────────

    /**
     * Get from cache; on miss, query the database and populate the cache.
     * <p>
     * When the DB query returns {@code null}, a special null-marker is cached
     * with a shorter TTL to prevent repeated cache misses (penetration).
     *
     * @param cacheKey        Redis cache key
     * @param targetClass     expected return type
     * @param dbSupplier      fallback DB query
     * @param cacheTimeout    TTL for a real value
     * @param nullValueTimeout TTL for the null-marker (typically shorter)
     * @return cached or DB value, may be null
     */
    public <T> T getWithPenetrationProtection(String cacheKey, Class<T> targetClass,
                                               Supplier<T> dbSupplier,
                                               Duration cacheTimeout,
                                               Duration nullValueTimeout) {
        // 1. Try cache
        Object cached = redisUtils.getRaw(cacheKey);
        if (cached != null) {
            if (RedisUtils.NULL_MARKER.equals(cached)) {
                return null;
            }
            return redisUtils.get(cacheKey, targetClass);
        }

        // 2. Query DB
        T dbValue = dbSupplier.get();
        if (dbValue == null) {
            redisUtils.set(cacheKey, RedisUtils.NULL_MARKER, nullValueTimeout);
            return null;
        }

        // 3. Populate cache
        redisUtils.set(cacheKey, dbValue, cacheTimeout);
        return dbValue;
    }

    /**
     * Get with penetration protection using the same TTL for real values
     * and a default 30-second TTL for the null marker.
     */
    public <T> T getWithPenetrationProtection(String cacheKey, Class<T> targetClass,
                                               Supplier<T> dbSupplier,
                                               Duration cacheTimeout) {
        return getWithPenetrationProtection(cacheKey, targetClass, dbSupplier,
                cacheTimeout, Duration.ofSeconds(30));
    }

    // ── Cache breakdown protection ──────────────────────

    /**
     * Get from cache; on miss, use a distributed mutex lock to ensure
     * only one thread queries the DB and rebuilds the cache (breakdown protection).
     * <p>
     * Other threads waiting for the lock spin for up to {@code maxWait}
     * (default 5 seconds) and then fall back to querying the DB directly.
     *
     * @param cacheKey     Redis cache key
     * @param targetClass  expected return type
     * @param dbSupplier   fallback DB query
     * @param cacheTimeout TTL for the cached value
     * @return cached or DB value
     */
    public <T> T getWithBreakdownProtection(String cacheKey, Class<T> targetClass,
                                             Supplier<T> dbSupplier,
                                             Duration cacheTimeout) {
        return getWithBreakdownProtection(cacheKey, targetClass, dbSupplier,
                cacheTimeout, Duration.ofSeconds(5));
    }

    /**
     * Get with breakdown protection and custom max wait time.
     *
     * @param maxWait how long to spin-wait for another thread to rebuild the cache
     */
    public <T> T getWithBreakdownProtection(String cacheKey, Class<T> targetClass,
                                             Supplier<T> dbSupplier,
                                             Duration cacheTimeout,
                                             Duration maxWait) {
        // 1. Try cache
        T cached = redisUtils.get(cacheKey, targetClass);
        if (cached != null) {
            return cached;
        }

        // 2. Try to acquire rebuild lock
        String mutexKey = MUTEX_PREFIX + cacheKey;
        String lockValue = UUID.randomUUID().toString();
        long deadline = System.currentTimeMillis() + maxWait.toMillis();

        if (distributedLock.tryLock(mutexKey, lockValue, Duration.ofSeconds(10))) {
            try {
                // Double-check: another thread may have rebuilt while we waited
                T doubleCheck = redisUtils.get(cacheKey, targetClass);
                if (doubleCheck != null) {
                    return doubleCheck;
                }

                // 3. Query DB and rebuild
                T dbValue = dbSupplier.get();
                if (dbValue != null) {
                    redisUtils.set(cacheKey, dbValue, cacheTimeout);
                }
                return dbValue;
            } finally {
                distributedLock.unlock(mutexKey, lockValue);
            }
        }

        // 4. Could not acquire lock — spin-wait for the winner to rebuild
        while (System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            T retry = redisUtils.get(cacheKey, targetClass);
            if (retry != null) {
                return retry;
            }
        }

        // 5. Timeout — fall back to DB directly
        log.debug("Breakdown-protection fallback to DB for key: {}", cacheKey);
        T fallback = dbSupplier.get();
        if (fallback != null) {
            redisUtils.set(cacheKey, fallback, cacheTimeout);
        }
        return fallback;
    }
}
