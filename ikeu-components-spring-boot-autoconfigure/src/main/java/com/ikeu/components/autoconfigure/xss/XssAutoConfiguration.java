package com.ikeu.components.autoconfigure.xss;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for XSS protection.
 * <p>
 * Registers {@link XssFilter} at the highest precedence order
 * ({@code Integer.MIN_VALUE}) so request parameters are sanitized before
 * any other filter or controller reads them.
 * <p>
 * Disabled by default. Enable with {@code ikeu.xss.enabled=true}.
 *
 * <h3>Configuration</h3>
 * <pre>{@code
 * ikeu:
 *   xss:
 *     enabled: true
 *     mode: escape             # escape | strip
 *     exclude-paths:
 *       - /api/v1/richtext/**
 * }</pre>
 */
@AutoConfiguration
@EnableConfigurationProperties(XssProperties.class)
@ConditionalOnProperty(prefix = "ikeu.xss", name = "enabled", havingValue = "true")
public class XssAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "xssFilter")
    public FilterRegistrationBean<XssFilter> xssFilterRegistration(XssProperties props) {
        FilterRegistrationBean<XssFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new XssFilter(props));
        reg.addUrlPatterns("/*");
        reg.setOrder(Integer.MIN_VALUE);
        reg.setName("xssFilter");
        return reg;
    }
}
