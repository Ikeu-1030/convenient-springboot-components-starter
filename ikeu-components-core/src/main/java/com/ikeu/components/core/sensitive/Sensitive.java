package com.ikeu.components.core.sensitive;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a String field for automatic data masking during JSON serialization.
 *
 * <p>The mask is applied by {@link SensitiveSerializer} which is registered globally
 * via the Jackson auto-configuration. Non-annotated String fields are unaffected.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {

    /** Masking strategy. Defaults to {@link SensitiveType#CUSTOM}. */
    SensitiveType value() default SensitiveType.CUSTOM;

    /** Number of leading characters kept unmasked (only for CUSTOM type). */
    int startInclude() default 0;

    /** Number of trailing characters kept unmasked (only for CUSTOM type). */
    int endInclude() default 0;

    /** Character used for masking. */
    char maskChar() default '*';
}
