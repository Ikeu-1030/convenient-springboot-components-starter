package com.ikeu.components.security.util;

import com.ikeu.components.security.model.TokenPair;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private static final String ACCESS_SECRET = "access-secret-key-32chars-min!";
    private static final String REFRESH_SECRET = "refresh-secret-key-32chars-min!";

    private JwtUtils singleMode;
    private JwtUtils dualMode;

    @BeforeEach
    void setUp() {
        singleMode = new JwtUtils(ACCESS_SECRET, REFRESH_SECRET,
                Duration.ofHours(2), Duration.ofDays(7), false);
        dualMode = new JwtUtils(ACCESS_SECRET, REFRESH_SECRET,
                Duration.ofMinutes(15), Duration.ofDays(7), true);
    }

    // ── Access token ──

    @Test
    void generateAndParseAccessToken() {
        String token = singleMode.generateAccessToken("user123", Map.of("role", "admin"));
        Claims claims = singleMode.parseAccessToken(token);
        assertEquals("user123", claims.getSubject());
        assertEquals("admin", claims.get("role"));
        assertEquals("access", claims.get("tok"));
    }

    @Test
    void getUserIdFromAccessToken() {
        String token = singleMode.generateAccessToken("user456", null);
        assertEquals("user456", singleMode.getUserIdFromAccessToken(token));
    }

    @Test
    void isAccessTokenExpired_validToken() {
        String token = singleMode.generateAccessToken("user", null);
        assertFalse(singleMode.isAccessTokenExpired(token));
    }

    @Test
    void isAccessTokenExpired_expiredToken() {
        JwtUtils shortLived = new JwtUtils(ACCESS_SECRET, REFRESH_SECRET,
                Duration.ofMillis(1), Duration.ofDays(7), false);
        String token = shortLived.generateAccessToken("user", null);
        assertTrue(shortLived.isAccessTokenExpired(token));
    }

    @Test
    void isAccessTokenExpired_invalidToken() {
        assertTrue(singleMode.isAccessTokenExpired("garbage-token"));
        assertTrue(singleMode.isAccessTokenExpired(null));
    }

    @Test
    void accessTokenHasTokClaim() {
        String token = singleMode.generateAccessToken("user", null);
        Claims claims = singleMode.parseAccessToken(token);
        assertEquals("access", claims.get("tok"));
    }

    @Test
    void customClaimsPreserved() {
        Map<String, Object> claims = Map.of("role", "admin", "dept", "IT");
        String token = singleMode.generateAccessToken("user", claims);
        Claims parsed = singleMode.parseAccessToken(token);
        assertEquals("admin", parsed.get("role"));
        assertEquals("IT", parsed.get("dept"));
    }

    // ── Refresh token ──

    @Test
    void generateAndParseRefreshToken() {
        String token = dualMode.generateRefreshToken("user123", Map.of("role", "user"));
        Claims claims = dualMode.parseRefreshToken(token);
        assertEquals("user123", claims.getSubject());
        assertEquals("refresh", claims.get("tok"));
    }

    @Test
    void refreshTokenHasRefreshClaim() {
        String token = dualMode.generateRefreshToken("user", null);
        Claims claims = dualMode.parseRefreshToken(token);
        assertEquals("refresh", claims.get("tok"));
    }

    // ── Token pair ──

    @Test
    void singleMode_generateTokenPair_refreshNull() {
        TokenPair pair = singleMode.generateTokenPair("user", Map.of("role", "admin"));
        assertNotNull(pair.getAccessToken());
        assertNull(pair.getRefreshToken());
        assertEquals(0, pair.getRefreshExpiresIn());
        assertTrue(pair.getExpiresIn() > 0);
    }

    @Test
    void dualMode_generateTokenPair_bothTokens() {
        TokenPair pair = dualMode.generateTokenPair("user", Map.of("role", "admin"));
        assertNotNull(pair.getAccessToken());
        assertNotNull(pair.getRefreshToken());
        assertTrue(pair.getExpiresIn() > 0);
        assertTrue(pair.getRefreshExpiresIn() > 0);
    }

    @Test
    void dualMode_refreshAccessToken_preservesClaims() {
        Map<String, Object> claims = Map.of("role", "admin");
        TokenPair original = dualMode.generateTokenPair("user", claims);
        TokenPair refreshed = dualMode.refreshAccessToken(original.getRefreshToken());
        assertNotNull(refreshed.getAccessToken());
        Claims newClaims = dualMode.parseAccessToken(refreshed.getAccessToken());
        assertEquals("admin", newClaims.get("role"));
    }

    @Test
    void dualMode_2hourAccess_7dayRefresh() {
        JwtUtils custom = new JwtUtils(ACCESS_SECRET, REFRESH_SECRET,
                Duration.ofHours(2), Duration.ofDays(7), true);
        assertEquals(7200, custom.generateTokenPair("user", null).getExpiresIn());
        assertEquals(604800, custom.generateTokenPair("user", null).getRefreshExpiresIn());
    }

    // ── Deprecated methods ──

    @Test
    @SuppressWarnings("deprecation")
    void deprecatedGenerateToken_delegatesToAccessToken() {
        String token = singleMode.generateToken("user", Map.of("key", "val"),
                Duration.ofHours(1));
        assertEquals("user", singleMode.getUserIdFromAccessToken(token));
        assertEquals("val", singleMode.parseAccessToken(token).get("key"));
    }

    @Test
    @SuppressWarnings("deprecation")
    void deprecatedGetUserId_delegatesToAccessToken() {
        String token = singleMode.generateAccessToken("user789", null);
        assertEquals("user789", singleMode.getUserId(token));
    }

    @Test
    @SuppressWarnings("deprecation")
    void deprecatedIsExpired_delegatesToAccessToken() {
        String token = singleMode.generateAccessToken("user", null);
        assertFalse(singleMode.isExpired(token));
    }
}