package com.ikeu.components.autoconfigure.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class JwtPropertiesTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Configuration
    @EnableConfigurationProperties(JwtProperties.class)
    static class TestConfig {}

    @Test
    void defaultValues() {
        runner.run(ctx -> {
            JwtProperties props = ctx.getBean(JwtProperties.class);
            assertEquals(JwtProperties.TokenMode.SINGLE, props.getMode());
            assertFalse(props.isEnabled());
            assertEquals(Duration.ofHours(24), props.getExpiration());
            assertEquals("Authorization", props.getHeaderName());
            assertEquals("Bearer ", props.getTokenPrefix());
            assertTrue(props.isAutoFilter());
            assertFalse(props.isFailOnInvalid());
        });
    }

    @Test
    void resolveAccessSecret_fallsBackToSecret() {
        runner.withPropertyValues(
                "ikeu.jwt.secret=custom-secret"
        ).run(ctx -> {
            JwtProperties props = ctx.getBean(JwtProperties.class);
            assertEquals("custom-secret", props.resolveAccessSecret());
        });
    }

    @Test
    void resolveAccessSecret_overrideWins() {
        runner.withPropertyValues(
                "ikeu.jwt.secret=shared-secret",
                "ikeu.jwt.access-secret=access-specific"
        ).run(ctx -> {
            JwtProperties props = ctx.getBean(JwtProperties.class);
            assertEquals("access-specific", props.resolveAccessSecret());
        });
    }

    @Test
    void resolveRefreshSecret_singleMode_returnsAccessSecret() {
        runner.withPropertyValues(
                "ikeu.jwt.mode=single",
                "ikeu.jwt.secret=only-secret"
        ).run(ctx -> {
            JwtProperties props = ctx.getBean(JwtProperties.class);
            assertEquals("only-secret", props.resolveRefreshSecret());
        });
    }

    @Test
    void resolveRefreshSecret_dualMode_fallsBackToSecret() {
        runner.withPropertyValues(
                "ikeu.jwt.mode=dual",
                "ikeu.jwt.secret=shared",
                "ikeu.jwt.access-secret=access-key"
        ).run(ctx -> {
            JwtProperties props = ctx.getBean(JwtProperties.class);
            assertEquals("shared", props.resolveRefreshSecret());
        });
    }

    @Test
    void resolveAccessExpiration_fallsBack() {
        runner.withPropertyValues("ikeu.jwt.expiration=48h").run(ctx -> {
            JwtProperties props = ctx.getBean(JwtProperties.class);
            assertEquals(Duration.ofHours(48), props.resolveAccessExpiration());
        });
    }

    @Test
    void customAccessHeaderName() {
        runner.withPropertyValues("ikeu.jwt.access-header-name=X-Auth-Token").run(ctx -> {
            JwtProperties props = ctx.getBean(JwtProperties.class);
            assertEquals("X-Auth-Token", props.resolveAccessHeaderName());
        });
    }

    @Test
    void resolveRefreshHeaderName_defaultsToXRefreshToken() {
        runner.run(ctx -> {
            JwtProperties props = ctx.getBean(JwtProperties.class);
            assertEquals("X-Refresh-Token", props.resolveRefreshHeaderName());
        });
    }
}