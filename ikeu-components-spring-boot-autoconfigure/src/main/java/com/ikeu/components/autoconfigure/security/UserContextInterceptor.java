package com.ikeu.components.autoconfigure.security;

import com.ikeu.components.core.utils.JsonUtils;
import com.ikeu.components.security.annotation.AnonymousAccess;
import com.ikeu.components.security.context.UserContextHolder;
import com.ikeu.components.web.model.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Spring MVC interceptor that verifies authentication in {@code preHandle}
 * and clears {@link UserContextHolder} in {@code afterCompletion}.
 * <p>
 * Skips authentication when the handler is annotated with {@link AnonymousAccess}
 * or the request path matches a configured exclude pattern.
 * @author ikeu
 * @since 1.0.0
 */
@Slf4j
public class UserContextInterceptor implements HandlerInterceptor {

    private final List<String> excludePaths;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public UserContextInterceptor(List<String> excludePaths) {
        this.excludePaths = (excludePaths != null) ? excludePaths : Collections.emptyList();
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        if (isAnonymousAccess(handler) || isExcluded(request.getRequestURI())) {
            return true;
        }

        if (UserContextHolder.getUserId() == null) {
            writeUnauthorized(response);
            return false;
        }
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        UserContextHolder.clear();
    }

    private boolean isAnonymousAccess(Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return false;
        }
        if (handlerMethod.getMethodAnnotation(AnonymousAccess.class) != null) {
            return true;
        }
        return handlerMethod.getBeanType().isAnnotationPresent(AnonymousAccess.class);
    }

    private boolean isExcluded(String requestPath) {
        for (String pattern : excludePaths) {
            if (pathMatcher.match(pattern, requestPath)) {
                return true;
            }
        }
        return false;
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtils.toJson(
                Result.error(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required")));
    }
}