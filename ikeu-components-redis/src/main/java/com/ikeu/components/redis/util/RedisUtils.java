package com.ikeu.components.redis.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Convenience wrapper around {@link RedisTemplate} with automatic JSON
 * serialization/deserialization.
 *
 * <p>All {@code get} methods accept a target {@link Class} and perform
 * type-safe conversion. Collections (List, Set) automatically convert
 * each element to the target type.
 *
 * <pre>{@code
 * redisUtils.set("user:1", user, Duration.ofHours(1));
 * User user = redisUtils.get("user:1", User.class);
 * redisUtils.setList("users", userList);
 * List<User> users = redisUtils.getList("users", User.class);
 * redisUtils.incr("counter");
 * }</pre>
 */
public class RedisUtils {

    private static final Logger log = LoggerFactory.getLogger(RedisUtils.class);

    /** Sentinel value stored in Redis to mark a cached null (penetration protection). */
    public static final String NULL_MARKER = "__NULL__";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = buildObjectMapper();
    }

    // ── Basic set / get ──────────────────────────────────

    /**
     * Store a value with no expiration.
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * Store a value with TTL.
     */
    public void set(String key, Object value, Duration timeout) {
        redisTemplate.opsForValue().set(key, value, timeout);
    }

    /**
     * Store a value with TTL in seconds.
     */
    public void set(String key, Object value, long timeoutSeconds) {
        redisTemplate.opsForValue().set(key, value, timeoutSeconds, TimeUnit.SECONDS);
    }

    /**
     * Get and deserialize to target type. Returns null if key does not exist
     * or stores the {@link #NULL_MARKER}.
     */
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) return null;
        if (NULL_MARKER.equals(value)) return null;
        if (clazz.isInstance(value)) return clazz.cast(value);
        return objectMapper.convertValue(value, clazz);
    }

    /**
     * Get raw value (may be LinkedHashMap/ArrayList from Jackson deserialization).
     */
    public Object getRaw(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // ── List operations ─────────────────────────────────

    /**
     * Store a list (serializes the entire list as JSON array).
     */
    public <T> void setList(String key, List<T> list) {
        redisTemplate.opsForValue().set(key, list);
    }

    /**
     * Store a list with TTL.
     */
    public <T> void setList(String key, List<T> list, Duration timeout) {
        redisTemplate.opsForValue().set(key, list, timeout);
    }

    /**
     * Get a list and convert each element to target type.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key, Class<T> elementClass) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) return Collections.emptyList();
        if (!(value instanceof List)) {
            log.warn("Key {} is not a list, got {}", key, value.getClass());
            return Collections.emptyList();
        }
        List<?> rawList = (List<?>) value;
        if (rawList.isEmpty()) return Collections.emptyList();
        // If elements are already the right type, cast; otherwise convert
        if (elementClass.isInstance(rawList.get(0))) {
            return (List<T>) rawList;
        }
        return rawList.stream()
                .map(e -> objectMapper.convertValue(e, elementClass))
                .collect(Collectors.toList());
    }

    // ── Map operations ──────────────────────────────────

    /**
     * Store a map as a Redis hash in a single key (JSON-serialized value).
     */
    public void setMap(String key, Map<String, Object> map) {
        redisTemplate.opsForValue().set(key, map);
    }

    /**
     * Store a map with TTL.
     */
    public void setMap(String key, Map<String, Object> map, Duration timeout) {
        redisTemplate.opsForValue().set(key, map, timeout);
    }

    /**
     * Get a map stored at key.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMap(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) return Collections.emptyMap();
        if (value instanceof Map) return (Map<String, Object>) value;
        log.warn("Key {} is not a map, got {}", key, value.getClass());
        return Collections.emptyMap();
    }

    // ── Set operations ──────────────────────────────────

    /**
     * Store a set (serialized as JSON array).
     */
    public <T> void setSet(String key, Set<T> set) {
        redisTemplate.opsForValue().set(key, set);
    }

    /**
     * Store a set with TTL.
     */
    public <T> void setSet(String key, Set<T> set, Duration timeout) {
        redisTemplate.opsForValue().set(key, set, timeout);
    }

    /**
     * Get a set and convert each element to target type.
     */
    @SuppressWarnings("unchecked")
    public <T> Set<T> getSet(String key, Class<T> elementClass) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) return Collections.emptySet();
        if (!(value instanceof Collection)) {
            log.warn("Key {} is not a collection, got {}", key, value.getClass());
            return Collections.emptySet();
        }
        Collection<?> rawColl = (Collection<?>) value;
        if (rawColl.isEmpty()) return Collections.emptySet();
        return rawColl.stream()
                .map(e -> objectMapper.convertValue(e, elementClass))
                .collect(Collectors.toSet());
    }

    // ── Key management ──────────────────────────────────

    /** Check if key exists. */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /** Set expiration for an existing key. */
    public boolean expire(String key, Duration timeout) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout));
    }

    /** Set expiration in seconds for an existing key. */
    public boolean expire(String key, long timeoutSeconds) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeoutSeconds, TimeUnit.SECONDS));
    }

    /** Get remaining TTL in seconds. Returns -1 if no expiration, -2 if key doesn't exist. */
    public long getExpire(String key) {
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire != null ? expire : -2;
    }

    /** Delete a single key. */
    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /** Delete multiple keys. Returns number of keys deleted. */
    public long delete(Collection<String> keys) {
        Long count = redisTemplate.delete(keys);
        return count != null ? count : 0;
    }

    /**
     * Set a key only if it does not already exist (SETNX).
     * @return true if the key was set
     */
    public boolean setIfAbsent(String key, Object value) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value));
    }

    /**
     * Set a key only if it does not exist, with TTL.
     * @return true if the key was set
     */
    public boolean setIfAbsent(String key, Object value, Duration timeout) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, timeout));
    }

    // ── Atomic operations ───────────────────────────────

    /** Increment by 1. */
    public long incr(String key) {
        Long result = redisTemplate.opsForValue().increment(key);
        return result != null ? result : 0;
    }

    /** Increment by delta. */
    public long incrBy(String key, long delta) {
        Long result = redisTemplate.opsForValue().increment(key, delta);
        return result != null ? result : 0;
    }

    /** Decrement by 1. */
    public long decr(String key) {
        Long result = redisTemplate.opsForValue().decrement(key);
        return result != null ? result : 0;
    }

    /** Decrement by delta. */
    public long decrBy(String key, long delta) {
        Long result = redisTemplate.opsForValue().decrement(key, delta);
        return result != null ? result : 0;
    }

    // ── Hash operations ─────────────────────────────────

    /** Set a hash field. */
    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /** Set multiple hash fields at once. */
    public void hSetAll(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /** Get a hash field and convert to target type. */
    @SuppressWarnings("unchecked")
    public <T> T hGet(String key, String field, Class<T> clazz) {
        Object value = redisTemplate.opsForHash().get(key, field);
        if (value == null) return null;
        if (clazz.isInstance(value)) return (T) value;
        return objectMapper.convertValue(value, clazz);
    }

    /** Get all fields of a hash. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> hGetAll(String key) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return result;
    }

    /** Check if a hash field exists. */
    public boolean hHasKey(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    /** Delete hash fields. */
    public long hDelete(String key, String... fields) {
        return redisTemplate.opsForHash().delete(key, (Object[]) fields);
    }

    // ── Internal ────────────────────────────────────────

    private static ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /**
     * Serialize an object to JSON string. Used by {@link RedisLockHelper}.
     */
    String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize value", e);
        }
    }

    /**
     * Deserialize JSON string to target type. Used by {@link RedisLockHelper}.
     */
    <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize to " + clazz.getName(), e);
        }
    }
}
