package com.ikeu.components.redis.lock;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RedisDistributedLockTest {

    @SuppressWarnings("unchecked")
    private final RedisTemplate<String, String> redisTemplate = mock(RedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> valueOps = mock(ValueOperations.class);

    @Test
    void tryLock_shouldCallSetIfAbsent() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(eq("lock:test"), eq("uuid123"), any(Duration.class)))
                .thenReturn(true);

        RedisDistributedLock lock = new RedisDistributedLock(redisTemplate, "lock:");
        boolean acquired = lock.tryLock("test", "uuid123", Duration.ofSeconds(30));

        assertTrue(acquired);
        verify(valueOps).setIfAbsent(eq("lock:test"), eq("uuid123"), eq(Duration.ofSeconds(30)));
    }

    @Test
    void tryLock_shouldReturnFalseWhenNotAcquired() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(false);

        RedisDistributedLock lock = new RedisDistributedLock(redisTemplate, "lock:");
        boolean acquired = lock.tryLock("test", "uuid123", Duration.ofSeconds(30));

        assertFalse(acquired);
    }

    @Test
    void unlock_shouldExecuteLuaScript() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.execute(any(RedisScript.class), anyList(), anyString()))
                .thenReturn(1L);

        RedisDistributedLock lock = new RedisDistributedLock(redisTemplate, "lock:");
        lock.unlock("test", "uuid123");

        verify(redisTemplate).execute(
                any(RedisScript.class),
                eq(Collections.singletonList("lock:test")),
                eq("uuid123"));
    }

    @Test
    void unlock_shouldNotThrowWhenKeyOrValueNull() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        RedisDistributedLock lock = new RedisDistributedLock(redisTemplate, "lock:");
        assertDoesNotThrow(() -> lock.unlock(null, "value"));
        assertDoesNotThrow(() -> lock.unlock("key", null));
    }

    @Test
    void tryLock_shouldThrowWhenArgsNull() {
        RedisDistributedLock lock = new RedisDistributedLock(redisTemplate, "lock:");
        assertThrows(IllegalArgumentException.class,
                () -> lock.tryLock(null, "v", Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class,
                () -> lock.tryLock("k", null, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class,
                () -> lock.tryLock("k", "v", null));
    }

    @Test
    void defaultPrefix_shouldBeUsedWhenNull() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(true);

        RedisDistributedLock lock = new RedisDistributedLock(redisTemplate, null);
        lock.tryLock("test", "v", Duration.ofSeconds(1));

        verify(valueOps).setIfAbsent(startsWith("lock:"), anyString(), any(Duration.class));
    }
}
