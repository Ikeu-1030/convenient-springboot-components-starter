package com.ikeu.components.security.util;

import com.ikeu.components.security.model.TokenPair;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT token utilities supporting HMAC-SHA256, single-token and dual-token modes.
 *
 * <h3>SINGLE vs DUAL mode</h3>
 * <table>
 *   <tr><td></td><th>SINGLE</th><th>DUAL</th></tr>
 *   <tr><td>Tokens</td><td>1 (access)</td><td>2 (access + refresh)</td></tr>
 *   <tr><td>Signing keys</td><td>1</td><td>2 (independent)</td></tr>
 *   <tr><td>Access TTL</td><td>24h typical</td><td>2h typical</td></tr>
 *   <tr><td>Refresh TTL</td><td>—</td><td>7d typical</td></tr>
 * </table>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * // Generate token pair
 * Map<String, Object> claims = Map.of("role", "admin");
 * TokenPair pair = jwtUtils.generateTokenPair(userId, claims);
 * // SINGLE mode: pair.refreshToken == null, pair.refreshExpiresIn == 0
 * // DUAL mode: both tokens populated
 *
 * // Refresh access token using refresh token
 * TokenPair newPair = jwtUtils.refreshAccessToken(refreshToken);
 *
 * // Generate access token only
 * String accessToken = jwtUtils.generateAccessToken(userId, claims);
 *
 * // Parse and validate
 * Claims claims = jwtUtils.parseAccessToken(token);
 * String userId = jwtUtils.getUserIdFromAccessToken(token);
 * boolean expired = jwtUtils.isAccessTokenExpired(token);
 * }</pre>
 *
 * <h3>Caveats</h3>
 * <ul>
 *   <li>Secrets shorter than 32 bytes are zero-padded (less entropy)</li>
 *   <li>{@code isExpired()} catches all parse failures and returns {@code true} —
 *       an invalid token is treated as "expired" for safety</li>
 *   <li>On refresh, all custom claims are preserved in the new token</li>
 * </ul>
 *
 * <p>
 * Bean is created by autoconfigure with properties injected.
 * In SINGLE mode the access key is used for all operations.
 * In DUAL mode separate keys sign access and refresh tokens.
 *
 * @author ikeu
 * @since 1.0.0
 */
@Slf4j
public class JwtUtils {

    private final SecretKey accessSigningKey;
    private final SecretKey refreshSigningKey;
    private final Duration accessExpiration;
    private final Duration refreshExpiration;
    private final boolean isDualMode;

    /**
     * Create a JwtUtils supporting dual-token mode.
     *
     * @param accessSecret     secret for access token signing
     * @param refreshSecret    secret for refresh token signing (may be same as access)
     * @param accessExpiration access token TTL
     * @param refreshExpiration refresh token TTL
     * @param dualMode         true = dual token mode with separate keys
     */
    public JwtUtils(String accessSecret, String refreshSecret,
                    Duration accessExpiration, Duration refreshExpiration,
                    boolean dualMode) {
        this.accessSigningKey = createSigningKey(accessSecret);
        this.refreshSigningKey = dualMode ? createSigningKey(refreshSecret) : this.accessSigningKey;
        this.accessExpiration = (accessExpiration != null) ? accessExpiration : Duration.ofHours(2);
        this.refreshExpiration = (refreshExpiration != null) ? refreshExpiration : Duration.ofDays(7);
        this.isDualMode = dualMode;
    }

    // ──────────────────────────────────────────────
    // Access token
    // ──────────────────────────────────────────────

    /** Generate an access token with the default access TTL. */
    public String generateAccessToken(String userId, Map<String, Object> claims) {
        return generateAccessToken(userId, claims, accessExpiration);
    }

    /** Generate an access token with a custom TTL. */
    public String generateAccessToken(String userId, Map<String, Object> claims,
                                       Duration duration) {
        Instant now = Instant.now();
        Duration ttl = (duration != null) ? duration : accessExpiration;
        Instant expiry = now.plus(ttl);

        Map<String, Object> allClaims = new HashMap<>();
        if (claims != null) {
            allClaims.putAll(claims);
        }
        allClaims.put("tok", "access");

        return Jwts.builder()
                .subject(userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claims(allClaims)
                .signWith(accessSigningKey)
                .compact();
    }

    /** Parse and validate an access token. */
    public Claims parseAccessToken(String token) {
        return Jwts.parser()
                .verifyWith(accessSigningKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Extract userId (subject) from an access token. */
    public String getUserIdFromAccessToken(String token) {
        return parseAccessToken(token).getSubject();
    }

    /** Check if an access token is expired. */
    public boolean isAccessTokenExpired(String token) {
        try {
            return parseAccessToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    // ──────────────────────────────────────────────
    // Refresh token
    // ──────────────────────────────────────────────

    /** Generate a refresh token with the default refresh TTL. */
    public String generateRefreshToken(String userId, Map<String, Object> claims) {
        return generateRefreshToken(userId, claims, refreshExpiration);
    }

    /** Generate a refresh token with a custom TTL. */
    public String generateRefreshToken(String userId, Map<String, Object> claims,
                                        Duration duration) {
        Instant now = Instant.now();
        Duration ttl = (duration != null) ? duration : refreshExpiration;
        Instant expiry = now.plus(ttl);

        Map<String, Object> allClaims = new HashMap<>();
        if (claims != null) {
            allClaims.putAll(claims);
        }
        allClaims.put("tok", "refresh");

        return Jwts.builder()
                .subject(userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claims(allClaims)
                .signWith(refreshSigningKey)
                .compact();
    }

    /** Parse and validate a refresh token. */
    public Claims parseRefreshToken(String token) {
        return Jwts.parser()
                .verifyWith(refreshSigningKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Extract userId (subject) from a refresh token. */
    public String getUserIdFromRefreshToken(String token) {
        return parseRefreshToken(token).getSubject();
    }

    /** Check if a refresh token is expired. */
    public boolean isRefreshTokenExpired(String token) {
        try {
            return parseRefreshToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    // ──────────────────────────────────────────────
    // Token pair (dual mode)
    // ──────────────────────────────────────────────

    /**
     * Generate an access + refresh token pair.
     * <p>
     * In SINGLE mode, returns a pair where refreshToken is null and
     * refreshExpiresIn is 0 — only the access token is populated.
     *
     * @param userId user identifier put into the {@code sub} claim
     * @param claims custom claims included in both tokens
     * @return a TokenPair with one or both tokens populated
     */
    public TokenPair generateTokenPair(String userId, Map<String, Object> claims) {
        String accessToken = generateAccessToken(userId, claims);
        long accessExpires = accessExpiration.toSeconds();

        if (!isDualMode) {
            return TokenPair.builder()
                    .accessToken(accessToken)
                    .refreshToken(null)
                    .expiresIn(accessExpires)
                    .refreshExpiresIn(0)
                    .build();
        }

        String refreshToken = generateRefreshToken(userId, claims);
        long refreshExpires = refreshExpiration.toSeconds();
        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(accessExpires)
                .refreshExpiresIn(refreshExpires)
                .build();
    }

    /**
     * Refresh an access token using a valid refresh token.
     * Parses the refresh token, extracts custom claims, and issues a new access token.
     *
     * @param refreshToken the valid refresh token
     * @return a new TokenPair (new access + same refresh, or just access in SINGLE mode)
     */
    public TokenPair refreshAccessToken(String refreshToken) {
        Claims claims = parseRefreshToken(refreshToken);
        String userId = claims.getSubject();

        Map<String, Object> customClaims = extractCustomClaims(claims);
        return generateTokenPair(userId, customClaims);
    }

    // ──────────────────────────────────────────────
    // Backward-compatible methods (delegate to access token)
    // ──────────────────────────────────────────────

    /**
     * Generate a token (SINGLE mode convenience, or access token in DUAL mode).
     * @deprecated Prefer {@link #generateAccessToken(String, Map)} for clarity.
     */
    @Deprecated
    public String generateToken(String userId, Map<String, Object> claims,
                                 Duration duration) {
        return generateAccessToken(userId, claims, duration);
    }

    /**
     * Parse claims from a token.
     * @deprecated Prefer {@link #parseAccessToken(String)} for clarity.
     */
    @Deprecated
    public Claims parseClaims(String token) {
        return parseAccessToken(token);
    }

    /**
     * Extract userId from a token.
     * @deprecated Prefer {@link #getUserIdFromAccessToken(String)} for clarity.
     */
    @Deprecated
    public String getUserId(String token) {
        return getUserIdFromAccessToken(token);
    }

    /**
     * Check if a token is expired.
     * @deprecated Prefer {@link #isAccessTokenExpired(String)} for clarity.
     */
    @Deprecated
    public boolean isExpired(String token) {
        return isAccessTokenExpired(token);
    }

    /**
     * Refresh a token.
     * @deprecated Prefer {@link #refreshAccessToken(String)} for clarity.
     */
    @Deprecated
    public String refreshToken(String token, Duration newDuration) {
        Claims claims = parseAccessToken(token);
        String userId = claims.getSubject();
        Map<String, Object> customClaims = extractCustomClaims(claims);
        return generateAccessToken(userId, customClaims, newDuration);
    }

    // ──────────────────────────────────────────────
    // Internal helpers
    // ──────────────────────────────────────────────

    private static SecretKey createSigningKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    private static Map<String, Object> extractCustomClaims(Claims claims) {
        Map<String, Object> custom = new HashMap<>(claims);
        custom.remove(Claims.SUBJECT);
        custom.remove(Claims.ISSUED_AT);
        custom.remove(Claims.EXPIRATION);
        custom.remove(Claims.ID);
        custom.remove(Claims.ISSUER);
        custom.remove(Claims.AUDIENCE);
        return custom;
    }
}