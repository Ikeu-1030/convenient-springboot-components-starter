package com.ikeu.components.autoconfigure.jackson;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Jackson JSON serialization.
 * <p>
 * All properties are under the {@code ikeu.jackson} prefix.
 * These override the sensible defaults provided by {@link JacksonCustomAutoConfiguration}.
 *
 * <pre>{@code
 * ikeu:
 *   jackson:
 *     date-pattern: yyyy-MM-dd HH:mm:ss
 *     long-as-string: true
 *     serialization-inclusion: non_null
 *     time-zone: Asia/Shanghai
 * }</pre>
 * @author ikeu
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "ikeu.jackson")
public class JacksonProperties {

    /** Default date format pattern. */
    private String datePattern = "yyyy-MM-dd HH:mm:ss";

    /** Serialize Long/long as String to prevent JavaScript precision loss. */
    private boolean longAsString = true;

    /**
     * Serialization inclusion policy.
     * One of: non_null, non_default, non_absent, always.
     */
    private String serializationInclusion = "non_null";

    /** Timezone for date formatting, e.g. "Asia/Shanghai" or "UTC". */
    private String timeZone;

    // ── Getters / Setters ──

    public String getDatePattern() {
        return datePattern;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    public boolean isLongAsString() {
        return longAsString;
    }

    public void setLongAsString(boolean longAsString) {
        this.longAsString = longAsString;
    }

    public String getSerializationInclusion() {
        return serializationInclusion;
    }

    public void setSerializationInclusion(String serializationInclusion) {
        this.serializationInclusion = serializationInclusion;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}