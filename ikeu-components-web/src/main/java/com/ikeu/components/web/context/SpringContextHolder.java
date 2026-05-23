package com.ikeu.components.web.context;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

/**
 * Static holder for Spring ApplicationContext. Provides convenient static methods to
 * retrieve beans and environment properties from non-Spring-managed classes.
 */
@Slf4j
public class SpringContextHolder implements ApplicationContextAware {

    private static volatile ApplicationContext context;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        SpringContextHolder.context = applicationContext;
        log.info("SpringContextHolder initialized");
    }

    public static <T> T getBean(Class<T> clazz) {
        if (context == null) {
            throw new IllegalStateException("ApplicationContext not initialized");
        }
        return context.getBean(clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        if (context == null) {
            throw new IllegalStateException("ApplicationContext not initialized");
        }
        return (T) context.getBean(name);
    }

    public static String getProperty(String key) {
        if (context == null) {
            return null;
        }
        return context.getEnvironment().getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        if (context == null) {
            return defaultValue;
        }
        return context.getEnvironment().getProperty(key, defaultValue);
    }

    public static ApplicationContext getApplicationContext() {
        return context;
    }
}
