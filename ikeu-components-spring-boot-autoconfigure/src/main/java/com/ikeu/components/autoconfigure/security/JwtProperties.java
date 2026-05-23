package com.ikeu.components.autoconfigure.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for JWT, prefix "ikeu.jwt".
 */
@Data
@ConfigurationProperties(prefix = "ikeu.jwt")
public class JwtProperties {

    /** Enable JWT auto-configuration. Default: false. */
    private boolean enabled = false;

    /** HMAC-SHA256 secret key. */
    private String secret = "ikeu-components-default-secret-key-change-me";

    /** Token expiration duration. */
    private Duration expiration = Duration.ofHours(24);

    /** JWT algorithm: HS256 or RS256. */
    private String algorithm = "HS256";

    /** RSA public key (PEM format, for RS256). */
    private String publicKey;

    /** RSA private key (PEM format, for RS256). */
    private String privateKey;

    /** Whether to auto-register the JwtAuthenticationFilter. Default: true. */
    private boolean autoFilter = true;

    /** HTTP header name to extract token from. Default: Authorization. */
    private String headerName = "Authorization";

    /** Token prefix before the actual token string. Default: "Bearer ". */
    private String tokenPrefix = "Bearer ";

    /** Ant-style path patterns to exclude from JWT validation. */
    private List<String> excludePaths = new ArrayList<>();

    /** If true, return 401 when token is missing or invalid. Default: false (delegate to application). */
    private boolean failOnInvalid = false;
}