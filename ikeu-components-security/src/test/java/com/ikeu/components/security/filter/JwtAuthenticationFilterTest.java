package com.ikeu.components.security.filter;

import com.ikeu.components.security.context.UserContextHolder;
import com.ikeu.components.security.util.JwtUtils;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationFilterTest {

    private static final String ACCESS_SECRET = "access-secret-key-32chars-min!";
    private static final String REFRESH_SECRET = "refresh-secret-key-32chars-min!";

    private JwtUtils jwtUtils;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(ACCESS_SECRET, REFRESH_SECRET,
                Duration.ofHours(2), Duration.ofDays(7), false);
        filter = new JwtAuthenticationFilter(jwtUtils,
                "Authorization", "Bearer ", "X-Refresh-Token", "Bearer ",
                false, Collections.emptyList(), true);
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void validAccessToken_setsContext() throws Exception {
        String token = jwtUtils.generateAccessToken("user123", Map.of("role", "admin"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> assertEquals("user123", UserContextHolder.getUserId());

        filter.doFilter(request, response, chain);
    }

    @Test
    void missingToken_failOnInvalid_returns401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> fail("Should not proceed");

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
    }

    @Test
    void missingToken_failOnInvalid_false_passes() throws Exception {
        JwtAuthenticationFilter lenient = new JwtAuthenticationFilter(jwtUtils,
                "Authorization", "Bearer ", null, null,
                false, Collections.emptyList(), false);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> { /* passes through */ };

        lenient.doFilter(request, response, chain);
        assertEquals(200, response.getStatus());
    }

    @Test
    void excludedPath_passesWithoutToken() throws Exception {
        JwtAuthenticationFilter excludeFilter = new JwtAuthenticationFilter(jwtUtils,
                "Authorization", "Bearer ", null, null,
                false, Collections.singletonList("/public/**"), true);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/public/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> { /* OK */ };

        excludeFilter.doFilter(request, response, chain);
        assertEquals(200, response.getStatus());
    }

    @Test
    void contextClearedAfterFilter() throws Exception {
        String token = jwtUtils.generateAccessToken("user", null);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {};

        filter.doFilter(request, response, chain);

        assertNull(UserContextHolder.getUserId());
    }

    @Test
    void dualMode_refreshTokenFallback() throws Exception {
        JwtUtils dualJwt = new JwtUtils(ACCESS_SECRET, REFRESH_SECRET,
                Duration.ofHours(2), Duration.ofDays(7), true);
        JwtAuthenticationFilter dualFilter = new JwtAuthenticationFilter(dualJwt,
                "Authorization", "Bearer ", "X-Refresh-Token", "Bearer ",
                true, Collections.emptyList(), true);

        String refreshToken = dualJwt.generateRefreshToken("user", null);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Refresh-Token", "Bearer " + refreshToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> assertEquals("user", UserContextHolder.getUserId());

        dualFilter.doFilter(request, response, chain);
    }
}