package com.ikeu.components.security.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserContextHolderTest {

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void setUserId_getUserId() {
        UserContextHolder.setUserId("user123");
        assertEquals("user123", UserContextHolder.getUserId());
    }

    @Test
    void setUser_getUser() {
        Object user = new Object();
        UserContextHolder.setUser(user);
        assertSame(user, UserContextHolder.getUser());
    }

    @Test
    void getUserId_fallsBackToUserToString() {
        UserContextHolder.setUser("plainUser");
        assertEquals("plainUser", UserContextHolder.getUserId());
    }

    @Test
    void setClaims_getClaims() {
        Map<String, Object> claims = Map.of("sub", "user1", "role", "admin");
        UserContextHolder.setClaims(claims);
        assertEquals("admin", UserContextHolder.getClaim("role"));
        assertEquals(2, UserContextHolder.getClaims().size());
    }

    @Test
    void getClaims_returnsUnmodifiable() {
        UserContextHolder.setClaims(Map.of("key", "val"));
        Map<String, Object> claims = UserContextHolder.getClaims();
        assertThrows(UnsupportedOperationException.class, () -> claims.put("new", "val"));
    }

    @Test
    void getSubject_fromClaims() {
        UserContextHolder.setClaims(Map.of("sub", "userId123"));
        assertEquals("userId123", UserContextHolder.getSubject());
    }

    @Test
    void getSubject_fallsBackToUserId() {
        UserContextHolder.setUserId("fallbackUser");
        assertEquals("fallbackUser", UserContextHolder.getSubject());
    }

    @Test
    void clear_removesAll() {
        UserContextHolder.setUserId("user");
        UserContextHolder.setClaims(Map.of("key", "val"));
        UserContextHolder.setUser(new Object());
        UserContextHolder.clear();
        assertNull(UserContextHolder.getUserId());
        assertNull(UserContextHolder.getUser());
        assertTrue(UserContextHolder.getClaims().isEmpty());
    }

    @Test
    void threadIsolation() throws InterruptedException {
        UserContextHolder.setUserId("main-thread-user");
        final String[] otherThreadUserId = {null};
        Thread t = new Thread(() -> {
            UserContextHolder.setUserId("other-thread-user");
            otherThreadUserId[0] = UserContextHolder.getUserId();
        });
        t.start();
        t.join();
        assertEquals("other-thread-user", otherThreadUserId[0]);
        assertEquals("main-thread-user", UserContextHolder.getUserId());
    }
}