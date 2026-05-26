package com.ikeu.components.redis.lock;

import com.ikeu.components.redis.util.RedisUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class RedisLockHelperTest {

    private RedisDistributedLock distributedLock;
    private RedisUtils redisUtils;
    private RedisLockHelper lockHelper;

    @BeforeEach
    void setUp() {
        distributedLock = mock(RedisDistributedLock.class);
        redisUtils = mock(RedisUtils.class);
        lockHelper = new RedisLockHelper(distributedLock, redisUtils);
    }

    // ── Functional auto-lock ──

    @Test
    void execute_supplier_shouldLockAndUnlock() {
        when(distributedLock.tryLock(eq("task"), anyString(), any(Duration.class)))
                .thenReturn(true);

        String result = lockHelper.execute("task", Duration.ofSeconds(10),
                () -> "done");

        assertEquals("done", result);
        verify(distributedLock).tryLock(eq("task"), anyString(), eq(Duration.ofSeconds(10)));
        verify(distributedLock).unlock(eq("task"), anyString());
    }

    @Test
    void execute_supplier_lockFailed_shouldReturnNull() {
        when(distributedLock.tryLock(anyString(), anyString(), any(Duration.class)))
                .thenReturn(false);

        String result = lockHelper.execute("task", Duration.ofSeconds(10),
                () -> "should not run");

        assertNull(result);
        verify(distributedLock, never()).unlock(anyString(), anyString());
    }

    @Test
    void execute_runnable_shouldReturnTrue() {
        when(distributedLock.tryLock(anyString(), anyString(), any(Duration.class)))
                .thenReturn(true);

        boolean executed = lockHelper.execute("task", Duration.ofSeconds(10),
                () -> {});

        assertTrue(executed);
    }

    @Test
    void execute_shouldUnlockOnException() {
        when(distributedLock.tryLock(anyString(), anyString(), any(Duration.class)))
                .thenReturn(true);

        assertThrows(RuntimeException.class, () ->
                lockHelper.execute("task", Duration.ofSeconds(10), () -> {
                    throw new RuntimeException("boom");
                }));

        verify(distributedLock).unlock(anyString(), anyString());
    }

    // ── Cache penetration protection ──

    @Test
    void penetration_cacheHit_shouldReturnCachedValue() {
        String cacheKey = "user:1";
        when(redisUtils.getRaw(cacheKey)).thenReturn(new Object()); // non-null, non-marker
        when(redisUtils.get(cacheKey, String.class)).thenReturn("cached");

        String result = lockHelper.getWithPenetrationProtection(cacheKey, String.class,
                () -> { throw new AssertionError("should not call DB"); },
                Duration.ofMinutes(5));

        assertEquals("cached", result);
    }

    @Test
    void penetration_nullMarker_shouldReturnNull() {
        when(redisUtils.getRaw("user:1")).thenReturn(RedisUtils.NULL_MARKER);

        String result = lockHelper.getWithPenetrationProtection("user:1", String.class,
                () -> "should not call",
                Duration.ofMinutes(5), Duration.ofSeconds(30));

        assertNull(result);
    }

    @Test
    void penetration_dbReturnsNull_shouldCacheMarker() {
        when(redisUtils.getRaw("user:1")).thenReturn(null);

        String result = lockHelper.getWithPenetrationProtection("user:1", String.class,
                () -> null,
                Duration.ofMinutes(5), Duration.ofSeconds(30));

        assertNull(result);
        verify(redisUtils).set("user:1", RedisUtils.NULL_MARKER, Duration.ofSeconds(30));
    }

    @Test
    void penetration_dbReturnsValue_shouldCacheIt() {
        when(redisUtils.getRaw("user:1")).thenReturn(null);

        String result = lockHelper.getWithPenetrationProtection("user:1", String.class,
                () -> "dbValue",
                Duration.ofMinutes(5), Duration.ofSeconds(30));

        assertEquals("dbValue", result);
        verify(redisUtils).set("user:1", "dbValue", Duration.ofMinutes(5));
    }

    // ── Cache breakdown protection ──

    @Test
    void breakdown_cacheHit_shouldReturnImmediately() {
        when(redisUtils.get("hot:1", String.class)).thenReturn("cached");

        String result = lockHelper.getWithBreakdownProtection("hot:1", String.class,
                () -> { throw new AssertionError("should not call DB"); },
                Duration.ofMinutes(5));

        assertEquals("cached", result);
        verify(distributedLock, never()).tryLock(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void breakdown_cacheMiss_lockAcquired_shouldRebuild() {
        when(redisUtils.get("hot:1", String.class)).thenReturn(null, null, "rebuilt");
        when(distributedLock.tryLock(eq("mutex:hot:1"), anyString(), any(Duration.class)))
                .thenReturn(true);

        String result = lockHelper.getWithBreakdownProtection("hot:1", String.class,
                () -> "dbValue",
                Duration.ofMinutes(5));

        assertEquals("dbValue", result);
        verify(redisUtils).set("hot:1", "dbValue", Duration.ofMinutes(5));
        verify(distributedLock).unlock(eq("mutex:hot:1"), anyString());
    }

    @Test
    void breakdown_doubleCheckAfterLock_shouldReturnCachedValue() {
        when(redisUtils.get("hot:1", String.class))
                .thenReturn(null)       // first check: miss
                .thenReturn("rebuilt"); // double-check after lock: hit
        when(distributedLock.tryLock(anyString(), anyString(), any(Duration.class)))
                .thenReturn(true);

        String result = lockHelper.getWithBreakdownProtection("hot:1", String.class,
                () -> "should not call DB",
                Duration.ofMinutes(5));

        assertEquals("rebuilt", result);
        // DB supplier should NOT have been called due to double-check hit
        verify(redisUtils, never()).set(anyString(), anyString(), any(Duration.class));
    }
}
