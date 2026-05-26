package com.ikeu.components.autoconfigure.trace;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.*;

class TraceAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TraceAutoConfiguration.class));

    @Test
    void shouldRegisterFilterWhenEnabled() {
        runner.run(ctx -> {
            assertTrue(ctx.containsBean("traceIdFilterRegistration"));
        });
    }

    @Test
    void shouldNotRegisterWhenDisabled() {
        runner.withPropertyValues("ikeu.trace.enabled=false").run(ctx -> {
            assertFalse(ctx.containsBean("traceIdFilterRegistration"));
        });
    }

    @Test
    void shouldBindCustomProperties() {
        runner.withPropertyValues(
                "ikeu.trace.header-name=X-Request-Id",
                "ikeu.trace.response-header=false",
                "ikeu.trace.mdc-key=customTraceId"
        ).run(ctx -> {
            TraceProperties props = ctx.getBean(TraceProperties.class);
            assertEquals("X-Request-Id", props.getHeaderName());
            assertFalse(props.isResponseHeader());
            assertEquals("customTraceId", props.getMdcKey());
        });
    }
}
