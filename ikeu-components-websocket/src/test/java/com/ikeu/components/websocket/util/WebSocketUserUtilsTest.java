package com.ikeu.components.websocket.util;

import com.ikeu.components.security.context.UserContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WebSocketUserUtilsTest {

    @BeforeEach
    void setUp() {
        UserContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    // ── getUserId(StompHeaderAccessor) ──

    @Test
    void getUserIdFromStompAccessorWithSessionAttributes() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", "user999");
        accessor.setSessionAttributes(sessionAttrs);

        assertEquals("user999", WebSocketUserUtils.getUserId(accessor));
    }

    @Test
    void getUserIdFromStompAccessorWithPrincipal() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setUser(() -> "principalUser");

        assertEquals("principalUser", WebSocketUserUtils.getUserId(accessor));
    }

    @Test
    void getUserIdFromStompAccessorSessionTakesPriorityOverPrincipal() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", "sessionUser");
        accessor.setSessionAttributes(sessionAttrs);
        accessor.setUser(() -> "principalUser");

        // session attribute is checked first
        assertEquals("sessionUser", WebSocketUserUtils.getUserId(accessor));
    }

    @Test
    void getUserIdFromNullStompAccessorFallsBackToThreadLocal() {
        UserContextHolder.setUserId("threadUser");

        assertEquals("threadUser", WebSocketUserUtils.getUserId((StompHeaderAccessor) null));
    }

    @Test
    void getUserIdFromEmptyStompAccessorFallsBackToThreadLocal() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);

        UserContextHolder.setUserId("threadUser");
        assertEquals("threadUser", WebSocketUserUtils.getUserId(accessor));
    }

    // ── getUserId(SimpMessageHeaderAccessor) ──

    @Test
    void getUserIdFromSimpAccessorWithPrincipal() {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();
        accessor.setUser(() -> "simpPrincipalUser");

        assertEquals("simpPrincipalUser", WebSocketUserUtils.getUserId(accessor));
    }

    @Test
    void getUserIdFromSimpAccessorWithSessionAttributes() {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("userId", "simpSessionUser");
        accessor.setSessionAttributes(sessionAttrs);

        assertEquals("simpSessionUser", WebSocketUserUtils.getUserId(accessor));
    }

    @Test
    void getUserIdFromNullSimpAccessorFallsBackToThreadLocal() {
        UserContextHolder.setUserId("threadUser");

        assertEquals("threadUser", WebSocketUserUtils.getUserId((SimpMessageHeaderAccessor) null));
    }

    @Test
    void getUserIdFromEmptySimpAccessorFallsBackToThreadLocal() {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();

        UserContextHolder.setUserId("threadUser");
        assertEquals("threadUser", WebSocketUserUtils.getUserId(accessor));
    }

    // ── getUserId(Map) ──

    @Test
    void getUserIdFromMapWithUserIdKey() {
        Map<String, Object> attrs = Map.of("userId", "mapUser");

        assertEquals("mapUser", WebSocketUserUtils.getUserId(attrs));
    }

    @Test
    void getUserIdFromMapWithoutUserIdKeyFallsBackToThreadLocal() {
        Map<String, Object> attrs = Map.of("foo", "bar");
        UserContextHolder.setUserId("fallbackUser");

        assertEquals("fallbackUser", WebSocketUserUtils.getUserId(attrs));
    }

    @Test
    void getUserIdFromNullMapFallsBackToThreadLocal() {
        UserContextHolder.setUserId("nullMapUser");

        assertEquals("nullMapUser", WebSocketUserUtils.getUserId((Map<String, Object>) null));
    }

    // ── ThreadLocal isolation ──

    @Test
    void getUserIdFromNullAccessorWithNoThreadLocalReturnsNull() {
        UserContextHolder.clear();

        assertNull(WebSocketUserUtils.getUserId((StompHeaderAccessor) null));
        assertNull(WebSocketUserUtils.getUserId((SimpMessageHeaderAccessor) null));
        assertNull(WebSocketUserUtils.getUserId((Map<String, Object>) null));
    }
}
