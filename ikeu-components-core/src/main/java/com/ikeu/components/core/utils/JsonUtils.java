package com.ikeu.components.core.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * JSON utilities wrapping a shared Jackson {@link ObjectMapper}.
 * <p>
 * Thread-safe — uses a {@code volatile} mapper with {@code synchronized} mutators
 * ({@link #setObjectMapper}, {@link #resetObjectMapper}).
 *
 * <h3>Default ObjectMapper configuration</h3>
 * <ul>
 *   <li>{@code NON_NULL} serialization inclusion — null fields omitted</li>
 *   <li>{@code FAIL_ON_UNKNOWN_PROPERTIES = false} — tolerant deserialization</li>
 *   <li>{@code WRITE_DATES_AS_TIMESTAMPS = false} — ISO string dates</li>
 *   <li>{@code SimpleDateFormat("yyyy-MM-dd HH:mm:ss")} — standard format</li>
 *   <li>{@code JavaTimeModule} with custom serializers for LocalDateTime, LocalDate, LocalTime</li>
 *   <li>{@code ToStringSerializer} for Long/long — prevents JS precision loss (&gt;2^53)</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * // Serialize
 * String json = JsonUtils.toJson(user);               // compact
 * String pretty = JsonUtils.toJsonPretty(user);        // indented
 * byte[] bytes = JsonUtils.toJsonBytes(user);
 *
 * // Deserialize
 * User user = JsonUtils.fromJson(json, User.class);
 * List<User> users = JsonUtils.fromJsonList(json, User.class);
 * Map<String, Object> map = JsonUtils.fromJsonMap(json);
 *
 * // Generic types
 * List<Order> orders = JsonUtils.fromJson(json, new TypeReference<List<Order>>() {});
 *
 * // DTO conversion via JSON round-trip
 * UserVo vo = JsonUtils.convert(entity, UserVo.class);
 *
 * // Validation
 * boolean valid = JsonUtils.isValidJson(input);  // true if parseable JSON
 * }</pre>
 *
 * <h3>Spring integration</h3>
 * When using {@code ikeu-components-spring-boot-starter}, the auto-configured
 * Spring ObjectMapper is synced to this class automatically. Use
 * {@link #getObjectMapper()} to obtain the current mapper; prefer the Spring
 * context's ObjectMapper in Spring-managed code.
 * <pre>{@code
 * // Manual sync (done automatically by JacksonCustomAutoConfiguration)
 * JsonUtils.setObjectMapper(springObjectMapper);
 * }</pre>
 *
 * @author ikeu
 * @since 1.0.0
 */
@Slf4j
public final class JsonUtils {

    private static volatile ObjectMapper mapper;

    static {
        mapper = createDefaultMapper();
    }

    private static ObjectMapper createDefaultMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // ── Serialization ──
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setDateFormat(new SimpleDateFormat(DateUtils.PATTERN_DATETIME));
        objectMapper.setTimeZone(TimeZone.getDefault());

        // ── Deserialization ──
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // ── Java 8 time module ──
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DateUtils.PATTERN_DATETIME);
        DateTimeFormatter df = DateTimeFormatter.ofPattern(DateUtils.PATTERN_DATE);
        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm:ss");
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(dtf));
        javaTimeModule.addSerializer(LocalDate.class,
                new LocalDateSerializer(df));
        javaTimeModule.addSerializer(LocalTime.class,
                new LocalTimeSerializer(tf));
        objectMapper.registerModule(javaTimeModule);

        // ── Long → String (prevents JS number precision loss) ──
        SimpleModule longModule = new SimpleModule();
        longModule.addSerializer(Long.class, ToStringSerializer.instance);
        longModule.addSerializer(long.class, ToStringSerializer.instance);
        objectMapper.registerModule(longModule);

        return objectMapper;
    }

    private JsonUtils() {
    }

    /**
     * Get the shared ObjectMapper for custom configuration.
     * <p>
     * Prefer obtaining the ObjectMapper from Spring context when in a Spring
     * application; use this method only in non-Spring contexts or static helpers.
     */
    public static ObjectMapper getObjectMapper() {
        return mapper;
    }

    /**
     * Replace the shared ObjectMapper with a custom one (e.g. from Spring context).
     * This allows the static utility to stay in sync with the application's
     * auto-configured ObjectMapper.
     */
    public static synchronized void setObjectMapper(ObjectMapper customMapper) {
        if (customMapper != null) {
            mapper = customMapper;
            log.info("JsonUtils ObjectMapper replaced with custom instance");
        }
    }

    /**
     * Reset to the default ObjectMapper, discarding any custom replacement.
     */
    public static synchronized void resetObjectMapper() {
        mapper = createDefaultMapper();
        log.info("JsonUtils ObjectMapper reset to default");
    }

    // ──────────────────────────────────────────────
    // Serialization
    // ──────────────────────────────────────────────

    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON", e);
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    /** Serialize to pretty-printed JSON. */
    public static String toJsonPretty(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to pretty JSON", e);
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    public static byte[] toJsonBytes(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return mapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON bytes", e);
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    // ──────────────────────────────────────────────
    // Deserialization
    // ──────────────────────────────────────────────

    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to {}", clazz.getName(), e);
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    /** Deserialize to List of the given type. */
    public static <T> List<T> fromJsonList(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, clazz);
            return mapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to List<{}>", clazz.getName(), e);
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    /** Deserialize using a TypeReference for generic types (e.g. Map, nested generics). */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return mapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON with TypeReference", e);
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    /** Deserialize JSON to Map&lt;String, Object&gt;. */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> fromJsonMap(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return mapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to Map", e);
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    // ──────────────────────────────────────────────
    // Conversion helpers
    // ──────────────────────────────────────────────

    /**
     * Convert an object to another type via JSON serialization round-trip.
     * Useful for converting between DTOs, VOs, etc.
     */
    public static <T> T convert(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        return fromJson(toJson(source), targetClass);
    }

    /**
     * Convert an object to another type via JSON round-trip, using TypeReference.
     */
    public static <T> T convert(Object source, TypeReference<T> typeReference) {
        if (source == null) {
            return null;
        }
        return fromJson(toJson(source), typeReference);
    }

    // ──────────────────────────────────────────────
    // Validation
    // ──────────────────────────────────────────────

    /** Check if a string is valid JSON. */
    public static boolean isValidJson(String json) {
        if (json == null || json.isBlank()) {
            return false;
        }
        try {
            mapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
}