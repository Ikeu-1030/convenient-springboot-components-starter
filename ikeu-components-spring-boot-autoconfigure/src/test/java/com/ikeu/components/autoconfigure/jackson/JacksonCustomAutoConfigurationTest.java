package com.ikeu.components.autoconfigure.jackson;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.*;

class JacksonCustomAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    JacksonAutoConfiguration.class,
                    JacksonCustomAutoConfiguration.class));

    @Test
    void createsCustomizerBean() {
        runner.run(ctx -> {
            assertTrue(ctx.containsBean("ikeuJacksonCustomizer"));
        });
    }

    @Test
    void objectMapperHasLongAsStringSerialization() {
        runner.run(ctx -> {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    ctx.getBean(com.fasterxml.jackson.databind.ObjectMapper.class);
            String json = mapper.writeValueAsString(new LongHolder(9999999999999999L));
            assertTrue(json.contains("\"9999999999999999\""),
                    "Long should serialize as String, got: " + json);
        });
    }

    @Test
    void objectMapperExcludesNullFields() {
        runner.run(ctx -> {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    ctx.getBean(com.fasterxml.jackson.databind.ObjectMapper.class);
            String json = mapper.writeValueAsString(new NullName());
            assertFalse(json.contains("\"name\""),
                    "Null fields should be omitted (NON_NULL), got: " + json);
        });
    }

    @Test
    void customDatePattern_isBound() {
        runner.withPropertyValues("ikeu.jackson.date-pattern=yyyy/MM/dd").run(ctx -> {
            JacksonProperties props = ctx.getBean(JacksonProperties.class);
            assertEquals("yyyy/MM/dd", props.getDatePattern());
        });
    }

    @Test
    void customSerializationInclusion_isBound() {
        runner.withPropertyValues("ikeu.jackson.serialization-inclusion=always").run(ctx -> {
            JacksonProperties props = ctx.getBean(JacksonProperties.class);
            assertEquals("always", props.getSerializationInclusion());
        });
    }

    // ── Test POJOs ──

    public static class LongHolder {
        private Long value;
        public LongHolder() {}
        public LongHolder(Long value) { this.value = value; }
        public Long getValue() { return value; }
        public void setValue(Long value) { this.value = value; }
    }

    public static class NullName {
        private String name;
        public NullName() {}
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}