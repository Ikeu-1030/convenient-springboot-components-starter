package com.ikeu.components.autoconfigure.security;

import com.ikeu.components.security.util.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.*;

class SecurityAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SecurityAutoConfiguration.class));

    @Test
    void disabledByDefault_noJwtUtilsBean() {
        runner.run(ctx -> assertFalse(ctx.containsBean("jwtUtils")));
    }

    @Test
    void enabled_createsJwtUtils() {
        runner.withPropertyValues("ikeu.jwt.enabled=true").run(ctx -> {
            assertTrue(ctx.containsBean("jwtUtils"));
            JwtUtils jwtUtils = ctx.getBean(JwtUtils.class);
            assertNotNull(jwtUtils);
        });
    }

    @Test
    void autoFilterTrue_createsFilter() {
        runner.withPropertyValues(
                "ikeu.jwt.enabled=true",
                "ikeu.jwt.auto-filter=true"
        ).run(ctx -> {
            assertTrue(ctx.containsBean("jwtFilterRegistration"));
        });
    }

    @Test
    void autoFilterFalse_skipsFilter() {
        runner.withPropertyValues(
                "ikeu.jwt.enabled=true",
                "ikeu.jwt.auto-filter=false"
        ).run(ctx -> {
            assertFalse(ctx.containsBean("jwtFilterRegistration"));
        });
    }

    @Test
    void userContextClearFilter_alwaysRegistered() {
        runner.withPropertyValues("ikeu.jwt.enabled=true").run(ctx -> {
            assertTrue(ctx.containsBean("userContextClearFilterRegistration"));
        });
    }

    @Test
    void dualMode_configuration() {
        runner.withPropertyValues(
                "ikeu.jwt.enabled=true",
                "ikeu.jwt.mode=dual",
                "ikeu.jwt.access-secret=access-key-32chars-minimum!!!!!",
                "ikeu.jwt.refresh-secret=refresh-key-32chars-minimum!!!",
                "ikeu.jwt.access-expiration=30m",
                "ikeu.jwt.refresh-expiration=14d"
        ).run(ctx -> {
            JwtUtils jwtUtils = ctx.getBean(JwtUtils.class);
            assertNotNull(jwtUtils);
            // Verify it doesn't crash — JwtUtils is wired properly
            var pair = jwtUtils.generateTokenPair("user", null);
            assertNotNull(pair.getAccessToken());
            assertNotNull(pair.getRefreshToken());
            assertTrue(pair.getRefreshExpiresIn() > 0);
        });
    }
}