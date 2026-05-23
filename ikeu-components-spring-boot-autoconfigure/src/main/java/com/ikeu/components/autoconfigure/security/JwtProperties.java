package com.ikeu.components.autoconfigure.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration properties for JWT, prefix "ikeu.jwt".
 */
@Data
@ConfigurationProperties(prefix = "ikeu.jwt")
public class JwtProperties {

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
}
