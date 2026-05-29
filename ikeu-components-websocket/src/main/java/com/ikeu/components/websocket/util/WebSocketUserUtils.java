package com.ikeu.components.websocket.util;

import com.ikeu.components.security.context.UserContextHolder;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import java.security.Principal;
import java.util.Map;

/**
 * Utility methods for extracting the current user from WebSocket / STOMP contexts.
 */
public final class WebSocketUserUtils {

    private WebSocketUserUtils() {
    }

    /**
     * Get the current user ID from a {@link StompHeaderAccessor}.
     * Checks session attributes first, then the Principal, then ThreadLocal.
     */
    public static String getUserId(StompHeaderAccessor accessor) {
        if (accessor == null) {
            return UserContextHolder.getUserId();
        }

        Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
        if (sessionAttrs != null) {
            Object userId = sessionAttrs.get("userId");
            if (userId != null) {
                return userId.toString();
            }
        }

        Principal principal = accessor.getUser();
        if (principal != null) {
            return principal.getName();
        }

        return UserContextHolder.getUserId();
    }

    /**
     * Get the current user ID from a {@link SimpMessageHeaderAccessor}
     * (the typical way to access headers in {@code @MessageMapping} methods).
     */
    public static String getUserId(SimpMessageHeaderAccessor accessor) {
        if (accessor == null) {
            return UserContextHolder.getUserId();
        }

        Principal principal = accessor.getUser();
        if (principal != null) {
            return principal.getName();
        }

        Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
        if (sessionAttrs != null) {
            Object userId = sessionAttrs.get("userId");
            if (userId != null) {
                return userId.toString();
            }
        }

        return UserContextHolder.getUserId();
    }

    /**
     * Convenience method: get the current user ID from the handshake attributes map
     * (available during handshake interception).
     */
    public static String getUserId(Map<String, Object> handshakeAttributes) {
        if (handshakeAttributes == null) {
            return UserContextHolder.getUserId();
        }
        Object userId = handshakeAttributes.get("userId");
        return userId != null ? userId.toString() : UserContextHolder.getUserId();
    }
}
