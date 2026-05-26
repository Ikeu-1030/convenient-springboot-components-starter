package com.ikeu.components.autoconfigure.trace;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

import jakarta.servlet.ServletException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TraceIdFilterTest {

    @Test
    void shouldGenerateTraceIdWhenHeaderMissing() throws Exception {
        TraceProperties props = new TraceProperties();
        props.setEnabled(true);
        props.setHeaderName("X-Trace-Id");
        props.setMdcKey("traceId");

        TraceIdFilter filter = new TraceIdFilter(props);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        String traceId = response.getHeader("X-Trace-Id");
        assertNotNull(traceId, "Should generate and set response header");
        assertEquals(32, traceId.length(), "UUID without dashes should be 32 chars");
        assertNull(MDC.get("traceId"), "MDC should be cleared after request");
    }

    @Test
    void shouldReuseExistingTraceId() throws Exception {
        TraceProperties props = new TraceProperties();
        props.setEnabled(true);
        props.setHeaderName("X-Trace-Id");
        props.setMdcKey("traceId");

        TraceIdFilter filter = new TraceIdFilter(props);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Trace-Id", "existing-trace-id");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertEquals("existing-trace-id", response.getHeader("X-Trace-Id"));
        assertNull(MDC.get("traceId"));
    }

    @Test
    void shouldNotSetResponseHeaderWhenDisabled() throws Exception {
        TraceProperties props = new TraceProperties();
        props.setEnabled(true);
        props.setHeaderName("X-Trace-Id");
        props.setResponseHeader(false);
        props.setMdcKey("traceId");

        TraceIdFilter filter = new TraceIdFilter(props);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertNull(response.getHeader("X-Trace-Id"));
        assertNull(MDC.get("traceId"));
    }

    @Test
    void shouldUseCustomMdcKey() throws Exception {
        TraceProperties props = new TraceProperties();
        props.setEnabled(true);
        props.setMdcKey("customTraceId");
        props.setHeaderName("X-Trace-Id");

        TraceIdFilter filter = new TraceIdFilter(props);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res)
                    throws IOException, ServletException {
                assertNotNull(MDC.get("customTraceId"), "MDC should be set during filter chain");
            }
        };

        filter.doFilterInternal(request, response, chain);

        assertNull(MDC.get("customTraceId"), "MDC should be cleared after request");
    }
}
