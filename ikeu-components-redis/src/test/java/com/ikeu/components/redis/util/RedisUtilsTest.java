package com.ikeu.components.redis.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class RedisUtilsTest {

    private RedisTemplate<String, Object> redisTemplate;
    private ValueOperations<String, Object> valueOps;
    private HashOperations<String, Object, Object> hashOps;
    private RedisUtils redisUtils;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        hashOps = mock(HashOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.opsForHash()).thenReturn(hashOps);

        redisUtils = new RedisUtils(redisTemplate);
    }

    // ── Basic set / get ──

    @Test
    void set_shouldCallOpsForValueSet() {
        redisUtils.set("key", "hello");
        verify(valueOps).set("key", "hello");
    }

    @Test
    void setWithTimeout_shouldCallOpsForValueSet() {
        Duration ttl = Duration.ofMinutes(5);
        redisUtils.set("key", "hello", ttl);
        verify(valueOps).set("key", "hello", ttl);
    }

    @Test
    void get_shouldReturnDeserializedValue() {
        User user = new User("John");
        when(valueOps.get("user:1")).thenReturn(user);

        User result = redisUtils.get("user:1", User.class);
        assertEquals("John", result.getName());
    }

    @Test
    void get_nullMarker_shouldReturnNull() {
        when(valueOps.get("key")).thenReturn(RedisUtils.NULL_MARKER);
        assertNull(redisUtils.get("key", String.class));
    }

    @Test
    void get_keyNotFound_shouldReturnNull() {
        when(valueOps.get("key")).thenReturn(null);
        assertNull(redisUtils.get("key", String.class));
    }

    // ── List operations ──

    @Test
    void getList_shouldReturnConvertedList() {
        List<Map<String, Object>> rawList = List.of(
                Map.of("name", "John"),
                Map.of("name", "Jane"));
        when(valueOps.get("users")).thenReturn(rawList);

        List<User> result = redisUtils.getList("users", User.class);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getName());
        assertEquals("Jane", result.get(1).getName());
    }

    @Test
    void getList_keyNotFound_shouldReturnEmptyList() {
        when(valueOps.get("key")).thenReturn(null);
        assertTrue(redisUtils.getList("key", User.class).isEmpty());
    }

    // ── Map operations ──

    @Test
    void getMap_shouldReturnMap() {
        Map<String, Object> data = Map.of("name", "John", "age", 30);
        when(valueOps.get("key")).thenReturn(data);

        Map<String, Object> result = redisUtils.getMap("key");
        assertEquals("John", result.get("name"));
        assertEquals(30, result.get("age"));
    }

    // ── Key management ──

    @Test
    void hasKey_shouldReturnTrue() {
        when(redisTemplate.hasKey("key")).thenReturn(true);
        assertTrue(redisUtils.hasKey("key"));
    }

    @Test
    void hasKey_shouldReturnFalse() {
        when(redisTemplate.hasKey("key")).thenReturn(false);
        assertFalse(redisUtils.hasKey("key"));
    }

    @Test
    void expire_shouldReturnTrue() {
        when(redisTemplate.expire(eq("key"), any(Duration.class))).thenReturn(true);
        assertTrue(redisUtils.expire("key", Duration.ofMinutes(1)));
    }

    @Test
    void getExpire_shouldReturnTtl() {
        when(redisTemplate.getExpire("key", java.util.concurrent.TimeUnit.SECONDS))
                .thenReturn(120L);
        assertEquals(120L, redisUtils.getExpire("key"));
    }

    @Test
    void delete_singleKey_shouldReturnTrue() {
        when(redisTemplate.delete("key")).thenReturn(true);
        assertTrue(redisUtils.delete("key"));
    }

    @Test
    void delete_multipleKeys_shouldReturnCount() {
        when(redisTemplate.delete(anyCollection())).thenReturn(3L);
        assertEquals(3L, redisUtils.delete(List.of("a", "b", "c")));
    }

    // ── Atomic operations ──

    @Test
    void incr_shouldReturnIncrementedValue() {
        when(valueOps.increment("counter")).thenReturn(42L);
        assertEquals(42L, redisUtils.incr("counter"));
    }

    @Test
    void incrBy_shouldReturnIncrementedValue() {
        when(valueOps.increment("counter", 5L)).thenReturn(10L);
        assertEquals(10L, redisUtils.incrBy("counter", 5));
    }

    @Test
    void decr_shouldReturnDecrementedValue() {
        when(valueOps.decrement("counter")).thenReturn(0L);
        assertEquals(0L, redisUtils.decr("counter"));
    }

    // ── Hash operations ──

    @Test
    void hSet_shouldCallPut() {
        redisUtils.hSet("hash", "field", "value");
        verify(hashOps).put("hash", "field", "value");
    }

    @Test
    void hGet_shouldReturnValue() {
        when(hashOps.get("hash", "field")).thenReturn("hello");
        assertEquals("hello", redisUtils.hGet("hash", "field", String.class));
    }

    @Test
    void hHasKey_shouldReturnTrue() {
        when(hashOps.hasKey("hash", "field")).thenReturn(true);
        assertTrue(redisUtils.hHasKey("hash", "field"));
    }

    // ── Null marker ──

    @Test
    void setIfAbsent_shouldReturnTrue() {
        when(valueOps.setIfAbsent(eq("key"), eq("value"), any(Duration.class)))
                .thenReturn(true);
        assertTrue(redisUtils.setIfAbsent("key", "value", Duration.ofMinutes(1)));
    }


    // ── Test bean ──

    public static class User {
        private String name;
        public User() {}
        public User(String name) { this.name = name; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
