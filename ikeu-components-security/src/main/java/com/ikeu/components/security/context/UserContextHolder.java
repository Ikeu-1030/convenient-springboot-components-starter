package com.ikeu.components.security.context;

/**
 * ThreadLocal-based holder for the current login user. Must be cleared after each request
 * to prevent memory leaks (handled by UserContextClearFilter in autoconfigure).
 */
public final class UserContextHolder {

    private static final ThreadLocal<Object> USER_HOLDER = new ThreadLocal<>();

    private UserContextHolder() {
    }

    /**
     * Set the current user in thread-local context.
     */
    public static void setUser(Object user) {
        USER_HOLDER.set(user);
    }

    /**
     * Get the current user from thread-local context.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getUser() {
        return (T) USER_HOLDER.get();
    }

    /**
     * Get the current user's ID. Returns user.toString() if available, otherwise null.
     */
    public static String getUserId() {
        Object user = USER_HOLDER.get();
        return user != null ? user.toString() : null;
    }

    /**
     * Clear the thread-local context. Must be called after each request.
     */
    public static void clear() {
        USER_HOLDER.remove();
    }
}
