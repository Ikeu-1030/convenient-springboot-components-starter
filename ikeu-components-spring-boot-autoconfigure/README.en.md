English | [中文](README.md)

# ikeu-components-spring-boot-autoconfigure

All auto-configuration classes and `@ConfigurationProperties`. No manual `@Import` needed — Spring Boot 3.x discovers them via `AutoConfiguration.imports`.

## Registered Auto-Configurations

| Class | Activation | Description |
|-------|-----------|-------------|
| `WebAutoConfiguration` | always | Imports `SpringContextHolder` + `GlobalExceptionHandler` |
| `JacksonCustomAutoConfiguration` | Jackson on classpath | Global ObjectMapper customization (date/Long/timezone) + `@Sensitive` registration |
| `HttpClientAutoConfiguration` | always | Configures static `HttpClientUtil` from `ikeu.http-client.*` |
| `TraceAutoConfiguration` | enabled by default | TraceId MDC injection, order=`MIN_VALUE+100` |
| `CorsAutoConfiguration` | `ikeu.cors.enabled=true` | WebMvcConfigurer for CORS mappings |
| `XssAutoConfiguration` | `ikeu.xss.enabled=true` | XssFilter, order=`MIN_VALUE` |
| `SecurityAutoConfiguration` | `ikeu.jwt.enabled=true` | JwtUtils + JWT Filter/Interceptor + UserContext cleanup |
| `SecurityProtectionAutoConfiguration` | `ikeu.security-protection.enabled=true` + Spring Security on classpath | SecurityFilterChain (headers/CSRF/Session) |
| `OssAutoConfiguration` | `ikeu.oss.enabled=true` | AliyunOssTemplate or MinioOssTemplate |
| `MybatisPlusAutoConfiguration` | `ikeu.mybatis-plus.enabled=true` + MP on classpath | Pagination plugin + MetaObjectHandler |
| `RedisAutoConfiguration` | `ikeu.redis.enabled=true` + Redis on classpath | RedisTemplate(JSON) + RedisUtils + RedisLock + RedisLockHelper |

## Configuration Property Prefixes

| Prefix | Properties Class | Default |
|--------|-----------------|---------|
| `ikeu.jackson` | `JacksonProperties` | ✅ always |
| `ikeu.http-client` | `HttpClientProperties` | ✅ always |
| `ikeu.trace` | `TraceProperties` | ✅ true |
| `ikeu.jwt` | `JwtProperties` | ❌ false |
| `ikeu.oss` | `OssProperties` | ❌ false |
| `ikeu.cors` | `CorsProperties` | ❌ false |
| `ikeu.xss` | `XssProperties` | ❌ false |
| `ikeu.mybatis-plus` | `MybatisPlusProperties` | ❌ false |
| `ikeu.redis` | `RedisProperties` | ❌ false |
| `ikeu.security-protection` | `SecurityProtectionProperties` | ❌ false |

Full config reference: [application-example.yml](src/main/resources/application-example.yml)

## Filter Chain

```
Request
  │
  ├─ XssFilter                  order=Integer.MIN_VALUE     parameter sanitization
  ├─ TraceIdFilter              order=MIN_VALUE+100        MDC traceId injection
  ├─ JwtAuthenticationFilter   order=-100                  Bearer Token validation
  ├─ UserContextInterceptor    (MVC Interceptor)            @AnonymousAccess check
  │
  ├─ Controller
  │
  └─ UserContextClearFilter     order=Integer.MAX_VALUE    ThreadLocal cleanup
```

## Dependency Notes

The autoconfigure module depends on all functional modules (core/web/security/oss/redis). Users only need `ikeu-components-spring-boot-starter` to get everything transitively.

The following are `optional` — **not transitive**, users add as needed:

| Dependency | Unlocks |
|-----------|---------|
| `com.baomidou:mybatis-plus-spring-boot3-starter` | MyBatis-Plus pagination/fill |
| `org.springframework.boot:spring-boot-starter-data-redis` | Redis cache/lock |
| `org.springframework.boot:spring-boot-starter-security` | Spring Security headers |
| `com.aliyun.oss:aliyun-sdk-oss` | Alibaba Cloud OSS |
| `io.minio:minio` | MinIO OSS |

## Overriding Beans

All auto-configured beans use `@ConditionalOnMissingBean`. Define your own with the same name (or `FilterRegistrationBean`/`WebMvcConfigurer` name) to replace.

```java
// Override JwtUtils
@Bean
public JwtUtils jwtUtils(JwtProperties props) { ... }

// Override JWT Filter
@Bean("jwtFilterRegistration")
public FilterRegistrationBean<Filter> myFilter(...) { ... }

// Override RedisTemplate
@Bean("ikeuRedisTemplate")
public RedisTemplate<String, Object> myTemplate(...) { ... }

// Exclude entire auto-configuration
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
```
