package com.ikeu.components.autoconfigure.jackson;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.*;

class JacksonPropertiesTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Configuration
    @EnableConfigurationProperties(JacksonProperties.class)
    static class TestConfig {}

    @Test
    void defaultValues() {
        runner.run(ctx -> {
            JacksonProperties props = ctx.getBean(JacksonProperties.class);
            assertEquals("yyyy-MM-dd HH:mm:ss", props.getDatePattern());
            assertTrue(props.isLongAsString());
            assertEquals("non_null", props.getSerializationInclusion());
        });
    }

    @Test
    void customDatePattern() {
        runner.withPropertyValues("ikeu.jackson.date-pattern=yyyy/MM/dd").run(ctx -> {
            JacksonProperties props = ctx.getBean(JacksonProperties.class);
            assertEquals("yyyy/MM/dd", props.getDatePattern());
        });
    }

    @Test
    void customTimeZone() {
        runner.withPropertyValues("ikeu.jackson.time-zone=Asia/Shanghai").run(ctx -> {
            JacksonProperties props = ctx.getBean(JacksonProperties.class);
            assertEquals("Asia/Shanghai", props.getTimeZone());
        });
    }
}