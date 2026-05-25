package com.ikeu.components.web.context;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SpringContextHolderTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        SpringContextHolder springContextHolder() {
            return new SpringContextHolder();
        }
    }

    @Test
    void getApplicationContext_notNull() {
        assertNotNull(SpringContextHolder.getApplicationContext());
    }

    @Test
    void getProperty_returnsSystemProperty() {
        String value = SpringContextHolder.getProperty("java.version");
        assertNotNull(value);
    }

    @Test
    void getProperty_defaultValue() {
        String value = SpringContextHolder.getProperty("nonexistent.key", "default");
        assertEquals("default", value);
    }

    @Test
    void getProperty_nullWhenKeyMissing() {
        assertNull(SpringContextHolder.getProperty("some.nonexistent.prop"));
    }

    @Test
    void getBean_returnsSelf() {
        // SpringContextHolder is itself a bean in this test context
        SpringContextHolder self = SpringContextHolder.getBean(SpringContextHolder.class);
        assertNotNull(self);
    }
}