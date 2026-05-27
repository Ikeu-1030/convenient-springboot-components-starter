package com.ikeu.components.autoconfigure.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration properties for Redis customization.
 * <p>
 * Prefix: {@code ikeu.redis}
 */
@ConfigurationProperties(prefix = "ikeu.redis")
public class RedisProperties {

    /** Enable Redis auto-configuration. Default: false (opt-in). */
    private boolean enabled = false;

    /** Key prefix for distributed lock keys. */
    private String lockPrefix = "ikeu:lock:";

    /** Whether to customize RedisTemplate with JSON serialization. */
    private boolean useJsonSerialization = true;

    // ── Cache configuration ──

    /** Default TTL for cache entries. */
    private Duration cacheDefaultTtl = Duration.ofMinutes(30);

    /** TTL for null value caching (cache penetration protection). */
    private Duration cacheNullTtl = Duration.ofMinutes(5);

    /** Key prefix applied to all cache keys. */
    private String cacheKeyPrefix = "ikeu:cache:";

    /** Whether to prefix cache keys with {@link #cacheKeyPrefix}. */
    private boolean cacheUseKeyPrefix = true;

    /** Whether to cache null values to prevent cache penetration. */
    private boolean cacheCacheNullValues = true;

    // ── Getters / Setters ──

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getLockPrefix() { return lockPrefix; }
    public void setLockPrefix(String lockPrefix) { this.lockPrefix = lockPrefix; }

    public boolean isUseJsonSerialization() { return useJsonSerialization; }
    public void setUseJsonSerialization(boolean useJsonSerialization) { this.useJsonSerialization = useJsonSerialization; }

    public Duration getCacheDefaultTtl() { return cacheDefaultTtl; }
    public void setCacheDefaultTtl(Duration cacheDefaultTtl) { this.cacheDefaultTtl = cacheDefaultTtl; }

    public Duration getCacheNullTtl() { return cacheNullTtl; }
    public void setCacheNullTtl(Duration cacheNullTtl) { this.cacheNullTtl = cacheNullTtl; }

    public String getCacheKeyPrefix() { return cacheKeyPrefix; }
    public void setCacheKeyPrefix(String cacheKeyPrefix) { this.cacheKeyPrefix = cacheKeyPrefix; }

    public boolean isCacheUseKeyPrefix() { return cacheUseKeyPrefix; }
    public void setCacheUseKeyPrefix(boolean cacheUseKeyPrefix) { this.cacheUseKeyPrefix = cacheUseKeyPrefix; }

    public boolean isCacheCacheNullValues() { return cacheCacheNullValues; }
    public void setCacheCacheNullValues(boolean cacheCacheNullValues) { this.cacheCacheNullValues = cacheCacheNullValues; }
}
