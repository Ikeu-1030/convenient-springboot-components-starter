package com.ikeu.components.websocket.config;

import com.ikeu.components.security.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtWebSocketHandshakeInterceptorTest {

    @Mock private JwtUtils jwtUtils;
    @Mock private ServerHttpResponse response;
    @Mock private WebSocketHandler wsHandler;

    private JwtWebSocketHandshakeInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new JwtWebSocketHandshakeInterceptor(
                jwtUtils, "access_token", List.of("/public/ws"));
    }

    @AfterEach
    void tearDown() {
        com.ikeu.components.security.context.UserContextHolder.clear();
    }

    // ── Token from query param (SockJS / ServletServerHttpRequest) ──

    @Test
    void shouldAcceptWhenTokenFromQueryParamIsValid() {
        ServerHttpRequest request = servletRequest("/ws/chat", "access_token=valid.jwt.token", "token1");
        when(jwtUtils.isAccessTokenExpired("token1")).thenReturn(false);
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user123");
        when(jwtUtils.parseAccessToken("token1")).thenReturn(claims);

        Map<String, Object> attributes = new HashMap<>();
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        assertTrue(result);
        assertEquals("user123", attributes.get("userId"));
        assertEquals("user123", attributes.get("loginUser"));
        assertSame(claims, attributes.get("claims"));
    }

    @Test
    void shouldRejectWhenTokenFromQueryParamIsExpired() {
        ServerHttpRequest request = servletRequest("/ws/chat", "access_token=expired.jwt", "exp1");
        when(jwtUtils.isAccessTokenExpired("exp1")).thenReturn(true);

        Map<String, Object> attributes = new HashMap<>();
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        assertFalse(result);
        assertNull(attributes.get("userId"));
    }

    @Test
    void shouldRejectWhenTokenFromQueryParamIsInvalid() {
        ServerHttpRequest request = servletRequest("/ws/chat", "access_token=bad.jwt", "bad1");
        when(jwtUtils.isAccessTokenExpired("bad1")).thenReturn(false);
        when(jwtUtils.parseAccessToken("bad1")).thenThrow(new RuntimeException("invalid"));

        Map<String, Object> attributes = new HashMap<>();
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        assertFalse(result);
    }

    // ── Token from Authorization header ──

    @Test
    void shouldAcceptWhenTokenFromAuthHeaderIsValid() {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(request.getURI()).thenReturn(URI.create("/ws/chat"));
        org.springframework.http.HttpHeaders httpHeaders = new org.springframework.http.HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer header.token.xyz");
        when(request.getHeaders()).thenReturn(httpHeaders);

        when(jwtUtils.isAccessTokenExpired("header.token.xyz")).thenReturn(false);
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user789");
        when(jwtUtils.parseAccessToken("header.token.xyz")).thenReturn(claims);

        Map<String, Object> attributes = new HashMap<>();
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        assertTrue(result);
        assertEquals("user789", attributes.get("userId"));
    }

    @Test
    void shouldRejectWhenAuthHeaderHasExpiredToken() {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(request.getURI()).thenReturn(URI.create("/ws/chat"));
        org.springframework.http.HttpHeaders httpHeaders = new org.springframework.http.HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer expired.token");
        when(request.getHeaders()).thenReturn(httpHeaders);

        when(jwtUtils.isAccessTokenExpired("expired.token")).thenReturn(true);

        Map<String, Object> attributes = new HashMap<>();
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        assertFalse(result);
    }

    // ── No token ──

    @Test
    void shouldRejectWhenNoTokenProvided() {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(request.getURI()).thenReturn(URI.create("/ws/chat"));
        when(request.getHeaders()).thenReturn(new org.springframework.http.HttpHeaders());

        Map<String, Object> attributes = new HashMap<>();
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        assertFalse(result);
    }

    // ── Whitelist ──

    @Test
    void shouldBypassAuthWhenPathIsWhitelisted() {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(request.getURI()).thenReturn(URI.create("/public/ws/health"));
        // no token headers set — would fail without whitelist

        Map<String, Object> attributes = new HashMap<>();
        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        assertTrue(result);
        verifyNoInteractions(jwtUtils);
    }

    // ── Default constructor values ──

    @Test
    void shouldDefaultTokenParamToAccessToken() {
        JwtWebSocketHandshakeInterceptor defaultInterceptor =
                new JwtWebSocketHandshakeInterceptor(jwtUtils, null, null);

        ServerHttpRequest request = servletRequest("/ws", "access_token=abc.def", "abc.def");
        when(jwtUtils.isAccessTokenExpired("abc.def")).thenReturn(false);
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("u1");
        when(jwtUtils.parseAccessToken("abc.def")).thenReturn(claims);

        boolean result = defaultInterceptor.beforeHandshake(request, response, wsHandler, new HashMap<>());
        assertTrue(result);
    }

    // ── afterHandshake is a no-op ──

    @Test
    void afterHandshakeShouldNotThrow() {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        assertDoesNotThrow(() ->
                interceptor.afterHandshake(request, response, wsHandler, null));
    }

    // ── Helper to build ServletServerHttpRequest ──

    private ServerHttpRequest servletRequest(String path, String queryString, String tokenParamValue) {
        HttpServletRequest httpReq = mock(HttpServletRequest.class);
        when(httpReq.getParameter("access_token")).thenReturn(tokenParamValue);
        ServletServerHttpRequest req = new ServletServerHttpRequest(httpReq);
        // override URI to include path + query so raw-query fallback doesn't interfere
        try {
            java.lang.reflect.Field uriField = org.springframework.http.server.ServletServerHttpRequest.class
                    .getDeclaredField("uri");
            uriField.setAccessible(true);
            uriField.set(req, new URI(null, null, path, queryString, null));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return req;
    }
}
