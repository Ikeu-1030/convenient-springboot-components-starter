package com.ikeu.components.autoconfigure.xss;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for XSS protection.
 * <p>
 * Prefix: {@code ikeu.xss}
 */
@ConfigurationProperties(prefix = "ikeu.xss")
public class XssProperties {

    /** Enable XSS protection. Default: false (opt-in). */
    private boolean enabled = false;

    /** XSS handling mode: {@code escape} (HTML-encode) or {@code strip} (remove tags). */
    private XssMode mode = XssMode.ESCAPE;

    /** Ant-style URL path patterns to exclude from XSS filtering. */
    private List<String> excludePaths = new ArrayList<>();

    public enum XssMode {
        /** Encode special HTML characters (e.g. {@code <} → {@code &lt;}). */
        ESCAPE,
        /** Remove HTML tags using regex (e.g. {@code <script>} → removed). */
        STRIP
    }

    // ── Getters / Setters ──

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public XssMode getMode() { return mode; }
    public void setMode(XssMode mode) { this.mode = mode; }

    public List<String> getExcludePaths() { return excludePaths; }
    public void setExcludePaths(List<String> excludePaths) { this.excludePaths = excludePaths; }
}
