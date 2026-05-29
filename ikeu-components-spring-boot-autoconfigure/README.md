[English](README.en.md) | 中文

# ikeu-components-spring-boot-autoconfigure

全部自动配置类和 `@ConfigurationProperties` 所在模块。用户无需手动 `@Import`，Spring Boot 3.x 通过 `AutoConfiguration.imports` 自动发现。

## 注册的自动配置

| 配置类 | 激活条件 | 说明 |
|--------|---------|------|
| `WebAutoConfiguration` | 始终 | 导入 `SpringContextHolder` + `GlobalExceptionHandler` |
| `JacksonCustomAutoConfiguration` | Jackson 在 classpath | 全局 ObjectMapper 定制（日期/Long/时区）+ `@Sensitive` 注册 |
| `HttpClientAutoConfiguration` | 始终 | 根据 `ikeu.http-client.*` 配置静态 `HttpClientUtil` |
| `TraceAutoConfiguration` | 默认启用 | TraceId MDC 注入，order=`MIN_VALUE+100` |
| `CorsAutoConfiguration` | `ikeu.cors.enabled=true` | WebMvcConfigurer 注册 CORS 映射 |
| `XssAutoConfiguration` | `ikeu.xss.enabled=true` | XssFilter，order=`MIN_VALUE` |
| `SecurityAutoConfiguration` | `ikeu.jwt.enabled=true` | JwtUtils + JWT Filter/Interceptor + UserContext 清理 |
| `SecurityProtectionAutoConfiguration` | `ikeu.security-protection.enabled=true` + Spring Security 在 classpath | SecurityFilterChain（安全头/CSRF/Session） |
| `OssAutoConfiguration` | `ikeu.oss.enabled=true` | AliyunOssTemplate 或 MinioOssTemplate |
| `MybatisPlusAutoConfiguration` | `ikeu.mybatis-plus.enabled=true` + MP 在 classpath | 分页插件 + MetaObjectHandler |
| `RedisAutoConfiguration` | `ikeu.redis.enabled=true` + Redis 在 classpath | RedisTemplate(JSON) + RedisUtils + RedisLock + RedisLockHelper |
| `SmsAutoConfiguration` | `ikeu.sms.enabled=true` | 绑定 SmsProperties（需用户提供 SmsTemplate Bean） |
| `PaymentAutoConfiguration` | `ikeu.payment.enabled=true` | 绑定 PaymentProperties（需用户提供 PaymentTemplate Bean） |
| `WebSocketAutoConfiguration` | `ikeu.websocket.enabled=true` + JwtUtils + spring-websocket 在 classpath | JWT 握手拦截器 + STOMP 通道拦截器 + 鉴权验证器 |

## 配置属性前缀

| 前缀 | 属性类 | 默认启用 |
|------|--------|---------|
| `ikeu.jackson` | `JacksonProperties` | ✅ always |
| `ikeu.http-client` | `HttpClientProperties` | ✅ always |
| `ikeu.trace` | `TraceProperties` | ✅ true |
| `ikeu.jwt` | `JwtProperties` | ❌ false |
| `ikeu.oss` | `OssProperties` | ❌ false |
| `ikeu.cors` | `CorsProperties` | ❌ false |
| `ikeu.xss` | `XssProperties` | ❌ false |
| `ikeu.mybatis-plus` | `MybatisPlusProperties` | ❌ false |
| `ikeu.redis` | `RedisProperties` | ❌ false |
| `ikeu.sms` | `SmsProperties` | ❌ false |
| `ikeu.payment` | `PaymentProperties` | ❌ false |
| `ikeu.websocket` | `WebSocketProperties` | ❌ false |
| `ikeu.security-protection` | `SecurityProtectionProperties` | ❌ false |

完整配置参考：[application-example.yml](src/main/resources/application-example.yml)

## Filter 执行链

```
Request
  │
  ├─ XssFilter                  order=Integer.MIN_VALUE     参数转义/标签剥离
  ├─ TraceIdFilter              order=MIN_VALUE+100         MDC traceId 注入
  ├─ JwtAuthenticationFilter   order=-100                  Bearer Token 验证
  ├─ UserContextInterceptor    (MVC Interceptor)            @AnonymousAccess 检查
  │
  ├─ Controller
  │
  └─ UserContextClearFilter     order=Integer.MAX_VALUE    ThreadLocal 清理
```

## 依赖说明

autoconfigure 模块直接依赖所有功能模块（core/web/security/oss/redis），用户只需引入 `ikeu-components-spring-boot-starter` 即可传递获得。

以下为 `optional`，**不传递**，用户按需添加：

| 依赖 | 解锁功能 |
|------|---------|
| `com.baomidou:mybatis-plus-spring-boot3-starter` | MyBatis-Plus 分页/填充 |
| `org.springframework.boot:spring-boot-starter-data-redis` | Redis 缓存/锁 |
| `org.springframework.boot:spring-boot-starter-security` | Spring Security 安全头 |
| `com.aliyun.oss:aliyun-sdk-oss` | 阿里云 OSS |
| `io.minio:minio` | MinIO OSS |
| `com.aliyun:dysmsapi20170525` | 阿里云 SMS |
| `com.tencentcloudapi:tencentcloud-sdk-java-sms` | 腾讯云 SMS |
| `com.github.wechatpay-apiv3:wechatpay-java` | 微信支付 |
| `com.alipay.sdk:alipay-easysdk` | 支付宝支付 |
| `org.springframework:spring-websocket` / `spring-messaging` | WebSocket/STOMP |

## 覆盖 Bean

所有自动配置的 Bean 都使用 `@ConditionalOnMissingBean`，用户定义同名（或同名 `FilterRegistrationBean`/`WebMvcConfigurer`）Bean 即可覆盖。部分 Bean 使用 `@ConditionalOnMissingBean(name = "...")` 精确匹配名称。

```java
// 覆盖 JwtUtils
@Bean
public JwtUtils jwtUtils(JwtProperties props) { ... }

// 覆盖 JWT Filter
@Bean("jwtFilterRegistration")
public FilterRegistrationBean<Filter> myFilter(...) { ... }

// 覆盖 RedisTemplate
@Bean("ikeuRedisTemplate")
public RedisTemplate<String, Object> myTemplate(...) { ... }

// 排除整个自动配置
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
```
