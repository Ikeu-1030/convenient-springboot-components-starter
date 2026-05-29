English | [中文](README.md)

# ikeu-components-websocket

WebSocket / STOMP real-time communication enhancements with JWT handshake authentication, STOMP channel interception, and user context injection. Depends on `ikeu-components-security`.

## Prerequisites

Requires `spring-websocket` and `spring-messaging` (included in Spring Boot Starter WebSocket):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

JWT authentication requires `ikeu-components-security` to be enabled first:

```yaml
ikeu:
  jwt:
    enabled: true
    secret: "your-256-bit-secret-at-least-32-characters"
```

## Configuration

```yaml
ikeu:
  websocket:
    enabled: true
    jwt-auth-enabled: true          # enable JWT auth (default)
    token-param: access_token       # SockJS query param name
    endpoint-paths: [/ws, /chat]    # STOMP endpoint paths
    handshake-whitelist: []         # paths excluded from JWT validation
    allowed-origins:                # allowed CORS origins (must configure in production)
      - "https://example.com"
    sock-js-enabled: true
```

## JwtWebSocketHandshakeInterceptor — Handshake Auth

Validates JWT during HTTP→WebSocket upgrade and injects user info into attributes.

**Token extraction order:**
1. URL query parameter `?access_token=<jwt>` (SockJS fallback)
2. HTTP Header `Authorization: Bearer <jwt>`

On success, writes to handshake `attributes`:
- `userId` — user ID
- `claims` — JWT Claims
- `loginUser` — login user identifier

```java
// Access in @MessageMapping methods
@MessageMapping("/chat.send")
public void send(ChatMessage msg, SimpMessageHeaderAccessor accessor) {
    String userId = WebSocketUserUtils.getUserId(accessor);
    // ...
}
```

## JwtStompChannelInterceptor — Channel Interception

Intercepts STOMP frames for authentication and authorization:

| Frame | Behavior |
|-------|----------|
| CONNECT | Extract JWT from `Authorization` header → validate → store in session attributes |
| SUBSCRIBE | Get current user → authorize via `StompAuthorizationValidator` → allow/deny |
| DISCONNECT | Auto-clear `UserContextHolder` |

**Returns `null` on auth failure — Spring automatically closes the connection.**

## StompAuthorizationValidator — Custom Authorization

The default validator only allows users to subscribe to their own private channels and public topics:

```java
// Default rules:
//   /user/{userId}/...   ✅ allowed (own scope only)
//   /topic/public/**      ✅ allowed
//   /queue/public/**      ✅ allowed
//   anything else          ❌ denied
```

Custom authorization:

```java
@Component
public class MyStompAuth implements StompAuthorizationValidator {
    @Override
    public boolean canSubscribe(String userId, String destination) {
        if (isAdmin(userId)) return true;
        return destination.startsWith("/user/" + userId + "/");
    }
}
```

## AbstractStompMessageBrokerConfigurer — Config Base Class

Simplifies STOMP configuration with automatic JWT interceptor registration:

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

## WebSocketUserUtils — Utility

Extract the current user ID from various contexts:

```java
// STOMP message processing
String userId = WebSocketUserUtils.getUserId(StompHeaderAccessor.wrap(message));

// @MessageMapping method
String userId = WebSocketUserUtils.getUserId(SimpMessageHeaderAccessor);

// Handshake phase
String userId = WebSocketUserUtils.getUserId(handshakeAttributes);

// ThreadLocal (already set by interceptor)
String userId = UserContextHolder.getUserId();
```

## Notes

- `allowed-origins` defaults to an empty list since `1.1.0` — must be explicitly configured in production
- The default `StompAuthorizationValidator` only allows user-scoped and public topic subscriptions; a WARN log at startup reminds you to customize it
- CONNECT failures cause immediate disconnect — handle `onClose` in the client
- `UserContextHolder` is auto-cleared on DISCONNECT; no manual cleanup needed
