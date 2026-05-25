package com.ikeu.components.autoconfigure.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.ikeu.components.core.utils.JsonUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Auto-configuration for global Jackson {@link ObjectMapper} customization.
 * <p>
 * Replaces the common boilerplate of overriding {@code extendMessageConverters}
 * in a project's {@code WebMvcConfigurer}.
 *
 * <h3>What it provides</h3>
 * <ul>
 *   <li>Date format: configurable via {@code ikeu.jackson.date-pattern}</li>
 *   <li>Long as String: JSON-safe for JavaScript &gt; 2^53 values</li>
 *   <li>Null exclusion: absent fields from serialized output</li>
 *   <li>Unknown property tolerance: no exceptions on extra JSON fields</li>
 *   <li>Java 8 time: LocalDateTime, LocalDate, LocalTime serialization</li>
 *   <li>Message converter integration: replaces ObjectMapper in all
 *       {@code MappingJackson2HttpMessageConverter} instances</li>
 * </ul>
 *
 * <h3>Config in application.yml</h3>
 * <pre>{@code
 * ikeu:
 *   jackson:
 *     date-pattern: "yyyy-MM-dd HH:mm:ss"
 *     long-as-string: true
 *     serialization-inclusion: non_null
 *     time-zone: Asia/Shanghai
 * }</pre>
 *
 * <h3>Compatibility</h3>
 * <ul>
 *   <li>User defines {@link ObjectMapper @Bean} → Spring Boot backs off (own
 *       {@code @ConditionalOnMissingBean}), customizer has no effect target</li>
 *   <li>User defines {@link Jackson2ObjectMapperBuilderCustomizer} → additive,
 *       both apply</li>
 *   <li>After all customizers run, {@link JsonUtils#setObjectMapper} is called
 *       via {@code @EventListener(ApplicationReadyEvent)} to keep the static
 *       utility in sync</li>
 * </ul>
 *
 * @see JacksonProperties
 * @see JsonUtils
 * @author ikeu
 * @since 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(JacksonProperties.class)
@ConditionalOnClass({ObjectMapper.class, Jackson2ObjectMapperBuilder.class})
public class JacksonCustomAutoConfiguration {

    // ──────────────────────────────────────────────
    // ObjectMapper customizer — hooks into Spring
    // Boot's Jackson2ObjectMapperBuilder pipeline
    // ──────────────────────────────────────────────

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer ikeuJacksonCustomizer(JacksonProperties props) {
        return builder -> {
            builder.simpleDateFormat(props.getDatePattern());
            builder.featuresToDisable(
                    SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

            // Serialization inclusion
            builder.serializationInclusion(parseInclusion(props.getSerializationInclusion()));

            // Timezone
            if (props.getTimeZone() != null && !props.getTimeZone().isBlank()) {
                builder.timeZone(props.getTimeZone());
            }

            // Java 8 time
            JavaTimeModule javaTimeModule = buildJavaTimeModule(props.getDatePattern());
            builder.modules(javaTimeModule);

            // Long → String
            if (props.isLongAsString()) {
                builder.serializerByType(Long.class, ToStringSerializer.instance);
                builder.serializerByType(long.class, ToStringSerializer.instance);
            }
        };
    }

    // ──────────────────────────────────────────────
    // ObjectMapper bridge — sync Spring's
    // auto-configured ObjectMapper back to JsonUtils
    // after all customizers have been applied
    // ──────────────────────────────────────────────

    /**
     * After Spring Boot has fully built the ObjectMapper (all customizers
     * applied), sync it to {@link JsonUtils} so non-Spring static helpers
     * use the same configured mapper.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void syncJsonUtils(ObjectMapper objectMapper) {
        JsonUtils.setObjectMapper(objectMapper);
    }

    // ──────────────────────────────────────────────
    // Message converter — explicit WebMvcConfigurer
    // ──────────────────────────────────────────────

    @Bean
    @ConditionalOnMissingBean(name = "ikeuJacksonWebMvcConfigurer")
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(name = "org.springframework.web.servlet.config.annotation.WebMvcConfigurer")
    public WebMvcConfigurer ikeuJacksonWebMvcConfigurer(ObjectMapper objectMapper) {
        return new WebMvcConfigurer() {
            @Override
            public void extendMessageConverters(
                    java.util.List<org.springframework.http.converter.HttpMessageConverter<?>> converters) {
                for (org.springframework.http.converter.HttpMessageConverter<?> converter : converters) {
                    if (converter instanceof org.springframework.http.converter.json.MappingJackson2HttpMessageConverter jsonConverter) {
                        jsonConverter.setObjectMapper(objectMapper);
                    }
                }
            }
        };
    }

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────

    private static JavaTimeModule buildJavaTimeModule(String datePattern) {
        JavaTimeModule module = new JavaTimeModule();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(datePattern);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm:ss");
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dtf));
        module.addSerializer(LocalDate.class, new LocalDateSerializer(df));
        module.addSerializer(LocalTime.class, new LocalTimeSerializer(tf));
        return module;
    }

    static JsonInclude.Include parseInclusion(String value) {
        if (value == null) {
            return JsonInclude.Include.NON_NULL;
        }
        return switch (value.toLowerCase()) {
            case "always" -> JsonInclude.Include.ALWAYS;
            case "non_default" -> JsonInclude.Include.NON_DEFAULT;
            case "non_absent" -> JsonInclude.Include.NON_ABSENT;
            case "non_empty" -> JsonInclude.Include.NON_EMPTY;
            default -> JsonInclude.Include.NON_NULL;
        };
    }
}