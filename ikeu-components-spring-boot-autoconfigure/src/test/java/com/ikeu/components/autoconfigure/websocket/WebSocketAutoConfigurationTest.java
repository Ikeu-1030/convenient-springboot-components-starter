package com.ikeu.components.autoconfigure.websocket;

import com.ikeu.components.security.util.JwtUtils;
import com.ikeu.components.websocket.config.JwtStompChannelInterceptor;
import com.ikeu.components.websocket.config.JwtWebSocketHandshakeInterceptor;
import com.ikeu.components.websocket.config.StompAuthorizationValidator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WebSocketAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(WebSocketAutoConfiguration.class));

    @Test
    void shouldNotRegisterBeansWhenDisabled() {
        runner.run(ctx -> {
            assertFalse(ctx.containsBean("jwtWebSocketHandshakeInterceptor"));
            assertFalse(ctx.containsBean("jwtStompChannelInterceptor"));
            assertFalse(ctx.containsBean("defaultStompAuthorizationValidator"));
        });
    }

    @Test
    void shouldRegisterBeansWhenEnabledWithJwtUtils() {
        runner.withPropertyValues(
                        "ikeu.websocket.enabled=true",
                        "ikeu.jwt.enabled=true",
                        "ikeu.jwt.secret=test-secret-key-min-32-chars-xxx")
                .withBean(JwtUtils.class,
                        () -> new JwtUtils("test-secret-key-min-32-chars-xxx",
                                "refresh-secret-key-min-32-chars-x",
                                Duration.ofHours(2), Duration.ofDays(7), false))
                .run(ctx -> {
                    assertTrue(ctx.containsBean("jwtWebSocketHandshakeInterceptor"));
                    assertTrue(ctx.containsBean("jwtStompChannelInterceptor"));
                    assertTrue(ctx.containsBean("defaultStompAuthorizationValidator"));
                });
    }

    @Test
    void shouldNotRegisterWithoutJwtUtils() {
        runner.withPropertyValues("ikeu.websocket.enabled=true")
                .run(ctx -> {
                    assertFalse(ctx.containsBean("jwtWebSocketHandshakeInterceptor"));
                    assertFalse(ctx.containsBean("jwtStompChannelInterceptor"));
                });
    }

    @Test
    void shouldBindProperties() {
        WebSocketProperties props = new WebSocketProperties();
        props.setEnabled(true);
        props.setJwtAuthEnabled(true);
        props.setTokenParam("my_token");
        props.setEndpointPaths(List.of("/ws", "/chat"));
        props.setHandshakeWhitelist(List.of("/public"));
        props.setAllowedOrigins(List.of("https://example.com"));
        props.setSockJsEnabled(false);

        assertTrue(props.isEnabled());
        assertTrue(props.isJwtAuthEnabled());
        assertEquals("my_token", props.getTokenParam());
        assertEquals(List.of("/ws", "/chat"), props.getEndpointPaths());
        assertEquals(List.of("/public"), props.getHandshakeWhitelist());
        assertEquals(List.of("https://example.com"), props.getAllowedOrigins());
        assertFalse(props.isSockJsEnabled());
    }

    @Test
    void defaultPropertyValues() {
        WebSocketProperties props = new WebSocketProperties();

        assertFalse(props.isEnabled());
        assertTrue(props.isJwtAuthEnabled());
        assertEquals("access_token", props.getTokenParam());
        assertEquals(List.of("/ws"), props.getEndpointPaths());
        assertTrue(props.getHandshakeWhitelist().isEmpty());
        assertTrue(props.getAllowedOrigins().isEmpty());
        assertTrue(props.isSockJsEnabled());
    }

    @Test
    void defaultValidatorShouldAllowUserScopedDestination() {
        runner.withPropertyValues(
                        "ikeu.websocket.enabled=true",
                        "ikeu.jwt.enabled=true",
                        "ikeu.jwt.secret=test-secret-key-min-32-chars-xxx")
                .withBean(JwtUtils.class,
                        () -> new JwtUtils("test-secret-key-min-32-chars-xxx",
                                "refresh-secret-key-min-32-chars-x",
                                Duration.ofHours(2), Duration.ofDays(7), false))
                .run(ctx -> {
                    StompAuthorizationValidator validator =
                            ctx.getBean(StompAuthorizationValidator.class);

                    assertTrue(validator.canSubscribe("user123", "/user/user123/messages"));
                    assertTrue(validator.canSubscribe("user123", "/topic/public/events"));
                    assertTrue(validator.canSubscribe("user123", "/queue/public/tasks"));
                    assertFalse(validator.canSubscribe("user123", "/user/user999/messages"));
                    assertFalse(validator.canSubscribe("user123", "/app/private"));
                });
    }

    @Test
    void canOverrideDefaultValidator() {
        runner.withPropertyValues(
                        "ikeu.websocket.enabled=true",
                        "ikeu.jwt.enabled=true",
                        "ikeu.jwt.secret=test-secret-key-min-32-chars-xxx")
                .withBean(JwtUtils.class,
                        () -> new JwtUtils("test-secret-key-min-32-chars-xxx",
                                "refresh-secret-key-min-32-chars-x",
                                Duration.ofHours(2), Duration.ofDays(7), false))
                .withBean(StompAuthorizationValidator.class,
                        () -> (userId, destination) -> true)
                .run(ctx -> {
                    StompAuthorizationValidator validator =
                            ctx.getBean(StompAuthorizationValidator.class);
                    // custom validator allows everything
                    assertTrue(validator.canSubscribe("anyone", "/any/destination"));
                });
    }

    @Test
    void shouldRegisterJwtHandshakeInterceptor() {
        runner.withPropertyValues(
                        "ikeu.websocket.enabled=true",
                        "ikeu.websocket.token-param=my_token",
                        "ikeu.jwt.enabled=true",
                        "ikeu.jwt.secret=test-secret-key-min-32-chars-xxx")
                .withBean(JwtUtils.class,
                        () -> new JwtUtils("test-secret-key-min-32-chars-xxx",
                                "refresh-secret-key-min-32-chars-x",
                                Duration.ofHours(2), Duration.ofDays(7), false))
                .run(ctx -> {
                    JwtWebSocketHandshakeInterceptor interceptor =
                            ctx.getBean(JwtWebSocketHandshakeInterceptor.class);
                    assertNotNull(interceptor);
                });
    }

    @Test
    void shouldRegisterJwtStompChannelInterceptor() {
        runner.withPropertyValues(
                        "ikeu.websocket.enabled=true",
                        "ikeu.jwt.enabled=true",
                        "ikeu.jwt.secret=test-secret-key-min-32-chars-xxx")
                .withBean(JwtUtils.class,
                        () -> new JwtUtils("test-secret-key-min-32-chars-xxx",
                                "refresh-secret-key-min-32-chars-x",
                                Duration.ofHours(2), Duration.ofDays(7), false))
                .run(ctx -> {
                    JwtStompChannelInterceptor interceptor =
                            ctx.getBean(JwtStompChannelInterceptor.class);
                    assertNotNull(interceptor);
                });
    }
}
