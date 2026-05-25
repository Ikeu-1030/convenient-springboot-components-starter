package com.ikeu.components.security.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Access + refresh token pair returned by {@code JwtUtils.generateTokenPair()}.
 * <p>
 * In {@code SINGLE} mode: {@code refreshToken} is {@code null} and
 * {@code refreshExpiresIn} is 0. Only the access token is populated.
 *
 * <table>
 *   <tr><td>{@code accessToken}</td><td>Short-lived (e.g. 2h) access credential</td></tr>
 *   <tr><td>{@code refreshToken}</td><td>Long-lived (e.g. 7d) refresh credential; null in SINGLE mode</td></tr>
 *   <tr><td>{@code expiresIn}</td><td>Seconds until access token expires</td></tr>
 *   <tr><td>{@code refreshExpiresIn}</td><td>Seconds until refresh token expires; 0 in SINGLE mode</td></tr>
 * </table>
 *
 * @author ikeu
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenPair {

    /** Short-lived access token. */
    private String accessToken;

    /** Long-lived refresh token. */
    private String refreshToken;

    /** Seconds until the access token expires. */
    private long expiresIn;

    /** Seconds until the refresh token expires. */
    private long refreshExpiresIn;
}