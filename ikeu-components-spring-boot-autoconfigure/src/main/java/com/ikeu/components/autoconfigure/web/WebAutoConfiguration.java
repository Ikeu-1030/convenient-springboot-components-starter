package com.ikeu.components.autoconfigure.web;

import com.ikeu.components.web.context.SpringContextHolder;
import com.ikeu.components.web.handler.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for web utilities. Imports {@link SpringContextHolder}
 * (which implements {@code ApplicationContextAware}) and
 * {@link GlobalExceptionHandler} ({@code @RestControllerAdvice}) into the
 * Spring context, making them available without component scanning.
 * <p>
 * Always active — no property toggle. Users can exclude it by removing
 * this entry from {@code AutoConfiguration.imports} or using
 * {@code @SpringBootApplication(exclude = WebAutoConfiguration.class)}.
 *
 * @author ikeu
 * @since 1.0.0
 */
@AutoConfiguration
@Import({SpringContextHolder.class, GlobalExceptionHandler.class})
public class WebAutoConfiguration {
}
