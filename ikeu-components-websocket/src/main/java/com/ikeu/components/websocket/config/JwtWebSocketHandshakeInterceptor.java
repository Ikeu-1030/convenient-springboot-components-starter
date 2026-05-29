package com.ikeu.components.websocket.config;

import com.ikeu.components.security.context.UserContextHolder;
import com.ikeu.components.security.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * WebSocket handshake interceptor that validates a JWT token before
 * upgrading the HTTP connection to a WebSocket.
 * <p>
 * Token is extracted from:
 * <ol>
 *   <li>Query parameter {@code access_token} (SockJS fallback)</li>
 *   <li>{@code Authorization} header (native WebSocket)</li>
 * </ol>
 * Validated user info is placed in the handshake {@code attributes}
 * under {@code loginUser}, {@code userId}, and {@code claims}.
 */
@Slf4j
public class JwtWebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwtUtils;
    private final String tokenParam;
    private final List<String> whitelistPaths;

    public JwtWebSocketHandshakeInterceptor(JwtUtils jwtUtils,
                                             String tokenParam,
                                             List<String> whitelistPaths) {
        this.jwtUtils = jwtUtils;
        this.tokenParam = tokenParam != null ? tokenParam : "access_token";
        this.whitelistPaths = whitelistPaths != null ? whitelistPaths : Collections.emptyList();
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        String path = request.getURI().getPath();
        if (isWhitelisted(path)) {
            return true;
        }

        String token = extractToken(request);
        if (token == null) {
            log.warn("WebSocket handshake rejected: no token in request to {}", path);
            return false;
        }

        try {
            if (jwtUtils.isAccessTokenExpired(token)) {
                log.warn("WebSocket handshake rejected: expired token for {}", path);
                return false;
            }
            Claims claims = jwtUtils.parseAccessToken(token);
            String userId = claims.getSubject();
            attributes.put("userId", userId);
            attributes.put("claims", claims);
            attributes.put("loginUser", userId);
            log.debug("WebSocket handshake accepted: userId={}, path={}", userId, path);
            return true;
        } catch (Exception e) {
            log.warn("WebSocket handshake rejected: invalid token for {} — {}", path, e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }

    private String extractToken(ServerHttpRequest request) {
        // 1. Query parameter (SockJS)
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpReq = servletRequest.getServletRequest();
            String token = httpReq.getParameter(tokenParam);
            if (token != null && !token.isBlank()) {
                return token;
            }
        }

        // 2. Authorization header
        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }

        // 3. Raw query string parsing
        String query = request.getURI().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2 && tokenParam.equals(kv[0])) {
                    return kv[1];
                }
            }
        }
        return null;
    }

    private boolean isWhitelisted(String path) {
        for (String whitelisted : whitelistPaths) {
            if (path.startsWith(whitelisted)) {
                return true;
            }
        }
        return false;
    }
}
