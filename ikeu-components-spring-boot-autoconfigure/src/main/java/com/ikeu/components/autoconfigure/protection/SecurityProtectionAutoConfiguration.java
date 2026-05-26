package com.ikeu.components.autoconfigure.protection;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

/**
 * Auto-configuration for Spring Security common protections.
 * <p>
 * Creates a {@link SecurityFilterChain} that provides security headers, CSRF
 * disabling, and stateless session management — <strong>without</strong>
 * enforcing authentication. Authentication is handled by the existing
 * JWT servlet filter ({@code JwtAuthenticationFilter}).
 * <p>
 * Disabled by default. Enable with {@code ikeu.security-protection.enabled=true}
 * and add {@code spring-boot-starter-security} to your project.
 *
 * <h3>Configuration</h3>
 * <pre>{@code
 * ikeu:
 *   security-protection:
 *     enabled: true
 *     csrf-disabled: true
 *     stateless-session: true
 *     security-headers: true
 *     permit-all-patterns: [/public/**, /actuator/health]
 * }</pre>
 */
@AutoConfiguration
@EnableConfigurationProperties(SecurityProtectionProperties.class)
@ConditionalOnClass({SecurityFilterChain.class, HttpSecurity.class, EnableWebSecurity.class})
@ConditionalOnProperty(prefix = "ikeu.security-protection", name = "enabled", havingValue = "true")
public class SecurityProtectionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "ikeuSecurityFilterChain")
    public SecurityFilterChain ikeuSecurityFilterChain(HttpSecurity http,
                                                        SecurityProtectionProperties props) throws Exception {
        // CSRF
        if (props.isCsrfDisabled()) {
            http.csrf(AbstractHttpConfigurer::disable);
        }

        // Session management
        if (props.isStatelessSession()) {
            http.sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        }

        // Security headers
        if (props.isSecurityHeaders()) {
            http.headers(headers -> headers
                    .xssProtection(xss -> xss.headerValue(
                            XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                    .contentSecurityPolicy(csp ->
                            csp.policyDirectives("default-src 'self'"))
                    .frameOptions(frame -> frame.sameOrigin())
                    .contentTypeOptions(Customizer.withDefaults())
                    .httpStrictTransportSecurity(hsts ->
                            hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                    .cacheControl(Customizer.withDefaults())
            );
        }

        // Authorization: permit all. Authentication is handled by the
        // JWT servlet filter, not by Spring Security.
        http.authorizeHttpRequests(auth -> {
            String[] patterns = props.getPermitAllPatterns();
            if (patterns != null && patterns.length > 0) {
                auth.requestMatchers(patterns).permitAll();
            }
            auth.anyRequest().permitAll();
        });

        return http.build();
    }
}
