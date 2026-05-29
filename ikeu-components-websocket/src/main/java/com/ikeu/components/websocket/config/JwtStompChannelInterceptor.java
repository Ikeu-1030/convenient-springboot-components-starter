package com.ikeu.components.websocket.config;

import com.ikeu.components.security.context.UserContextHolder;
import com.ikeu.components.security.util.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * STOMP channel interceptor providing:
 * <ul>
 *   <li>CONNECT frame: extracts and validates a JWT token from the
 *       {@code Authorization} header and sets the authenticated user
 *       in the session attributes.</li>
 *   <li>SUBSCRIBE frame: verifies that the authenticated user is allowed
 *       to subscribe to the requested destination via an optional
 *       {@link StompAuthorizationValidator}.</li>
 * </ul>
 */
@Slf4j
public class JwtStompChannelInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final StompAuthorizationValidator authorizationValidator;

    public JwtStompChannelInterceptor(JwtUtils jwtUtils,
                                       StompAuthorizationValidator authorizationValidator) {
        this.jwtUtils = jwtUtils;
        this.authorizationValidator = authorizationValidator;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (command == null) {
            return message;
        }

        switch (command) {
            case CONNECT:
                return handleConnect(message, accessor);
            case SUBSCRIBE:
                return handleSubscribe(message, accessor);
            case DISCONNECT:
                UserContextHolder.clear();
                break;
            default:
                break;
        }
        return message;
    }

    private Message<?> handleConnect(Message<?> message, StompHeaderAccessor accessor) {
        String token = extractToken(accessor);
        if (token == null) {
            log.warn("STOMP CONNECT rejected: no token provided");
            return null;
        }

        try {
            if (jwtUtils.isAccessTokenExpired(token)) {
                log.warn("STOMP CONNECT rejected: token expired");
                return null;
            }
            Claims claims = jwtUtils.parseAccessToken(token);
            String userId = claims.getSubject();

            Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
            if (sessionAttrs == null) {
                sessionAttrs = new HashMap<>();
                accessor.setSessionAttributes(sessionAttrs);
            }
            sessionAttrs.put("userId", userId);
            sessionAttrs.put("claims", claims);
            sessionAttrs.put("loginUser", userId);
            accessor.setUser(new StompPrincipal(userId));

            UserContextHolder.setUserId(userId);
            log.debug("STOMP CONNECT accepted: userId={}", userId);
            return new GenericMessage<>(message.getPayload(), accessor.getMessageHeaders());
        } catch (Exception e) {
            log.warn("STOMP CONNECT rejected: invalid token — {}", e.getMessage());
            return null;
        }
    }

    private Message<?> handleSubscribe(Message<?> message, StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) {
            return message;
        }

        Principal principal = accessor.getUser();
        String userId = principal != null ? principal.getName() : null;
        if (userId == null) {
            Object attr = accessor.getSessionAttributes() != null
                    ? accessor.getSessionAttributes().get("userId") : null;
            userId = attr != null ? attr.toString() : null;
        }

        if (authorizationValidator != null && userId != null) {
            if (!authorizationValidator.canSubscribe(userId, destination)) {
                log.warn("STOMP SUBSCRIBE rejected: userId={}, destination={}", userId, destination);
                return null;
            }
        }

        log.debug("STOMP SUBSCRIBE accepted: userId={}, destination={}", userId, destination);
        return message;
    }

    private String extractToken(StompHeaderAccessor accessor) {
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String auth = authHeaders.get(0);
            if (auth.startsWith("Bearer ")) {
                return auth.substring(7);
            }
        }
        return null;
    }

    /**
     * Simple Principal implementation holding the userId for STOMP session identity.
     */
    private record StompPrincipal(String name) implements Principal {
        @Override
        public String getName() {
            return name;
        }
    }
}
