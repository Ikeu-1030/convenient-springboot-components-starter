package com.ikeu.components.autoconfigure.trace;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration properties for request tracing.
 * <p>
 * Prefix: {@code ikeu.trace}
 */
@ConfigurationProperties(prefix = "ikeu.trace")
public class TraceProperties {

    /** Enable trace ID generation and MDC integration. Default: true. */
    private boolean enabled = true;

    /** HTTP header name for the trace ID (both request input and response output). */
    private String headerName = "X-Trace-Id";

    /** Whether to echo the trace ID back in the response header. */
    private boolean responseHeader = true;

    /** MDC key name used in logging configuration (e.g., {@code %X{traceId}}). */
    private String mdcKey = "traceId";

    // ── Getters / Setters ──

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public boolean isResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(boolean responseHeader) {
        this.responseHeader = responseHeader;
    }

    public String getMdcKey() {
        return mdcKey;
    }

    public void setMdcKey(String mdcKey) {
        this.mdcKey = mdcKey;
    }
}
