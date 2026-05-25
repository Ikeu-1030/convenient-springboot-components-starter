package com.ikeu.components.autoconfigure.http;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for {@link com.ikeu.components.core.utils.HttpClientUtil}.
 * <p>
 * All properties are under the {@code ikeu.http-client} prefix.
 *
 * <pre>{@code
 * ikeu:
 *   http-client:
 *     connect-timeout: 10s
 *     request-timeout: 30s
 *     download-timeout: 5m
 *     proxy-host: 127.0.0.1
 *     proxy-port: 8888
 * }</pre>
 * @author ikeu
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "ikeu.http-client")
public class HttpClientProperties {

    /** Connection timeout. */
    private java.time.Duration connectTimeout = java.time.Duration.ofSeconds(10);

    /** Per-request timeout. */
    private java.time.Duration requestTimeout = java.time.Duration.ofSeconds(30);

    /** Download timeout (applies to {@code download()} method). */
    private java.time.Duration downloadTimeout = java.time.Duration.ofMinutes(5);

    /** Proxy host. Only used when both host and port are set. */
    private String proxyHost;

    /** Proxy port. */
    private Integer proxyPort;

    // ── Getters / Setters ──

    public java.time.Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(java.time.Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public java.time.Duration getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(java.time.Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public java.time.Duration getDownloadTimeout() {
        return downloadTimeout;
    }

    public void setDownloadTimeout(java.time.Duration downloadTimeout) {
        this.downloadTimeout = downloadTimeout;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }
}