# ikeu-components v1.1.3 Release Notes

> **SMS / Payment / WebSocket 三大新模块** — 扩展企业级集成能力

本次版本在 v1.1.0 基础上新增三个功能模块：短信服务抽象（阿里云/腾讯云）、支付网关抽象（微信支付/支付宝）、WebSocket/STOMP JWT 认证增强。同步修复若干安全问题，完善双语文档。

---

## 新增模块

### 短信服务（`ikeu-components-sms`）

| 类 | 说明 |
|---|------|
| `SmsTemplate` | 短信接口：`send()` / `sendBatch()` |
| `SmsException` | 统一短信异常（errorCode + message） |
| `SmsProperties` | `@ConfigurationProperties("ikeu.sms")` — ali/tencent 子配置 |
| `SmsAutoConfiguration` | 属性绑定，JavaDoc 提供阿里云/腾讯云实现示例 |

### 支付服务（`ikeu-components-payment`）

| 类 | 说明 |
|---|------|
| `PaymentTemplate` | 支付接口：`unifiedOrder()` / `queryOrder()` / `closeOrder()` / `refund()` / `verifyCallback()` |
| `PaymentCallbackHandler` | 模板方法抽象类——签名验证 → 解析 → `handleSuccess` / `handleFailure` |
| `PaymentRequest` / `PaymentResult` / `RefundRequest` | 统一请求/响应 DTO |
| `PaymentStatus` | 枚举：SUCCESS / CLOSED / REFUND / NOTPAY / USERPAYING |
| `PaymentException` | 统一支付异常 |
| `PaymentProperties` | `@ConfigurationProperties("ikeu.payment")` — wechat/alipay 子配置 |
| `PaymentAutoConfiguration` | 属性绑定，JavaDoc 提供微信/支付宝实现示例 |

### WebSocket/STOMP 实时通信（`ikeu-components-websocket`）

| 类 | 说明 |
|---|------|
| `JwtWebSocketHandshakeInterceptor` | HTTP→WebSocket 升级时验证 JWT，提取用户写入 attributes |
| `JwtStompChannelInterceptor` | CONNECT 认证 + SUBSCRIBE 鉴权 + DISCONNECT 清理 |
| `StompAuthorizationValidator` | `@FunctionalInterface` — 自定义订阅鉴权策略 |
| `AbstractStompMessageBrokerConfigurer` | STOMP 配置基类，子类只需覆写端点 + 消息代理 |
| `WebSocketUserUtils` | 从 `StompHeaderAccessor` / `SimpMessageHeaderAccessor` / handshake 提取 userId |
| `WebSocketProperties` | `@ConfigurationProperties("ikeu.websocket")` |
| `WebSocketAutoConfiguration` | 自动注册 JWT 拦截器 + 默认鉴权验证器 |

---

## 改进与增强

### Redis 自动配置增强

- 新增 `StringRedisTemplate` Bean，`@ConditionalOnMissingBean` 可覆盖
- `RedisAutoConfiguration` 文档补充 `StringRedisTemplate` 说明
- `RedisAutoConfigurationTest` 新增 `stringRedisTemplate` Bean 断言

### 安全修复

| 问题 | 严重性 | 文件 | 修复 |
|------|--------|------|------|
| STOMP `login` header 作为 JWT fallback | MEDIUM | `JwtStompChannelInterceptor` | 移除 `login` header fallback，仅接受标准 `Authorization: Bearer` |
| 默认 `StompAuthorizationValidator` 允许所有订阅 | MEDIUM | `WebSocketAutoConfiguration` | 默认仅允许 `/user/{userId}/...` + `/topic/public/**` + `/queue/public/**`，启动时 WARN 提示自定义 |
| WebSocket `allowed-origins` 默认值 `*` | MEDIUM | `WebSocketProperties` | 默认改为空列表，生产必须显式配置 |

### `JwtStompChannelInterceptor` 代码健壮性

- `handleConnect` 新增 session attributes null 检查，自动初始化空 Map
- `preSend` 改用 `StompHeaderAccessor.wrap()` 获取可变 accessor，处理后返回含修改 headers 的新消息

---

## 文档

- 新增三个模块各 2 份 README（中/英），含完整 API 示例和配置参考
- 父工程 README 更新模块地图、功能速览、配置属性、可选依赖表
- autoconfigure README 更新注册表和属性前缀表
- `application-example.yml` 新增 `ikeu.sms` / `ikeu.payment` / `ikeu.websocket` 配置段
- `ENV_VERSION.md` 新增 SMS/Payment/WebSocket 使用说明和依赖表
- 所有 RELEASE_NOTES 和 ENV_VERSION 统一整理至 `doc/` 目录

---

## 测试

- 新增 **44 个**单元测试（累计 **339 个**）
  - `JwtWebSocketHandshakeInterceptorTest` — 9 个
  - `JwtStompChannelInterceptorTest` — 13 个
  - `WebSocketUserUtilsTest` — 13 个
  - `WebSocketAutoConfigurationTest` — 9 个

---

## 模块依赖关系

```
ikeu-components-spring-boot-starter
  └── ikeu-components-spring-boot-autoconfigure
        ├── ikeu-components-core
        ├── ikeu-components-web
        ├── ikeu-components-security
        ├── ikeu-components-oss
        ├── ikeu-components-redis
        ├── ikeu-components-sms       ← 新增
        ├── ikeu-components-payment    ← 新增
        └── ikeu-components-websocket  ← 新增
```

---

## 配置示例

```yaml
ikeu:
  # ── 短信 ──
  sms:
    enabled: true
    type: aliyun
    default-sign-name: "XX平台"
    aliyun:
      access-key-id: "xxx"
      access-key-secret: "xxx"

  # ── 支付 ──
  payment:
    enabled: true
    type: wechat
    wechat:
      merchant-id: "xxx"
      api-v3-key: "xxx"

  # ── WebSocket ──
  websocket:
    enabled: true
    jwt-auth-enabled: true
    allowed-origins:
      - "https://example.com"
```

---

## 可选依赖

新模块的 SDK 依赖均**不传递**，用户按需添加：

| 功能 | 依赖 |
|------|------|
| SMS-阿里云 | `com.aliyun:dysmsapi20170525` |
| SMS-腾讯云 | `com.tencentcloudapi:tencentcloud-sdk-java-sms` |
| 支付-微信 | `com.github.wechatpay-apiv3:wechatpay-java` |
| 支付-支付宝 | `com.alipay.sdk:alipay-easysdk` |
| WebSocket | `org.springframework.boot:spring-boot-starter-websocket` |

---

## 技术环境

| 项 | 版本 |
|----|------|
| Java | 17+ |
| Spring Boot | 3.2.0 |
| Spring Framework | 6.1.1 |
| jjwt | 0.12.6 |
| Lombok | 1.18.30 |

---

## 仓库

[https://github.com/Ikeu-1030/convenient-springboot-components-starter](https://github.com/Ikeu-1030/convenient-springboot-components-starter)

## License

Apache-2.0
