package com.ikeu.components.autoconfigure.xss;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Servlet filter that wraps incoming requests with {@link XssRequestWrapper}
 * to sanitize parameters and headers.
 */
public class XssFilter extends OncePerRequestFilter {

    private final XssProperties props;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public XssFilter(XssProperties props) {
        this.props = props;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (isExcluded(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }
        filterChain.doFilter(new XssRequestWrapper(request, props.getMode()), response);
    }

    private boolean isExcluded(String requestUri) {
        List<String> excludePaths = props.getExcludePaths();
        if (excludePaths == null || excludePaths.isEmpty()) return false;
        for (String pattern : excludePaths) {
            if (pathMatcher.match(pattern, requestUri)) return true;
        }
        return false;
    }
}
