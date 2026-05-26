package com.ikeu.components.autoconfigure.cors;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.*;

class CorsAutoConfigurationTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CorsAutoConfiguration.class));

    @Test
    void shouldNotRegisterWhenDisabled() {
        runner.run(ctx -> {
            assertFalse(ctx.containsBean("ikeuCorsWebMvcConfigurer"));
        });
    }

    @Test
    void shouldRegisterWhenEnabled() {
        runner.withPropertyValues("ikeu.cors.enabled=true").run(ctx -> {
            assertTrue(ctx.containsBean("ikeuCorsWebMvcConfigurer"));
        });
    }

    @Test
    void shouldBindCustomProperties() {
        runner.withPropertyValues(
                "ikeu.cors.enabled=true",
                "ikeu.cors.allowed-origins=https://example.com,https://app.example.com",
                "ikeu.cors.allowed-methods=GET,POST",
                "ikeu.cors.allow-credentials=true",
                "ikeu.cors.max-age=3600s"
        ).run(ctx -> {
            CorsProperties props = ctx.getBean(CorsProperties.class);
            assertEquals("/**", props.getPathPattern());
            assertEquals(2, props.getAllowedOrigins().size());
            assertTrue(props.isAllowCredentials());
            assertEquals(java.time.Duration.ofSeconds(3600), props.getMaxAge());
        });
    }
}
