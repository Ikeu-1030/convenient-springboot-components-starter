package com.ikeu.components.autoconfigure.security;

import com.ikeu.components.security.filter.JwtAuthenticationFilter;
import com.ikeu.components.security.util.JwtUtils;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for security: JwtUtils, JwtAuthenticationFilter.
 * Activated only when ikeu.jwt.enabled=true.
 */
@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
@ConditionalOnProperty(prefix = "ikeu.jwt", name = "enabled", havingValue = "true")
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtUtils jwtUtils(JwtProperties properties) {
        return new JwtUtils(properties.getSecret(), properties.getExpiration());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "ikeu.jwt", name = "auto-filter", havingValue = "true",
            matchIfMissing = true)
    public FilterRegistrationBean<Filter> jwtFilterRegistration(JwtUtils jwtUtils) {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JwtAuthenticationFilter(jwtUtils));
        registration.addUrlPatterns("/*");
        registration.setOrder(-100);
        registration.setName("jwtAuthenticationFilter");
        return registration;
    }
}
