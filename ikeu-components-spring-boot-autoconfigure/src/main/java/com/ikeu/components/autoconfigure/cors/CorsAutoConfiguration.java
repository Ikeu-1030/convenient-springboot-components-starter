package com.ikeu.components.autoconfigure.cors;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Auto-configuration for CORS (Cross-Origin Resource Sharing).
 * <p>
 * Registers a {@link WebMvcConfigurer} that configures CORS mappings via
 * Spring MVC's {@link CorsRegistry}, which integrates with {@code @CrossOrigin}
 * and handler-level CORS processing.
 * <p>
 * Disabled by default. Enable with {@code ikeu.cors.enabled=true}.
 *
 * <h3>Configuration</h3>
 * <pre>{@code
 * ikeu:
 *   cors:
 *     enabled: true
 *     path-pattern: /**
 *     allowed-origins: ["https://example.com"]
 *     allowed-methods: [GET, POST, PUT, DELETE]
 *     allowed-headers: ["*"]
 *     allow-credentials: true
 *     max-age: 1800s
 * }</pre>
 */
@AutoConfiguration
@EnableConfigurationProperties(CorsProperties.class)
@ConditionalOnProperty(prefix = "ikeu.cors", name = "enabled", havingValue = "true")
public class CorsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "ikeuCorsWebMvcConfigurer")
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(name = "org.springframework.web.servlet.config.annotation.WebMvcConfigurer")
    public WebMvcConfigurer ikeuCorsWebMvcConfigurer(CorsProperties props) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                CorsRegistration reg = registry.addMapping(props.getPathPattern());
                reg.allowedOrigins(props.getAllowedOrigins().toArray(new String[0]));
                reg.allowedMethods(props.getAllowedMethods().toArray(new String[0]));
                reg.allowedHeaders(props.getAllowedHeaders().toArray(new String[0]));
                if (!props.getExposedHeaders().isEmpty()) {
                    reg.exposedHeaders(props.getExposedHeaders().toArray(new String[0]));
                }
                reg.allowCredentials(props.isAllowCredentials());
                reg.maxAge(props.getMaxAge().getSeconds());
            }
        };
    }
}
