package com.ikeu.components.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a controller method or class as publicly accessible (skip JWT authentication).
 * Checked by {@code UserContextInterceptor#preHandle}.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AnonymousAccess {
}