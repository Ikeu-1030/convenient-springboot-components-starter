# ikeu-components — Spring Boot Convenience Toolkit

A comprehensive Spring Boot 3.2+ convenience toolkit that eliminates boilerplate in daily development. Provides unified response, JWT security, OSS abstraction, and common utility classes out of the box.

## Modules

| Module | Description |
|---|---|
| `ikeu-components-core` | Pure Java utilities — bean copy, JSON, date/time, string, tree, Snowflake ID |
| `ikeu-components-web` | Unified response `Result<T>`, paging, business exception, global handler, Spring context holder |
| `ikeu-components-security` | JWT (jjwt 0.12.x), BCrypt/AES password encryption, ThreadLocal user context |
| `ikeu-components-oss` | Abstract OSS template with Alibaba Cloud OSS and MinIO implementations |
| `ikeu-components-spring-boot-autoconfigure` | All auto-configuration, `@ConfigurationProperties`, and `spring.factories` equivalent |
| `ikeu-components-spring-boot-starter` | Entry-point starter — just add this dependency |

## Quick Start

### Maven

```xml
<dependency>
    <groupId>com.ikeu.components</groupId>
    <artifactId>ikeu-components-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.ikeu.components:ikeu-components-spring-boot-starter:0.0.1-SNAPSHOT'
```

### Configuration

```yaml
ikeu:
  jwt:
    enabled: true
    secret: "your-256-bit-secret-key-at-least-32-chars"
    expiration: 24h
```

## Features

- **Result & PageResult** — Unified API response wrappers with static factories
- **Global Exception Handler** — Catch `BusinessException`, validation errors, and unhandled exceptions
- **JWT Utilities** — Generate, parse, validate, refresh tokens (HS256/RS256)
- **Password Utilities** — BCrypt hashing + AES symmetric encryption
- **User Context** — ThreadLocal-based, auto-cleared after each request
- **OSS Abstraction** — Single interface for Alibaba Cloud OSS and MinIO
- **Bean Copy** — Null-excluding, depth-limited nested copy with `PropertyDescriptor` cache
- **Snowflake ID** — Clock-drift-safe, thread-safe distributed ID generator
- **Tree Builder** — Build tree from flat list, extract leaves, recursive sort

## Requirements

- Java 17+
- Spring Boot 3.2.x
- Lombok

## License

MIT
