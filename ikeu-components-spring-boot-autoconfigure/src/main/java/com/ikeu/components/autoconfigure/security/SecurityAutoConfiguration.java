package com.ikeu.components.autoconfigure.security;

import com.ikeu.components.security.filter.JwtAuthenticationFilter;
import com.ikeu.components.security.filter.UserContextClearFilter;
import com.ikeu.components.security.util.JwtUtils;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Auto-configuration for security: JwtUtils, JwtAuthenticationFilter,
 * UserContextClearFilter, and UserContextInterceptor.
 * Activated when {@code ikeu.jwt.enabled=true}.
 */
@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
@ConditionalOnProperty(prefix = "ikeu.jwt", name = "enabled", havingValue = "true")
public class SecurityAutoConfiguration {

    // ──────────────────────────────────────────────
    // JWT utilities
    // ──────────────────────────────────────────────

    @Bean
    @ConditionalOnMissingBean
    public JwtUtils jwtUtils(JwtProperties properties) {
        return new JwtUtils(properties.getSecret(), properties.getExpiration());
    }

    // ──────────────────────────────────────────────
    // UserContext cleanup — filter (always)
    // Runs first/last in chain to wrap all others
    // ──────────────────────────────────────────────

    @Bean
    @ConditionalOnMissingBean(name = "userContextClearFilterRegistration")
    public FilterRegistrationBean<Filter> userContextClearFilterRegistration() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new UserContextClearFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Integer.MAX_VALUE);
        registration.setName("userContextClearFilter");
        return registration;
    }

    // ──────────────────────────────────────────────
    // Authentication interceptor (Spring MVC)
    // preHandle  → verifies UserContextHolder is set, returns 401 if not
    // afterCompletion → clears UserContextHolder
    // Registered when Spring MVC is present on the classpath
    // ──────────────────────────────────────────────

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(HandlerInterceptor.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public UserContextInterceptor userContextInterceptor(JwtProperties properties) {
        return new UserContextInterceptor(properties.getExcludePaths());
    }

    @Bean
    @ConditionalOnClass(HandlerInterceptor.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public WebMvcConfigurer userContextWebMvcConfigurer(UserContextInterceptor interceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(interceptor)
                        .addPathPatterns("/**")
                        .order(0);
            }
        };
    }

    // ──────────────────────────────────────────────
    // JWT authentication filter (opt-out via ikeu.jwt.auto-filter=false)
    // ──────────────────────────────────────────────

    @Bean
    @ConditionalOnMissingBean(name = "jwtFilterRegistration")
    @ConditionalOnProperty(prefix = "ikeu.jwt", name = "auto-filter", havingValue = "true",
            matchIfMissing = true)
    public FilterRegistrationBean<Filter> jwtFilterRegistration(JwtUtils jwtUtils,
                                                                 JwtProperties properties) {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(
                jwtUtils,
                properties.getHeaderName(),
                properties.getTokenPrefix(),
                properties.getExcludePaths(),
                properties.isFailOnInvalid());

        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(jwtFilter);
        registration.addUrlPatterns("/*");
        registration.setOrder(-100);
        registration.setName("jwtAuthenticationFilter");
        return registration;
    }
}