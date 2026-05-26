package com.ikeu.components.autoconfigure.xss;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.web.util.HtmlUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * {@link HttpServletRequestWrapper} that sanitizes request parameters and headers
 * against XSS attacks.
 *
 * <p>Body content ({@code getInputStream}/{@code getReader}) is NOT sanitized
 * — rich-text editors and file uploads depend on the raw body. Sanitize at
 * the application layer if needed.
 */
public class XssRequestWrapper extends HttpServletRequestWrapper {

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");

    private final XssProperties.XssMode mode;

    public XssRequestWrapper(HttpServletRequest request, XssProperties.XssMode mode) {
        super(request);
        this.mode = mode;
    }

    @Override
    public String getParameter(String name) {
        return sanitize(super.getParameter(name));
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) return null;
        String[] cleaned = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            cleaned[i] = sanitize(values[i]);
        }
        return cleaned;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> original = super.getParameterMap();
        Map<String, String[]> cleaned = new HashMap<>(original.size());
        for (Map.Entry<String, String[]> entry : original.entrySet()) {
            String[] values = entry.getValue();
            String[] sanitized = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                sanitized[i] = sanitize(values[i]);
            }
            cleaned.put(entry.getKey(), sanitized);
        }
        return cleaned;
    }

    @Override
    public String getHeader(String name) {
        return sanitize(super.getHeader(name));
    }

    @Override
    public String getQueryString() {
        return sanitize(super.getQueryString());
    }

    private String sanitize(String value) {
        if (value == null) return null;
        return switch (mode) {
            case ESCAPE -> HtmlUtils.htmlEscape(value);
            case STRIP -> HTML_TAG_PATTERN.matcher(value).replaceAll("");
        };
    }
}
