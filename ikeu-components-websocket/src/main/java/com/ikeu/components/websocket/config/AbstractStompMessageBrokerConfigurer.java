package com.ikeu.components.websocket.config;

import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.util.List;

/**
 * Convenience base class for STOMP-over-WebSocket configuration.
 * <p>
 * Business code extends this class instead of implementing
 * {@link WebSocketMessageBrokerConfigurer} directly. The subclass only
 * needs to override:
 * <ul>
 *   <li>{@link #registerStompEndpoints(StompEndpointRegistry)}</li>
 *   <li>{@link #configureMessageBroker(MessageBrokerRegistry)}</li>
 * </ul>
 * <p>
 * JWT interceptors are automatically registered via the injected beans.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * @Configuration
 * @EnableWebSocketMessageBroker
 * public class WebSocketConfig extends AbstractStompMessageBrokerConfigurer {
 *
 *     @Override
 *     public void registerStompEndpoints(StompEndpointRegistry registry) {
 *         registry.addEndpoint("/ws")
 *                 .setAllowedOriginPatterns("*")
 *                 .withSockJS();
 *     }
 *
 *     @Override
 *     public void configureMessageBroker(MessageBrokerRegistry registry) {
 *         registry.enableSimpleBroker("/topic", "/queue");
 *         registry.setApplicationDestinationPrefixes("/app");
 *         registry.setUserDestinationPrefix("/user");
 *     }
 * }
 * }</pre>
 */
public abstract class AbstractStompMessageBrokerConfigurer
        implements WebSocketMessageBrokerConfigurer {

    private final JwtWebSocketHandshakeInterceptor handshakeInterceptor;
    private final JwtStompChannelInterceptor channelInterceptor;
    private final List<String> endpointPaths;

    protected AbstractStompMessageBrokerConfigurer(
            JwtWebSocketHandshakeInterceptor handshakeInterceptor,
            JwtStompChannelInterceptor channelInterceptor,
            List<String> endpointPaths) {
        this.handshakeInterceptor = handshakeInterceptor;
        this.channelInterceptor = channelInterceptor;
        this.endpointPaths = endpointPaths != null ? endpointPaths : List.of("/ws");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // no-op: subclass overrides this
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // no-op: subclass overrides this
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(channelInterceptor);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
    }
}
