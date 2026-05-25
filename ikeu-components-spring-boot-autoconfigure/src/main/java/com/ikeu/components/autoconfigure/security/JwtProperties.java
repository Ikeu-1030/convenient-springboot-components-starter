package com.ikeu.components.autoconfigure.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for JWT, prefix "ikeu.jwt".
 * <p>
 * Supports two token modes:
 * <ul>
 *   <li><b>SINGLE</b> — one token, one secret. Access token acts as both
 *       authentication and refresh credential.</li>
 *   <li><b>DUAL</b> — separate access token (short-lived) and refresh token
 *       (long-lived), each with its own secret, TTL, header, and prefix.</li>
 * </ul>
 *
 * <pre>{@code
 * # SINGLE mode (default, backward-compatible)
 * ikeu.jwt.enabled=true
 * ikeu.jwt.mode=single
 * ikeu.jwt.secret=my-secret
 * ikeu.jwt.expiration=24h
 *
 * # DUAL mode
 * ikeu.jwt.enabled=true
 * ikeu.jwt.mode=dual
 * ikeu.jwt.access-secret=access-key-32chars-min-xxxxxx
 * ikeu.jwt.access-expiration=2h
 * ikeu.jwt.refresh-secret=refresh-key-32chars-min-xxxxx
 * ikeu.jwt.refresh-expiration=7d
 * }</pre>
 * @author ikeu
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "ikeu.jwt")
public class JwtProperties {

    /**
     * Token mode.
     * <ul>
     *   <li>{@code single} — one token (access token only)</li>
     *   <li>{@code dual} — access + refresh token pair</li>
     * </ul>
     */
    private TokenMode mode = TokenMode.SINGLE;

    /** Enable JWT auto-configuration. */
    private boolean enabled = false;

    // ── Shared / legacy properties (used as fallback in both modes) ──

    /**
     * Default HMAC-SHA256 secret key.
     * In SINGLE mode this is the only secret.
     * In DUAL mode, if {@code access-secret} or {@code refresh-secret} is
     * not set, this value is used as fallback.
     */
    private String secret = "ikeu-components-default-secret-key-change-me";

    /** Default token expiration. Fallback when per-token TTL is not set. */
    private Duration expiration = Duration.ofHours(24);

    /** JWT algorithm: HS256 or RS256. */
    private String algorithm = "HS256";

    /** RSA public key (PEM format, for RS256). */
    private String publicKey;

    /** RSA private key (PEM format, for RS256). */
    private String privateKey;

    /** Default HTTP header name for token extraction. */
    private String headerName = "Authorization";

    /** Default token prefix before the actual token string. */
    private String tokenPrefix = "Bearer ";

    // ── Access token overrides ──

    /**
     * Secret for access tokens.
     * Falls back to {@link #secret} when not set.
     */
    private String accessSecret;

    /**
     * Access token TTL.
     * Falls back to {@link #expiration} when not set.
     */
    private Duration accessExpiration;

    /**
     * Header name for the access token.
     * Falls back to {@link #headerName} when not set.
     */
    private String accessHeaderName;

    /**
     * Token prefix for the access token.
     * Falls back to {@link #tokenPrefix} when not set.
     */
    private String accessTokenPrefix;

    // ── Refresh token overrides (only meaningful in DUAL mode) ──

    /**
     * Secret for refresh tokens.
     * In DUAL mode, falls back to {@link #secret} when not set.
     */
    private String refreshSecret;

    /**
     * Refresh token TTL.
     * In DUAL mode, falls back to {@link #expiration} when not set.
     */
    private Duration refreshExpiration;

    /**
     * Header name for the refresh token (only used in DUAL mode).
     * Falls back to {@code X-Refresh-Token} when not set.
     */
    private String refreshHeaderName;

    /**
     * Token prefix for the refresh token.
     * Falls back to {@link #tokenPrefix} when not set.
     */
    private String refreshTokenPrefix;

    // ── Filter / interceptor settings ──

    /** Whether to auto-register the JwtAuthenticationFilter. */
    private boolean autoFilter = true;

    /** Ant-style path patterns to exclude from JWT validation. */
    private List<String> excludePaths = new ArrayList<>();

    /**
     * If true, return 401 when token is missing or invalid.
     * If false, delegates the decision to the application.
     */
    private boolean failOnInvalid = false;

    // ── Resolution methods — per-token values with fallback chain ──

    /** Resolve the effective access token secret. */
    public String resolveAccessSecret() {
        return isSet(accessSecret) ? accessSecret : secret;
    }

    /** Resolve the effective access token expiration. */
    public Duration resolveAccessExpiration() {
        return accessExpiration != null ? accessExpiration : expiration;
    }

    /** Resolve the effective access token header name. */
    public String resolveAccessHeaderName() {
        return isSet(accessHeaderName) ? accessHeaderName : headerName;
    }

    /** Resolve the effective access token prefix. */
    public String resolveAccessTokenPrefix() {
        return isSet(accessTokenPrefix) ? accessTokenPrefix : tokenPrefix;
    }

    /** Resolve the effective refresh token secret. */
    public String resolveRefreshSecret() {
        if (mode == TokenMode.SINGLE) {
            return resolveAccessSecret();
        }
        return isSet(refreshSecret) ? refreshSecret : secret;
    }

    /** Resolve the effective refresh token expiration. */
    public Duration resolveRefreshExpiration() {
        if (mode == TokenMode.SINGLE) {
            return resolveAccessExpiration();
        }
        return refreshExpiration != null ? refreshExpiration : expiration;
    }

    /** Resolve the effective refresh token header name. */
    public String resolveRefreshHeaderName() {
        return isSet(refreshHeaderName) ? refreshHeaderName : "X-Refresh-Token";
    }

    /** Resolve the effective refresh token prefix. */
    public String resolveRefreshTokenPrefix() {
        return isSet(refreshTokenPrefix) ? refreshTokenPrefix : tokenPrefix;
    }

    private static boolean isSet(String value) {
        return value != null && !value.isBlank();
    }

    // ── Enums ──

    public enum TokenMode {
        SINGLE,
        DUAL
    }
}