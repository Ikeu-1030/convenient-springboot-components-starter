package com.ikeu.components.autoconfigure.redis;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ikeu.components.redis.lock.RedisDistributedLock;
import com.ikeu.components.redis.lock.RedisLockHelper;
import com.ikeu.components.redis.util.RedisUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Auto-configuration for Redis customization.
 * <p>
 * Provides:
 * <ul>
 *   <li>{@code RedisTemplate<String, Object>} with JSON serialization
 *       (replacing the default JDK serialization)</li>
 *   <li>{@link RedisCacheManager} for Spring Cache abstraction
 *       ({@code @Cacheable}, {@code @CacheEvict}, {@code @CachePut})</li>
 *   <li>{@link RedisDistributedLock} for manual lock/unlock</li>
 *   <li>{@link RedisUtils} for convenient get/set/list/map/hash/atomic operations</li>
 *   <li>{@link RedisLockHelper} for functional auto-lock, cache-penetration
 *       and cache-breakdown protection</li>
 * </ul>
 * <p>
 * Disabled by default. Enable with {@code ikeu.redis.enabled=true} and
 * add {@code spring-boot-starter-data-redis} to your project.
 * <p>
 * To use the Spring Cache abstraction, add {@code @EnableCaching} to
 * your application configuration class.
 *
 * <h3>Configuration</h3>
 * <pre>{@code
 * ikeu:
 *   redis:
 *     enabled: true
 *     lock-prefix: "ikeu:lock:"
 *     use-json-serialization: true
 *     cache-default-ttl: 30m
 *     cache-null-ttl: 5m
 *     cache-key-prefix: "ikeu:cache:"
 * }</pre>
 */
@AutoConfiguration
@EnableConfigurationProperties(RedisProperties.class)
@ConditionalOnClass({RedisTemplate.class, RedisConnectionFactory.class})
@ConditionalOnProperty(prefix = "ikeu.redis", name = "enabled", havingValue = "true")
public class RedisAutoConfiguration {

    /**
     * Custom {@link RedisTemplate} with JSON value serialization instead of JDK.
     */
    @Bean
    @ConditionalOnMissingBean(name = "ikeuRedisTemplate")
    public RedisTemplate<String, Object> ikeuRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.registerModule(new JavaTimeModule());

        Jackson2JsonRedisSerializer<Object> jsonSerializer =
                new Jackson2JsonRedisSerializer<>(mapper, Object.class);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.setDefaultSerializer(jsonSerializer);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Distributed lock backed by {@link StringRedisTemplate}.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(StringRedisTemplate.class)
    public RedisDistributedLock redisDistributedLock(StringRedisTemplate stringRedisTemplate,
                                                      RedisProperties props) {
        return new RedisDistributedLock(stringRedisTemplate, props.getLockPrefix());
    }

    /**
     * Convenience wrapper around the JSON-serializing {@code ikeuRedisTemplate}.
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisUtils redisUtils(RedisTemplate<String, Object> ikeuRedisTemplate) {
        return new RedisUtils(ikeuRedisTemplate);
    }

    /**
     * High-level lock/cache helper combining {@link RedisDistributedLock}
     * and {@link RedisUtils}.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({RedisDistributedLock.class, RedisUtils.class})
    public RedisLockHelper redisLockHelper(RedisDistributedLock redisDistributedLock,
                                            RedisUtils redisUtils) {
        return new RedisLockHelper(redisDistributedLock, redisUtils);
    }

    /**
     * {@link RedisCacheManager} configured with JSON serialization and
     * configurable TTL for use with Spring Cache annotations
     * ({@code @Cacheable}, {@code @CacheEvict}, {@code @CachePut}).
     * <p>
     * Uses the same serializer as {@code ikeuRedisTemplate} so cached
     * values are stored as JSON. Null-value caching is enabled by default
     * to prevent cache penetration.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(name = "ikeuRedisTemplate")
    public RedisCacheManager redisCacheManager(RedisTemplate<String, Object> ikeuRedisTemplate,
                                               RedisProperties props) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(props.getCacheDefaultTtl())
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(ikeuRedisTemplate.getValueSerializer()));

        if (!props.isCacheCacheNullValues()) {
            config = config.disableCachingNullValues();
        }

        if (props.isCacheUseKeyPrefix()) {
            config = config.prefixCacheNameWith(props.getCacheKeyPrefix());
        }

        return RedisCacheManager.builder(ikeuRedisTemplate.getRequiredConnectionFactory())
                .cacheDefaults(config)
                .build();
    }
}
