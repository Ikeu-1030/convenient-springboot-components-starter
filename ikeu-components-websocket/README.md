[English](README.en.md) | 中文

# ikeu-components-websocket

WebSocket / STOMP 实时通信增强，提供 JWT 握手认证、STOMP 通道拦截、用户上下文注入。依赖 `ikeu-components-security`。

## 前置条件

需要 `spring-websocket` 和 `spring-messaging`（Spring Boot Starter WebSocket 已包含）：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

JWT 认证依赖 `ikeu-components-security`，需先启用：

```yaml
ikeu:
  jwt:
    enabled: true
    secret: "your-256-bit-secret-at-least-32-characters"
```

## 配置

```yaml
ikeu:
  websocket:
    enabled: true
    jwt-auth-enabled: true          # 启用 JWT 认证（默认）
    token-param: access_token       # SockJS 查询参数名
    endpoint-paths: [/ws, /chat]    # STOMP 端点路径
    handshake-whitelist: []         # 跳过 JWT 验证的路径
    allowed-origins:                # 允许的跨域来源（生产必须显式配置）
      - "https://example.com"
    sock-js-enabled: true
```

## JwtWebSocketHandshakeInterceptor — 握手认证

在 HTTP → WebSocket 升级时验证 JWT，提取用户信息写入 attributes。

**Token 提取顺序：**
1. URL 查询参数 `?access_token=<jwt>`（SockJS fallback）
2. HTTP Header `Authorization: Bearer <jwt>`

验证通过后将以下信息写入握手 `attributes`：
- `userId` — 用户 ID
- `claims` — JWT Claims
- `loginUser` — 登录用户标识

```java
// 在 @MessageMapping 方法中访问
@MessageMapping("/chat.send")
public void send(ChatMessage msg, SimpMessageHeaderAccessor accessor) {
    String userId = WebSocketUserUtils.getUserId(accessor);
    // ...
}
```

## JwtStompChannelInterceptor — 通道拦截

拦截 STOMP 帧，提供认证和鉴权：

| 帧 | 行为 |
|----|------|
| CONNECT | 从 `Authorization` header 提取 JWT → 验证 → 写入 session attributes |
| SUBSCRIBE | 获取当前用户 → 通过 `StompAuthorizationValidator` 鉴权 → 允许/拒绝 |
| DISCONNECT | 自动调用 `UserContextHolder.clear()` |

**认证失败时返回 `null`，Spring 自动关闭连接。**

## StompAuthorizationValidator — 自定义鉴权

默认验证器只允许用户订阅自己的私有频道和公共主题：

```java
// 默认规则：
//   /user/{userId}/...   ✅ 允许（仅自己）
//   /topic/public/**      ✅ 允许
//   /queue/public/**      ✅ 允许
//   其他                   ❌ 拒绝
```

自定义鉴权规则：

```java
@Component
public class MyStompAuth implements StompAuthorizationValidator {
    @Override
    public boolean canSubscribe(String userId, String destination) {
        // 管理员可以订阅任何频道
        if (isAdmin(userId)) return true;
        // 普通用户只能订阅自己的频道
        return destination.startsWith("/user/" + userId + "/");
    }
}
```

## AbstractStompMessageBrokerConfigurer — 配置基类

简化 STOMP 配置，自动注册 JWT 拦截器：

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractStompMessageBrokerConfigurer {

    public WebSocketConfig(
            JwtWebSocketHandshakeInterceptor handshakeInterceptor,
            JwtStompChannelInterceptor channelInterceptor) {
        super(handshakeInterceptor, channelInterceptor, List.of("/ws"));
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }
}
```

## WebSocketUserUtils — 工具类

从不同上下文中获取当前用户 ID：

```java
// STOMP 消息处理
String userId = WebSocketUserUtils.getUserId(StompHeaderAccessor.wrap(message));

// @MessageMapping 方法
String userId = WebSocketUserUtils.getUserId(SimpMessageHeaderAccessor);

// 握手阶段
String userId = WebSocketUserUtils.getUserId(handshakeAttributes);

// ThreadLocal（已在拦截器中设置）
String userId = UserContextHolder.getUserId();
```

## 注意事项

- `allowed-origins` 默认值在 `1.1.0` 版本后已改为空列表，生产环境必须显式配置
- 默认 `StompAuthorizationValidator` 仅允许用户自有频道和公共主题，启动时会有 WARN 日志提示自定义
- CONNECT 认证失败时客户端会直接断开连接，前端需处理 `onClose` 事件
- `UserContextHolder` 在 DISCONNECT 时自动清理，无需手动调用
