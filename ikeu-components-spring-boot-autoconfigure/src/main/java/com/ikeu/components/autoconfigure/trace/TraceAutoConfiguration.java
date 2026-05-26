package com.ikeu.components.autoconfigure.trace;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for request tracing via {@link TraceIdFilter}.
 * <p>
 * Enabled by default. The trace ID is injected into SLF4J {@code MDC} so log patterns
 * can include it (e.g., {@code %X{traceId}}). The filter runs early in the chain
 * (order = {@code Integer.MIN_VALUE + 100}) after XSS filtering.
 *
 * <h3>Configuration</h3>
 * <pre>{@code
 * ikeu:
 *   trace:
 *     enabled: true          # default
 *     header-name: X-Trace-Id
 *     response-header: true
 *     mdc-key: traceId
 * }</pre>
 */
@AutoConfiguration
@EnableConfigurationProperties(TraceProperties.class)
@ConditionalOnProperty(prefix = "ikeu.trace", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TraceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "traceIdFilter")
    public FilterRegistrationBean<TraceIdFilter> traceIdFilterRegistration(TraceProperties props) {
        FilterRegistrationBean<TraceIdFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new TraceIdFilter(props));
        reg.addUrlPatterns("/*");
        reg.setOrder(Integer.MIN_VALUE + 100);
        reg.setName("traceIdFilter");
        return reg;
    }
}
