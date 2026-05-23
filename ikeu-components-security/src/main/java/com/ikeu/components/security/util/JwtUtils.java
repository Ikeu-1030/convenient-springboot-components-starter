package com.ikeu.components.security.util;

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
 * JWT token utilities supporting HMAC-SHA256.
 * Bean is created by autoconfigure with properties injected.
 */
@Slf4j
public class JwtUtils {

    private final SecretKey signingKey;
    private final Duration defaultExpiration;

    /**
     * @param secret            HMAC secret key string
     * @param defaultExpiration default token expiration duration
     */
    public JwtUtils(String secret, Duration defaultExpiration) {
        this.signingKey = createSigningKey(secret);
        this.defaultExpiration = defaultExpiration != null ? defaultExpiration : Duration.ofHours(24);
    }

    /**
     * Generate a JWT token with userId as subject and custom claims.
     */
    public String generateToken(String userId, Map<String, Object> claims, Duration duration) {
        Instant now = Instant.now();
        Instant expiry = now.plus(duration != null ? duration : defaultExpiration);

        Map<String, Object> allClaims = new HashMap<>();
        if (claims != null) {
            allClaims.putAll(claims);
        }

        return Jwts.builder()
                .subject(userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claims(allClaims)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Parse all claims from a JWT token.
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract the userId (subject) from a token.
     */
    public String getUserId(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Check whether a token is expired.
     */
    public boolean isExpired(String token) {
        try {
            return parseClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Refresh a token: parse existing claims, preserve all custom claims, issue with new expiry.
     */
    public String refreshToken(String token, Duration newDuration) {
        Claims claims = parseClaims(token);
        String userId = claims.getSubject();

        Map<String, Object> customClaims = new HashMap<>(claims);
        customClaims.remove(Claims.SUBJECT);
        customClaims.remove(Claims.ISSUED_AT);
        customClaims.remove(Claims.EXPIRATION);

        return generateToken(userId, customClaims, newDuration);
    }

    private static SecretKey createSigningKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }
}
