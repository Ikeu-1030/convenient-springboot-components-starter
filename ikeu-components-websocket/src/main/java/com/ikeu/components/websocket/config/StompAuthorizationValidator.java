package com.ikeu.components.websocket.config;

/**
 * Custom authorization strategy for STOMP channel subscriptions.
 * <p>
 * Business code implements this interface and registers it as a Spring bean;
 * the {@link JwtStompChannelInterceptor} will delegate subscription
 * authorization checks to it.
 */
@FunctionalInterface
public interface StompAuthorizationValidator {

    /**
     * Check whether a user is allowed to subscribe to the given destination.
     *
     * @param userId      the authenticated user ID extracted from the JWT
     * @param destination the STOMP destination (e.g. {@code /user/123/messages})
     * @return true if the subscription is allowed
     */
    boolean canSubscribe(String userId, String destination);
}
