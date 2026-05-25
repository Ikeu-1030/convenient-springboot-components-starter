package com.ikeu.components.autoconfigure.web;

import com.ikeu.components.web.context.SpringContextHolder;
import com.ikeu.components.web.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.*;

class WebAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(WebAutoConfiguration.class));

    @Test
    void importsSpringContextHolder() {
        runner.run(ctx -> {
            SpringContextHolder holder = ctx.getBean(SpringContextHolder.class);
            assertNotNull(holder);
        });
    }

    @Test
    void importsGlobalExceptionHandler() {
        runner.run(ctx -> {
            GlobalExceptionHandler handler = ctx.getBean(GlobalExceptionHandler.class);
            assertNotNull(handler);
        });
    }
}