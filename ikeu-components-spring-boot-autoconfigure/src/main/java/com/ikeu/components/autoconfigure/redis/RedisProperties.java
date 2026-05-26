package com.ikeu.components.autoconfigure.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

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

    // ── Getters / Setters ──

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getLockPrefix() { return lockPrefix; }
    public void setLockPrefix(String lockPrefix) { this.lockPrefix = lockPrefix; }

    public boolean isUseJsonSerialization() { return useJsonSerialization; }
    public void setUseJsonSerialization(boolean useJsonSerialization) { this.useJsonSerialization = useJsonSerialization; }
}
