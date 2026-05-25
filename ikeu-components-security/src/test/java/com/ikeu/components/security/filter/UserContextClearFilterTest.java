package com.ikeu.components.security.filter;

import com.ikeu.components.security.context.UserContextHolder;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class UserContextClearFilterTest {

    private final UserContextClearFilter filter = new UserContextClearFilter();

    @Test
    void clearsContextAfterRequest() throws Exception {
        UserContextHolder.setUserId("user123");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            // context should still be set during chain execution
            assertEquals("user123", UserContextHolder.getUserId());
        };

        filter.doFilter(request, response, chain);

        assertNull(UserContextHolder.getUserId(), "Context must be cleared after filter");
    }

    @Test
    void clearsContextEvenOnException() throws Exception {
        UserContextHolder.setUserId("user");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            throw new RuntimeException("test exception");
        };

        try {
            filter.doFilter(request, response, chain);
        } catch (RuntimeException ignored) {
        }

        assertNull(UserContextHolder.getUserId(), "Context must be cleared even on exception");
    }

    @Test
    void worksWhenContextAlreadyEmpty() throws Exception {
        UserContextHolder.clear();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {};

        filter.doFilter(request, response, chain);

        assertNull(UserContextHolder.getUserId());
    }
}