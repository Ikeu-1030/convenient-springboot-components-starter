package com.ikeu.components.autoconfigure.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that injects a trace ID into {@link MDC} for request-level log correlation.
 * <p>
 * The trace ID is extracted from the configured request header (default: {@code X-Trace-Id}).
 * If absent, a new UUID (without dashes) is generated. The ID is echoed back in the
 * response header and cleared from MDC after the request completes.
 */
public class TraceIdFilter extends OncePerRequestFilter {

    private final TraceProperties props;

    public TraceIdFilter(TraceProperties props) {
        this.props = props;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader(props.getHeaderName());
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        try {
            MDC.put(props.getMdcKey(), traceId);
            if (props.isResponseHeader()) {
                response.setHeader(props.getHeaderName(), traceId);
            }
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(props.getMdcKey());
        }
    }
}
