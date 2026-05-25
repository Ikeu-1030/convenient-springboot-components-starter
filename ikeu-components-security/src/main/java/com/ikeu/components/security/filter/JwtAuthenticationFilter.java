package com.ikeu.components.security.filter;

import com.ikeu.components.core.utils.JsonUtils;
import com.ikeu.components.security.context.UserContextHolder;
import com.ikeu.components.security.util.JwtUtils;
import com.ikeu.components.web.model.Result;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Extracts JWT from configured headers, validates it, stores claims in
 * {@link UserContextHolder}, and optionally returns 401 on invalid tokens.
 *
 * <h3>Processing flow</h3>
 * <ol>
 *   <li>Check path exclusion (Ant-style patterns)</li>
 *   <li>Extract access token from configured header</li>
 *   <li>Validate access token → set user context</li>
 *   <li>If DUAL mode and access token missing, fall back to refresh token</li>
 *   <li>Clear context in {@code finally} block</li>
 * </ol>
 *
 * <h3>Header handling</h3>
 * <table>
 *   <tr><th>Mode</th><th>Access header</th><th>Refresh header</th></tr>
 *   <tr><td>SINGLE</td><td>{@code Authorization: Bearer <token>}</td><td>—</td></tr>
 *   <tr><td>DUAL</td><td>{@code Authorization: Bearer <token>}</td><td>{@code X-Refresh-Token: Bearer <token>}</td></tr>
 * </table>
 * <p>
 * Path exclusion uses Ant-style patterns (e.g. {@code /public/**}).
 * The token type ("access" or "refresh") is stored in the {@code "tok"} claim
 * for downstream inspection.
 *
 * @author ikeu
 * @since 1.0.0
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final String accessHeaderName;
    private final String accessTokenPrefix;
    private final String refreshHeaderName;
    private final String refreshTokenPrefix;
    private final boolean isDualMode;
    private final List<String> excludePaths;
    private final boolean failOnInvalid;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtUtils jwtUtils,
                                   String accessHeaderName,
                                   String accessTokenPrefix,
                                   String refreshHeaderName,
                                   String refreshTokenPrefix,
                                   boolean isDualMode,
                                   List<String> excludePaths,
                                   boolean failOnInvalid) {
        this.jwtUtils = jwtUtils;
        this.accessHeaderName = blankTo(accessHeaderName, "Authorization");
        this.accessTokenPrefix = blankTo(accessTokenPrefix, "Bearer ");
        this.refreshHeaderName = blankTo(refreshHeaderName, "X-Refresh-Token");
        this.refreshTokenPrefix = blankTo(refreshTokenPrefix, "Bearer ");
        this.isDualMode = isDualMode;
        this.excludePaths = (excludePaths != null) ? excludePaths : Collections.emptyList();
        this.failOnInvalid = failOnInvalid;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String requestPath = request.getRequestURI();
            if (isExcluded(requestPath)) {
                filterChain.doFilter(request, response);
                return;
            }

            // ── Try access token first ──
            String accessToken = extractToken(request, accessHeaderName, accessTokenPrefix);
            if (accessToken != null) {
                if (tryAuthenticateAccessToken(accessToken, response)) {
                    filterChain.doFilter(request, response);
                    return;
                }
                // failOnInvalid handled inside — if we get here, failOnInvalid=false
            }

            // ── Dual mode: try refresh token as fallback ──
            if (isDualMode) {
                String refreshToken = extractToken(request, refreshHeaderName, refreshTokenPrefix);
                if (refreshToken != null) {
                    if (tryAuthenticateRefreshToken(refreshToken, response)) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                }
            }

            // ── No valid token found ──
            if (failOnInvalid) {
                writeError(response, "Missing or invalid token");
                return;
            }
            filterChain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
        }
    }

    // ── Token extraction ──

    private String extractToken(HttpServletRequest request, String headerName,
                                 String prefix) {
        String header = request.getHeader(headerName);
        if (header != null && header.startsWith(prefix)) {
            return header.substring(prefix.length());
        }
        return null;
    }

    // ── Authentication ──

    private boolean tryAuthenticateAccessToken(String token,
                                                HttpServletResponse response)
            throws IOException {
        try {
            if (!jwtUtils.isAccessTokenExpired(token)) {
                Claims claims = jwtUtils.parseAccessToken(token);
                UserContextHolder.setUserId(claims.getSubject());
                UserContextHolder.setClaims(claims);
                return true;
            }
            if (failOnInvalid) {
                writeError(response, "Access token has expired");
            }
        } catch (Exception e) {
            log.debug("Access token parsing failed: {}", e.getMessage());
            if (failOnInvalid) {
                writeError(response, "Invalid access token");
            }
        }
        return false;
    }

    private boolean tryAuthenticateRefreshToken(String token,
                                                 HttpServletResponse response)
            throws IOException {
        try {
            if (!jwtUtils.isRefreshTokenExpired(token)) {
                Claims claims = jwtUtils.parseRefreshToken(token);
                UserContextHolder.setUserId(claims.getSubject());
                UserContextHolder.setClaims(claims);
                return true;
            }
            if (failOnInvalid) {
                writeError(response, "Refresh token has expired");
            }
        } catch (Exception e) {
            log.debug("Refresh token parsing failed: {}", e.getMessage());
            if (failOnInvalid) {
                writeError(response, "Invalid refresh token");
            }
        }
        return false;
    }

    // ── Utilities ──

    private boolean isExcluded(String requestPath) {
        for (String pattern : excludePaths) {
            if (pathMatcher.match(pattern, requestPath)) {
                return true;
            }
        }
        return false;
    }

    private void writeError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtils.toJson(
                Result.error(HttpServletResponse.SC_UNAUTHORIZED, message)));
    }

    private static String blankTo(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }
}