English | [中文](README.md)

# ikeu-components — Spring Boot 3.2+ Convenience Toolkit

> Convention over Configuration · Eliminate Boilerplate · Batteries Included

A general-purpose toolkit for Spring Boot 3.2+ monolithic/microservice applications, covering web responses, JWT authentication, object storage, Redis caching/locking, MyBatis-Plus enhancements, sensitive data masking, security protection, and more.

## Module Map

| Module | Description | Docs |
|--------|-------------|------|
| `ikeu-components-parent` | Parent POM, unified version management | — |
| `ikeu-components-core` | Pure Java utilities (zero Spring dependency) | [README](ikeu-components-core/README.en.md) |
| `ikeu-components-web` | Unified response, paging, exception, assertions | [README](ikeu-components-web/README.en.md) |
| `ikeu-components-security` | JWT dual-mode, password encryption, user context | [README](ikeu-components-security/README.en.md) |
| `ikeu-components-oss` | Object storage abstraction (Aliyun/MinIO) | [README](ikeu-components-oss/README.en.md) |
| `ikeu-components-redis` | Redis utils, distributed lock, cache protection | [README](ikeu-components-redis/README.en.md) |
| `ikeu-components-spring-boot-autoconfigure` | All auto-configuration & Properties | [README](ikeu-components-spring-boot-autoconfigure/README.en.md) |
| `ikeu-components-spring-boot-starter` | Empty starter, aggregates all above | — |

## Quick Start

### 1. Local Install

```bash
git clone git@github.com:Ikeu-1030/convenient-springboot-components-starter.git
cd convenient-springboot-components-starter
mvn clean install -DskipTests
```

### 2. Add Dependency

**Maven**
```xml
<dependency>
    <groupId>com.ikeu.components</groupId>
    <artifactId>ikeu-components-spring-boot-starter</artifactId>
    <version>1.1.0</version>
</dependency>
```

**Gradle**
```groovy
implementation 'com.ikeu.components:ikeu-components-spring-boot-starter:1.1.0'
```

### 3. Minimal Configuration

```yaml
ikeu:
  jwt:
    enabled: true
    secret: "your-256-bit-secret-at-least-32-characters"
```

### 4. Your First Endpoint

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping("/{id}")
    public Result<UserVO> get(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }
}
```

Response:
```json
{"code": 200, "message": "success", "data": {"id": 1, "name": "John"}}
```

## Features at a Glance

| Feature | Module | Activation |
|---------|--------|------------|
| Unified response `Result<T>` + paging `PageResult<T>` | web | auto |
| Global exception handler | web | auto |
| Bean copy / convert / merge | core | auto |
| Date / JSON / String / Tree / Snowflake utils | core | auto |
| HTTP client `HttpClientUtil` | core | auto |
| Sensitive data masking `@Sensitive` | core | auto (Jackson) |
| JWT dual-token auth (single/dual mode) | security | `ikeu.jwt.enabled=true` |
| BCrypt + AES encryption | security | auto |
| User context `UserContextHolder` | security | auto |
| OSS (Aliyun/MinIO) | oss | `ikeu.oss.enabled=true` |
| Redis JSON serialization + distributed lock | redis | `ikeu.redis.enabled=true` |
| Cache penetration/breakdown protection | redis | `ikeu.redis.enabled=true` |
| MyBatis-Plus pagination + auto-fill | autoconfigure | `ikeu.mybatis-plus.enabled=true` |
| Request tracing TraceId (MDC) | autoconfigure | enabled by default |
| CORS | autoconfigure | `ikeu.cors.enabled=true` |
| XSS protection | autoconfigure | `ikeu.xss.enabled=true` |
| Spring Security headers/CSRF/Session | autoconfigure | `ikeu.security-protection.enabled=true` |
| Jackson global config | autoconfigure | auto |

## Configuration Properties

```yaml
ikeu:
  jackson:           # JSON serialization — see autoconfigure README
  http-client:       # HTTP client timeout/proxy
  jwt:               # JWT auth — see security README
  oss:               # Object storage — see oss README
  redis:             # Redis — see redis README
  mybatis-plus:      # MyBatis-Plus pagination/fill
  trace:             # Request tracing (enabled by default)
  cors:              # CORS config
  xss:               # XSS protection
  security-protection: # Spring Security headers
```

Full example: [application-example.yml](ikeu-components-spring-boot-autoconfigure/src/main/resources/application-example.yml)

## Filter Chain Order

```
Integer.MIN_VALUE     → XssFilter             parameter sanitization
Integer.MIN_VALUE+100 → TraceIdFilter         request tracing (MDC)
-100                  → JwtAuthenticationFilter JWT auth
Integer.MAX_VALUE     → UserContextClearFilter ThreadLocal cleanup
```

## Overriding Auto-Configured Beans

All beans use `@ConditionalOnMissingBean` and can be replaced:

```java
@Configuration
public class MyConfig {

    @Bean
    public JwtUtils jwtUtils(JwtProperties props) {
        return new JwtUtils(/* your custom config */);
    }

    @Bean("jwtFilterRegistration")
    public FilterRegistrationBean<Filter> myJwtFilter(...) {
        // your custom filter
    }
}
```

## Optional Dependencies

The following require explicit user dependencies to activate (not transitive):

| Feature | Required Dependency |
|---------|-------------------|
| OSS-Aliyun | `com.aliyun.oss:aliyun-sdk-oss` |
| OSS-MinIO | `io.minio:minio` |
| MyBatis-Plus | `com.baomidou:mybatis-plus-spring-boot3-starter` |
| Redis | `org.springframework.boot:spring-boot-starter-data-redis` |
| Spring Security | `org.springframework.boot:spring-boot-starter-security` |

## Requirements

- Java 17+
- Spring Boot 3.2.x
- Maven 3.8+

## License

Apache-2.0
