package com.ikeu.components.security.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * ThreadLocal-based holder for the current user and associated JWT claims.
 * Must be cleared after each request via {@link #clear()} to prevent memory leaks.
 */
public final class UserContextHolder {

    private static final ThreadLocal<Map<String, Object>> CLAIMS_HOLDER = ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<Object> USER_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ID_HOLDER = new ThreadLocal<>();

    private UserContextHolder() {
    }

    /** Set the current user object in thread-local context. */
    public static void setUser(Object user) {
        USER_HOLDER.set(user);
    }

    /** Get the current user object. */
    @SuppressWarnings("unchecked")
    public static <T> T getUser() {
        return (T) USER_HOLDER.get();
    }

    /** Set the current user ID. */
    public static void setUserId(String userId) {
        USER_ID_HOLDER.set(userId);
    }

    /** Get the current user ID. */
    public static String getUserId() {
        String userId = USER_ID_HOLDER.get();
        if (userId != null) {
            return userId;
        }
        Object user = USER_HOLDER.get();
        return user != null ? user.toString() : null;
    }

    /** Store all JWT claims for the current request. */
    public static void setClaims(Map<String, Object> claims) {
        CLAIMS_HOLDER.set(new HashMap<>(claims));
    }

    /** Get a specific claim value. */
    @SuppressWarnings("unchecked")
    public static <T> T getClaim(String key) {
        return (T) CLAIMS_HOLDER.get().get(key);
    }

    /** Get all claims (read-only). */
    public static Map<String, Object> getClaims() {
        return Collections.unmodifiableMap(CLAIMS_HOLDER.get());
    }

    /** Get the subject (userId) from claims, fallback to getUserId(). */
    public static String getSubject() {
        Object sub = CLAIMS_HOLDER.get().get("sub");
        if (sub != null) {
            return sub.toString();
        }
        return getUserId();
    }

    /** Clear all thread-local data. Call after each request. */
    public static void clear() {
        CLAIMS_HOLDER.remove();
        USER_HOLDER.remove();
        USER_ID_HOLDER.remove();
    }
}