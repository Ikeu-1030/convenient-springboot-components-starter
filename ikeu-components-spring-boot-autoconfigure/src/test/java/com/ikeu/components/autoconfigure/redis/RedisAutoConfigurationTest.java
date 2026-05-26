package com.ikeu.components.autoconfigure.redis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.*;

class RedisAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RedisAutoConfiguration.class));

    @Test
    void shouldNotRegisterWhenDisabled() {
        runner.run(ctx -> {
            assertFalse(ctx.containsBean("ikeuRedisTemplate"));
            assertFalse(ctx.containsBean("redisDistributedLock"));
            assertFalse(ctx.containsBean("redisLockHelper"));
            assertFalse(ctx.containsBean("redisUtils"));
        });
    }

    @Test
    void shouldBindProperties() {
        RedisProperties props = new RedisProperties();
        props.setLockPrefix("custom:lock:");
        props.setUseJsonSerialization(false);

        assertEquals("custom:lock:", props.getLockPrefix());
        assertFalse(props.isUseJsonSerialization());
    }
}
