package com.ikeu.components.autoconfigure.websocket;

import com.ikeu.components.security.util.JwtUtils;
import com.ikeu.components.websocket.config.JwtStompChannelInterceptor;
import com.ikeu.components.websocket.config.JwtWebSocketHandshakeInterceptor;
import com.ikeu.components.websocket.config.StompAuthorizationValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * Auto-configuration for WebSocket / STOMP JWT authentication.
 * <p>
 * Registers:
 * <ul>
 *   <li>{@link JwtWebSocketHandshakeInterceptor} — validates JWT during
 *       HTTP-to-WebSocket upgrade</li>
 *   <li>{@link JwtStompChannelInterceptor} — validates JWT on STOMP CONNECT
 *       and authorizes SUBSCRIBE destinations</li>
 * </ul>
 * <p>
 * Requires a {@link JwtUtils} bean (auto-configured by {@code SecurityAutoConfiguration})
 * and {@code spring-websocket} / {@code spring-messaging} on the classpath.
 */
@AutoConfiguration
@EnableConfigurationProperties(WebSocketProperties.class)
@ConditionalOnProperty(prefix = "ikeu.websocket", name = "enabled", havingValue = "true")
@ConditionalOnClass(name = "org.springframework.web.socket.server.HandshakeInterceptor")
@ConditionalOnBean(JwtUtils.class)
@Slf4j
public class WebSocketAutoConfiguration {

    /**
     * JWT-authenticated WebSocket handshake interceptor.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "ikeu.websocket", name = "jwt-auth-enabled",
            havingValue = "true", matchIfMissing = true)
    public JwtWebSocketHandshakeInterceptor jwtWebSocketHandshakeInterceptor(
            JwtUtils jwtUtils, WebSocketProperties props) {
        log.info("WebSocket JWT handshake interceptor registered (tokenParam={})",
                props.getTokenParam());
        return new JwtWebSocketHandshakeInterceptor(
                jwtUtils,
                props.getTokenParam(),
                props.getHandshakeWhitelist());
    }

    /**
     * JWT-authenticated STOMP channel interceptor.
     * <p>
     * If no {@link StompAuthorizationValidator} bean is defined by the user,
     * a permissive default (always-allowed) is used.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "ikeu.websocket", name = "jwt-auth-enabled",
            havingValue = "true", matchIfMissing = true)
    public JwtStompChannelInterceptor jwtStompChannelInterceptor(
            JwtUtils jwtUtils, StompAuthorizationValidator authorizationValidator) {
        log.info("WebSocket STOMP channel interceptor registered");
        return new JwtStompChannelInterceptor(jwtUtils, authorizationValidator);
    }

    /**
     * Default authorization validator — restricts subscriptions to the
     * authenticated user's own scoped destinations and public channels.
     * <p>
     * <b>Override</b> by defining your own {@link StompAuthorizationValidator}
     * bean for custom subscription rules.
     */
    @Bean
    @ConditionalOnMissingBean
    public StompAuthorizationValidator defaultStompAuthorizationValidator() {
        log.warn("Using default STOMP authorization validator — only user-scoped "
                + "destinations (/user/{userId}/...) and public topics (/topic/public/**) "
                + "are allowed. Define a custom StompAuthorizationValidator bean for "
                + "production use.");
        return (userId, destination) -> {
            if (destination == null || userId == null) {
                return false;
            }
            if (destination.startsWith("/user/" + userId + "/")
                    || destination.startsWith("/topic/public/")
                    || destination.startsWith("/queue/public/")) {
                return true;
            }
            log.warn("STOMP SUBSCRIBE denied by default validator: userId={}, destination={}",
                    userId, destination);
            return false;
        };
    }
}
