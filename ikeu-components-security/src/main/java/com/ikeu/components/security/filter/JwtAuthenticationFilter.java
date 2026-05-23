package com.ikeu.components.security.filter;

import com.ikeu.components.security.context.UserContextHolder;
import com.ikeu.components.security.util.JwtUtils;
import com.ikeu.components.core.utils.JsonUtils;
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
 * Extracts a JWT from the configured header, validates it, stores claims in
 * {@link UserContextHolder}, and optionally returns 401 on invalid tokens.
 * <p>
 * Path exclusion uses Ant-style patterns (e.g. {@code /public/**}).
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final String headerName;
    private final String tokenPrefix;
    private final List<String> excludePaths;
    private final boolean failOnInvalid;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtUtils jwtUtils,
                                   String headerName,
                                   String tokenPrefix,
                                   List<String> excludePaths,
                                   boolean failOnInvalid) {
        this.jwtUtils = jwtUtils;
        this.headerName = (headerName != null && !headerName.isBlank()) ? headerName : "Authorization";
        this.tokenPrefix = (tokenPrefix != null) ? tokenPrefix : "Bearer ";
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

            String header = request.getHeader(headerName);
            if (header != null && header.startsWith(tokenPrefix)) {
                String token = header.substring(tokenPrefix.length());
                try {
                    if (!jwtUtils.isExpired(token)) {
                        Claims claims = jwtUtils.parseClaims(token);
                        UserContextHolder.setUserId(claims.getSubject());
                        UserContextHolder.setClaims(claims);
                    } else if (failOnInvalid) {
                        writeError(response, "Token has expired");
                        return;
                    }
                } catch (Exception e) {
                    log.debug("JWT parsing failed: {}", e.getMessage());
                    if (failOnInvalid) {
                        writeError(response, "Invalid token");
                        return;
                    }
                }
            } else if (failOnInvalid && !isExcluded(requestPath)) {
                writeError(response, "Missing or malformed authorization header");
                return;
            }
            filterChain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
        }
    }

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
}