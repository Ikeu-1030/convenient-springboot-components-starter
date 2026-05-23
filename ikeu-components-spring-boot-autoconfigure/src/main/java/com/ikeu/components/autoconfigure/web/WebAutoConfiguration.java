package com.ikeu.components.autoconfigure.web;

import com.ikeu.components.web.context.SpringContextHolder;
import com.ikeu.components.web.handler.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for web utilities: SpringContextHolder and GlobalExceptionHandler.
 */
@AutoConfiguration
@Import({SpringContextHolder.class, GlobalExceptionHandler.class})
public class WebAutoConfiguration {
}
