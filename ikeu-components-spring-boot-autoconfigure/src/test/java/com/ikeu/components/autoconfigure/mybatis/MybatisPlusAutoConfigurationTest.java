package com.ikeu.components.autoconfigure.mybatis;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.*;

class MybatisPlusAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MybatisPlusAutoConfiguration.class));

    @Test
    void shouldNotRegisterWhenDisabled() {
        runner.run(ctx -> {
            assertFalse(ctx.containsBean("mybatisPlusInterceptor"));
            assertFalse(ctx.containsBean("autoFillMetaObjectHandler"));
        });
    }

    @Test
    void shouldRegisterInterceptorWhenEnabled() {
        runner.withPropertyValues("ikeu.mybatis-plus.enabled=true").run(ctx -> {
            assertTrue(ctx.containsBean("mybatisPlusInterceptor"));
            MybatisPlusInterceptor interceptor = ctx.getBean(MybatisPlusInterceptor.class);
            assertNotNull(interceptor);
        });
    }

    @Test
    void shouldRegisterMetaObjectHandlerWhenEnabled() {
        runner.withPropertyValues("ikeu.mybatis-plus.enabled=true").run(ctx -> {
            assertTrue(ctx.containsBean("autoFillMetaObjectHandler"));
        });
    }

    @Test
    void shouldBindCustomProperties() {
        runner.withPropertyValues(
                "ikeu.mybatis-plus.enabled=true",
                "ikeu.mybatis-plus.db-type=postgresql",
                "ikeu.mybatis-plus.auto-fill-create-fields=created,ctime",
                "ikeu.mybatis-plus.auto-fill-update-fields=updated,utime"
        ).run(ctx -> {
            MybatisPlusProperties props = ctx.getBean(MybatisPlusProperties.class);
            assertEquals("postgresql", props.getDbType());
            assertEquals(2, props.getAutoFillCreateFields().size());
            assertTrue(props.getAutoFillCreateFields().contains("created"));
            assertEquals(2, props.getAutoFillUpdateFields().size());
        });
    }
}
