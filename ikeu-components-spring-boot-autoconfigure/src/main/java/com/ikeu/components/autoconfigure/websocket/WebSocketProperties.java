package com.ikeu.components.autoconfigure.websocket;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for WebSocket / STOMP enhancements.
 * <p>
 * Prefix: {@code ikeu.websocket}
 */
@ConfigurationProperties(prefix = "ikeu.websocket")
@Data
public class WebSocketProperties {

    /** Enable WebSocket auto-configuration. Default: false. */
    private boolean enabled = false;

    /** Enable JWT authentication on WebSocket handshake and STOMP CONNECT. */
    private boolean jwtAuthEnabled = true;

    /** Query parameter name for the access token (SockJS fallback). */
    private String tokenParam = "access_token";

    /** STOMP endpoint paths for registering the handshake interceptor. */
    private List<String> endpointPaths = List.of("/ws");

    /** Paths excluded from JWT validation during handshake. */
    private List<String> handshakeWhitelist = new ArrayList<>();

    /** Destination prefixes that bypass subscription authorization. */
    private List<String> subscribeWhitelist = new ArrayList<>();

    /** Allowed origins for WebSocket connections (CORS). Must be explicitly configured. */
    private List<String> allowedOrigins = new ArrayList<>();

    /** Whether to allow SockJS fallback. */
    private boolean sockJsEnabled = true;
}
