[English](README.en.md) | 中文

# ikeu-components — Spring Boot 3.2+ Convenience Toolkit

> 本项目遵循Spring约定大于配置原则 · 消除复杂配置代码 · 开箱即用

一套面向 Spring Boot 3.2+ 单体/微服务应用的通用工具包，覆盖 Web 响应、JWT 认证、对象存储、Redis 缓存/锁、MyBatis-Plus 增强、敏感数据脱敏、安全防护等日常开发高频场景。

## 模块地图

| 模块 | 说明 | 文档 |
|------|------|------|
| `ikeu-components-parent` | 父 POM，统一版本管理 | — |
| `ikeu-components-core` | 纯 Java 工具（零 Spring 依赖） | [README](ikeu-components-core/README.md) |
| `ikeu-components-web` | 统一响应、分页、异常、断言 | [README](ikeu-components-web/README.md) |
| `ikeu-components-security` | JWT 双模式、密码加密、用户上下文 | [README](ikeu-components-security/README.md) |
| `ikeu-components-oss` | 对象存储抽象（阿里云/MinIO） | [README](ikeu-components-oss/README.md) |
| `ikeu-components-redis` | Redis 工具、分布式锁、缓存防护 | [README](ikeu-components-redis/README.md) |
| `ikeu-components-spring-boot-autoconfigure` | 全部自动配置 & Properties | [README](ikeu-components-spring-boot-autoconfigure/README.md) |
| `ikeu-components-spring-boot-starter` | 空 Starter，聚合上述全部 | — |

## 快速开始

### 1. 安装到本地

```bash
git clone git@github.com:Ikeu-1030/convenient-springboot-components-starter.git
cd convenient-springboot-components-starter
mvn clean install -DskipTests
```

### 2. 引入依赖

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

### 3. 最小配置

```yaml
ikeu:
  jwt:
    enabled: true
    secret: "your-256-bit-secret-at-least-32-characters"
```

### 4. 你的第一个接口

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

响应：
```json
{"code": 200, "message": "success", "data": {"id": 1, "name": "John"}}
```

## 功能速览

| 特性 | 所属模块 | 激活方式 |
|------|---------|---------|
| 统一响应 `Result<T>` + 分页 `PageResult<T>` | web | 自动 |
| 全局异常处理 `GlobalExceptionHandler` | web | 自动 |
| Bean 拷贝/转换/多源合并 | core | 自动 |
| 日期/JSON/字符串/Tree/Snowflake 工具 | core | 自动 |
| HTTP 客户端 `HttpClientUtil` | core | 自动 |
| 敏感数据脱敏 `@Sensitive` | core | 自动（Jackson 注册） |
| JWT 双 Token 认证（单/双模式） | security | `ikeu.jwt.enabled=true` |
| BCrypt + AES 密码加密 | security | 自动 |
| 用户上下文 `UserContextHolder` | security | 自动 |
| OSS 对象存储（阿里云/MinIO） | oss | `ikeu.oss.enabled=true` |
| Redis JSON 序列化 + 分布式锁 | redis | `ikeu.redis.enabled=true` |
| Redis 缓存穿透/击穿防护 | redis | `ikeu.redis.enabled=true` |
| MyBatis-Plus 分页 + 自动填充 | autoconfigure | `ikeu.mybatis-plus.enabled=true` |
| 请求追踪 TraceId（MDC） | autoconfigure | 默认启用 |
| CORS 跨域 | autoconfigure | `ikeu.cors.enabled=true` |
| XSS 防护 | autoconfigure | `ikeu.xss.enabled=true` |
| Spring Security 安全头/CSRF/Session | autoconfigure | `ikeu.security-protection.enabled=true` |
| Jackson 全局配置 | autoconfigure | 自动 |

## 配置属性一览

```yaml
ikeu:
  jackson:           # JSON 序列化 — 详见 autoconfigure README
  http-client:       # HTTP 客户端超时/代理
  jwt:               # JWT 认证 — 详见 security README
  oss:               # 对象存储 — 详见 oss README
  redis:             # Redis — 详见 redis README
  mybatis-plus:      # MyBatis-Plus 分页/填充
  trace:             # 请求追踪 (默认启用)
  cors:              # 跨域配置
  xss:               # XSS 防护
  security-protection: # Spring Security 安全头
```

完整配置示例见：[application-example.yml](ikeu-components-spring-boot-autoconfigure/src/main/resources/application-example.yml)

## Filter 执行顺序

```
Integer.MIN_VALUE     → XssFilter             参数安全
Integer.MIN_VALUE+100 → TraceIdFilter         请求追踪 (MDC)
-100                  → JwtAuthenticationFilter JWT 认证
Integer.MAX_VALUE     → UserContextClearFilter ThreadLocal 清理
```

## 覆盖自动配置的 Bean

所有自动配置的 Bean 均使用 `@ConditionalOnMissingBean`，可直接替换：

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

## 可选依赖

以下功能需要用户显式添加依赖才能激活（未传递）：

| 功能 | 需要的依赖 |
|------|-----------|
| OSS-阿里云 | `com.aliyun.oss:aliyun-sdk-oss` |
| OSS-MinIO | `io.minio:minio` |
| MyBatis-Plus | `com.baomidou:mybatis-plus-spring-boot3-starter` |
| Redis | `org.springframework.boot:spring-boot-starter-data-redis` |
| Spring Security | `org.springframework.boot:spring-boot-starter-security` |

## 环境要求

- Java 17+
- Spring Boot 3.2.x
- Maven 3.8+

## License

Apache-2.0
