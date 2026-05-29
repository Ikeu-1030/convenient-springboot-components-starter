package com.ikeu.components.websocket.config;

import com.ikeu.components.security.context.UserContextHolder;
import com.ikeu.components.security.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.security.Principal;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtStompChannelInterceptorTest {

    @Mock private JwtUtils jwtUtils;
    @Mock private StompAuthorizationValidator validator;
    @Mock private MessageChannel channel;

    private JwtStompChannelInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new JwtStompChannelInterceptor(jwtUtils, validator);
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    // ── CONNECT ──

    @Test
    void connectWithValidTokenShouldSetSessionAttributes() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer valid.jwt.token");
        accessor.setSessionAttributes(new HashMap<>());
        Message<?> message = fromAccessor(accessor);

        when(jwtUtils.isAccessTokenExpired("valid.jwt.token")).thenReturn(false);
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user123");
        when(jwtUtils.parseAccessToken("valid.jwt.token")).thenReturn(claims);

        Message<?> result = interceptor.preSend(message, channel);

        assertNotNull(result);
        StompHeaderAccessor resultAccessor = MessageHeaderAccessor.getAccessor(
                result, StompHeaderAccessor.class);
        assertNotNull(resultAccessor);
        assertEquals("user123", resultAccessor.getSessionAttributes().get("userId"));
        assertEquals("user123", resultAccessor.getSessionAttributes().get("loginUser"));
        assertSame(claims, resultAccessor.getSessionAttributes().get("claims"));
        assertNotNull(resultAccessor.getUser());
        assertEquals("user123", resultAccessor.getUser().getName());
        assertEquals("user123", UserContextHolder.getUserId());
    }

    @Test
    void connectWithNoTokenShouldReturnNull() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        Message<?> message = fromAccessor(accessor);

        Message<?> result = interceptor.preSend(message, channel);

        assertNull(result);
    }

    @Test
    void connectWithExpiredTokenShouldReturnNull() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer expired.jwt");
        Message<?> message = fromAccessor(accessor);

        when(jwtUtils.isAccessTokenExpired("expired.jwt")).thenReturn(true);

        assertNull(interceptor.preSend(message, channel));
    }

    @Test
    void connectWithInvalidTokenShouldReturnNull() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer bad.jwt");
        Message<?> message = fromAccessor(accessor);

        when(jwtUtils.isAccessTokenExpired("bad.jwt")).thenReturn(false);
        when(jwtUtils.parseAccessToken("bad.jwt")).thenThrow(new RuntimeException("bad signature"));

        assertNull(interceptor.preSend(message, channel));
    }

    // ── SUBSCRIBE ──

    @Test
    void subscribeToAllowedDestinationShouldPass() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/user/user123/messages");
        accessor.setUser(() -> "user123");
        Message<?> message = fromAccessor(accessor);

        when(validator.canSubscribe("user123", "/user/user123/messages")).thenReturn(true);

        Message<?> result = interceptor.preSend(message, channel);

        assertNotNull(result);
        verify(validator).canSubscribe("user123", "/user/user123/messages");
    }

    @Test
    void subscribeToDeniedDestinationShouldReturnNull() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/user/user999/messages");
        accessor.setUser(() -> "user123");
        Message<?> message = fromAccessor(accessor);

        when(validator.canSubscribe("user123", "/user/user999/messages")).thenReturn(false);

        assertNull(interceptor.preSend(message, channel));
    }

    @Test
    void subscribeWithNullDestinationShouldPass() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setUser(() -> "user123");
        Message<?> message = fromAccessor(accessor);

        Message<?> result = interceptor.preSend(message, channel);

        assertNotNull(result);
        verifyNoInteractions(validator);
    }

    @Test
    void subscribeWithUserIdFromSessionAttributes() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/public/announcements");
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", "user456");
        accessor.setSessionAttributes(sessionAttrs);
        // No Principal set — fallback to session attributes
        Message<?> message = fromAccessor(accessor);

        when(validator.canSubscribe("user456", "/topic/public/announcements")).thenReturn(true);

        Message<?> result = interceptor.preSend(message, channel);

        assertNotNull(result);
        verify(validator).canSubscribe("user456", "/topic/public/announcements");
    }

    @Test
    void subscribeWithNullValidatorShouldPass() {
        JwtStompChannelInterceptor noValidator = new JwtStompChannelInterceptor(jwtUtils, null);
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/any/topic");
        accessor.setUser(() -> "user123");
        Message<?> message = fromAccessor(accessor);

        Message<?> result = noValidator.preSend(message, channel);

        assertNotNull(result);
    }

    // ── DISCONNECT ──

    @Test
    void disconnectShouldClearUserContext() {
        UserContextHolder.setUserId("testUser");
        UserContextHolder.setClaims(Map.of("key", "val"));

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        Message<?> message = fromAccessor(accessor);

        Message<?> result = interceptor.preSend(message, channel);

        assertNotNull(result);
        assertNull(UserContextHolder.getUserId());
        assertTrue(UserContextHolder.getClaims().isEmpty());
    }

    // ── Pass-through cases ──

    @Test
    void nullAccessorShouldPassThrough() {
        // Plain message without a StompHeaderAccessor — should pass through
        Message<?> message = new GenericMessage<>("plain payload");

        Message<?> result = interceptor.preSend(message, channel);

        assertSame(message, result);
    }

    @Test
    void sendCommandShouldPassThrough() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setDestination("/app/chat");
        Message<?> message = fromAccessor(accessor);

        Message<?> result = interceptor.preSend(message, channel);

        assertSame(message, result);
    }

    @Test
    void messageCommandShouldPassThrough() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.MESSAGE);
        Message<?> message = fromAccessor(accessor);

        Message<?> result = interceptor.preSend(message, channel);

        assertSame(message, result);
    }

    // ── Helper ──

    private Message<byte[]> fromAccessor(StompHeaderAccessor accessor) {
        return new GenericMessage<>(new byte[0], accessor.getMessageHeaders());
    }
}
