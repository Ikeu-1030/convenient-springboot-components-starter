package com.ikeu.components.autoconfigure.protection;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.*;

class SecurityProtectionAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SecurityProtectionAutoConfiguration.class));

    @Test
    void shouldNotRegisterWhenDisabled() {
        runner.run(ctx -> {
            assertFalse(ctx.containsBean("ikeuSecurityFilterChain"));
        });
    }

    @Test
    void shouldBindProperties() {
        SecurityProtectionProperties props = new SecurityProtectionProperties();
        props.setCsrfDisabled(false);
        props.setStatelessSession(false);
        props.setSecurityHeaders(false);
        props.setPermitAllPatterns(new String[]{"/public/**", "/api/**"});

        assertFalse(props.isCsrfDisabled());
        assertFalse(props.isStatelessSession());
        assertFalse(props.isSecurityHeaders());
        assertEquals(2, props.getPermitAllPatterns().length);
    }
}
