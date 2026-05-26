package com.ikeu.components.autoconfigure.cors;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Configuration properties for CORS.
 * <p>
 * Prefix: {@code ikeu.cors}
 */
@ConfigurationProperties(prefix = "ikeu.cors")
public class CorsProperties {

    /** Enable CORS auto-configuration. Default: false (opt-in for security). */
    private boolean enabled = false;

    /** URL path pattern to apply CORS to. */
    private String pathPattern = "/**";

    /** Allowed origins. Default: "*" (all origins). */
    private List<String> allowedOrigins = Collections.singletonList("*");

    /** Allowed HTTP methods. */
    private List<String> allowedMethods = Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");

    /** Allowed request headers. */
    private List<String> allowedHeaders = Collections.singletonList("*");

    /** Headers exposed to the browser. */
    private List<String> exposedHeaders = new ArrayList<>();

    /** Whether to allow credentials (cookies, authorization headers). */
    private boolean allowCredentials = false;

    /** How long the preflight response can be cached. */
    private Duration maxAge = Duration.ofSeconds(1800);

    // ── Getters / Setters ──

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getPathPattern() { return pathPattern; }
    public void setPathPattern(String pathPattern) { this.pathPattern = pathPattern; }

    public List<String> getAllowedOrigins() { return allowedOrigins; }
    public void setAllowedOrigins(List<String> allowedOrigins) { this.allowedOrigins = allowedOrigins; }

    public List<String> getAllowedMethods() { return allowedMethods; }
    public void setAllowedMethods(List<String> allowedMethods) { this.allowedMethods = allowedMethods; }

    public List<String> getAllowedHeaders() { return allowedHeaders; }
    public void setAllowedHeaders(List<String> allowedHeaders) { this.allowedHeaders = allowedHeaders; }

    public List<String> getExposedHeaders() { return exposedHeaders; }
    public void setExposedHeaders(List<String> exposedHeaders) { this.exposedHeaders = exposedHeaders; }

    public boolean isAllowCredentials() { return allowCredentials; }
    public void setAllowCredentials(boolean allowCredentials) { this.allowCredentials = allowCredentials; }

    public Duration getMaxAge() { return maxAge; }
    public void setMaxAge(Duration maxAge) { this.maxAge = maxAge; }
}
