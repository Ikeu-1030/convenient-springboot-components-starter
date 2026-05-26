package com.ikeu.components.autoconfigure.xss;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.*;

class XssAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(XssAutoConfiguration.class));

    @Test
    void shouldNotRegisterWhenDisabled() {
        runner.run(ctx -> {
            assertFalse(ctx.containsBean("xssFilterRegistration"));
        });
    }

    @Test
    void shouldRegisterWhenEnabled() {
        runner.withPropertyValues("ikeu.xss.enabled=true").run(ctx -> {
            assertTrue(ctx.containsBean("xssFilterRegistration"));
        });
    }

    @Test
    void shouldBindCustomProperties() {
        runner.withPropertyValues(
                "ikeu.xss.enabled=true",
                "ikeu.xss.mode=strip",
                "ikeu.xss.exclude-paths=/api/v1/richtext/**,/api/v1/editor/**"
        ).run(ctx -> {
            XssProperties props = ctx.getBean(XssProperties.class);
            assertEquals(XssProperties.XssMode.STRIP, props.getMode());
            assertEquals(2, props.getExcludePaths().size());
            assertTrue(props.getExcludePaths().contains("/api/v1/richtext/**"));
        });
    }
}
