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
 * Auto-configuration for JWT security: {@link JwtUtils}, {@link JwtAuthenticationFilter},
 * {@link UserContextClearFilter}, and {@link UserContextInterceptor}.
 * <p>
 * Activated when {@code ikeu.jwt.enabled=true}. Supports {@code SINGLE} and
 * {@code DUAL} token modes via {@link JwtProperties.TokenMode}.
 *
 * <h3>Beans created (in order)</h3>
 * <ol>
 *   <li>{@code JwtUtils} — dual-key capable token utilities</li>
 *   <li>{@code UserContextClearFilter} — runs last, guarantees context cleanup</li>
 *   <li>{@code UserContextInterceptor} — Spring MVC interceptor, blocks unauthenticated requests</li>
 *   <li>{@code WebMvcConfigurer} — registers the interceptor at order 0</li>
 *   <li>{@code JwtAuthenticationFilter} — servlet filter, extracts and validates JWT at order -100
 *       (opt-out via {@code ikeu.jwt.auto-filter=false})</li>
 * </ol>
 *
 * <h3>Dual token config example</h3>
 * <pre>{@code
 * ikeu:
 *   jwt:
 *     enabled: true
 *     mode: dual
 *     access-secret: "my-access-secret-32chars-min-xxxx"
 *     access-expiration: 2h
 *     refresh-secret: "my-refresh-secret-32chars-min-xxxx"
 *     refresh-expiration: 7d
 *     exclude-paths:
 *       - /api/v1/auth/login
 *       - /api/v1/auth/refresh
 * }</pre>
 *
 * <p>
 * <b>Note:</b> When {@code auto-filter=false}, the JwtAuthenticationFilter is not registered,
 * but the UserContextInterceptor still runs. Ensure your own authentication mechanism
 * populates {@code UserContextHolder} before requests reach the interceptor.
 *
 * @author ikeu
 * @since 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
@ConditionalOnProperty(prefix = "ikeu.jwt", name = "enabled", havingValue = "true")
public class SecurityAutoConfiguration {

    // ──────────────────────────────────────────────
    // JWT utilities
    // ──────────────────────────────────────────────

    /**
     * Create JwtUtils wired with resolved access/refresh secrets and TTLs.
     * In DUAL mode, separate signing keys are used for access and refresh tokens.
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtUtils jwtUtils(JwtProperties properties) {
        boolean isDual = properties.getMode() == JwtProperties.TokenMode.DUAL;
        return new JwtUtils(
                properties.resolveAccessSecret(),
                properties.resolveRefreshSecret(),
                properties.resolveAccessExpiration(),
                properties.resolveRefreshExpiration(),
                isDual);
    }

    // ──────────────────────────────────────────────
    // UserContext cleanup — always registered,
    // runs last in the filter chain
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
    // JWT authentication filter
    // Opt-out via ikeu.jwt.auto-filter=false
    // ──────────────────────────────────────────────

    @Bean
    @ConditionalOnMissingBean(name = "jwtFilterRegistration")
    @ConditionalOnProperty(prefix = "ikeu.jwt", name = "auto-filter", havingValue = "true",
            matchIfMissing = true)
    public FilterRegistrationBean<Filter> jwtFilterRegistration(JwtUtils jwtUtils,
                                                                 JwtProperties properties) {
        boolean isDual = properties.getMode() == JwtProperties.TokenMode.DUAL;

        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(
                jwtUtils,
                properties.resolveAccessHeaderName(),
                properties.resolveAccessTokenPrefix(),
                properties.resolveRefreshHeaderName(),
                properties.resolveRefreshTokenPrefix(),
                isDual,
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