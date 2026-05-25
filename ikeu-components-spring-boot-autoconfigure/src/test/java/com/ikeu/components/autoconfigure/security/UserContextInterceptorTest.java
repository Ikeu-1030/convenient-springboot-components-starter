package com.ikeu.components.autoconfigure.security;

import com.ikeu.components.security.annotation.AnonymousAccess;
import com.ikeu.components.security.context.UserContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class UserContextInterceptorTest {

    private UserContextInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new UserContextInterceptor(Collections.emptyList());
        UserContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void allowsWhenAuthenticated() throws Exception {
        UserContextHolder.setUserId("user123");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void blocksWhenNotAuthenticated() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());
        assertFalse(result);
        assertEquals(401, response.getStatus());
    }

    @Test
    void allowsAnonymousAccessMethod() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handler = new HandlerMethod(new AnnotatedController(),
                AnnotatedController.class.getMethod("anonymousMethod"));

        assertTrue(interceptor.preHandle(request, response, handler));
    }

    @Test
    void allowsAnonymousAccessClass() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handler = new HandlerMethod(new AnnotatedClassController(),
                AnnotatedClassController.class.getMethod("someMethod"));

        assertTrue(interceptor.preHandle(request, response, handler));
    }

    @Test
    void allowsExcludedPath() throws Exception {
        UserContextInterceptor excludeInterceptor =
                new UserContextInterceptor(Collections.singletonList("/public/**"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/public/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(excludeInterceptor.preHandle(request, response, new Object()));
    }

    @Test
    void clearsContextAfterCompletion() throws Exception {
        UserContextHolder.setUserId("user");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.afterCompletion(request, response, new Object(), null);

        assertNull(UserContextHolder.getUserId());
    }

    // ── Test controllers ──

    static class AnnotatedController {
        @AnonymousAccess
        public void anonymousMethod() {}
    }

    @AnonymousAccess
    static class AnnotatedClassController {
        public void someMethod() {}
    }
}