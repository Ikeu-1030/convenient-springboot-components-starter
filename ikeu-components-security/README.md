[English](README.en.md) | 中文

# ikeu-components-security

JWT 认证、密码加密、用户上下文。依赖 `ikeu-components-web`。

## JwtUtils — JWT 令牌工具

### 两种模式

| | SINGLE（默认） | DUAL |
|---|------------|------|
| Token 数 | 1（Access） | 2（Access + Refresh） |
| 签名密钥 | 1 个共享 | 两个独立 |
| Access TTL | `expiration` | `access-expiration` |
| Refresh TTL | — | `refresh-expiration` |

### 配置

```yaml
ikeu:
  jwt:
    enabled: true
    mode: dual                     # single | dual
    secret: "access-key-at-least-32-characters!!"
    expiration: 2h
    header-name: Authorization
    token-prefix: "Bearer "
    # DUAL 模式的 Refresh 配置
    refresh-secret: "refresh-key-at-least-32-characters"
    refresh-expiration: 7d
    refresh-header-name: X-Refresh-Token
    # 过滤器配置
    auto-filter: true
    fail-on-invalid: false
    exclude-paths:
      - /public/**
      - /api/v1/auth/login
      - /api/v1/auth/refresh
```

> 每个 Token 的 secret/expiration/header 有回退链：专用配置 → 共享配置 → 硬编码默认值。

### API

```java
@Autowired private JwtUtils jwtUtils;

// SINGLE 模式
String token = jwtUtils.generateAccessToken(userId, Map.of("role", "admin"));
String uid = jwtUtils.getUserIdFromAccessToken(token);
boolean expired = jwtUtils.isAccessTokenExpired(token);

// DUAL 模式
TokenPair pair = jwtUtils.generateTokenPair(userId, Map.of("role", "admin"));
// pair.getAccessToken()    → 短时效
// pair.getRefreshToken()   → 长时效
// pair.getExpiresIn()     → 秒数

// 刷新
TokenPair newPair = jwtUtils.refreshAccessToken(pair.getRefreshToken());
```

## PasswordUtils — 密码与加密

```java
// BCrypt
String encoded = PasswordUtils.encode("123456");
boolean match = PasswordUtils.matches("123456", encoded);

// AES 对称加密（Base64 输出，随机 IV）
String encrypted = PasswordUtils.encryptAes("13800138000", "my-16byte-key!!");
String decrypted = PasswordUtils.decryptAes(encrypted, "my-16byte-key!!");
```

> AES 密钥自动适配: <16 字节补零, 16→AES-128, 24→AES-192, 32→AES-256。

## UserContextHolder — 用户上下文

ThreadLocal 隔离，请求结束自动清理。

```java
String userId = UserContextHolder.getUserId();
User user = UserContextHolder.getUser();
String role = UserContextHolder.getClaim("role");
Map<String, Object> claims = UserContextHolder.getClaims();

// 非 JWT 场景手动设置
UserContextHolder.setUserId("12345");
UserContextHolder.setUser(user);
```

> **必须清理：** `UserContextClearFilter`（order=MAX）和 `UserContextInterceptor.afterCompletion` 提供双重清理保障。自行设置时务必在 finally 中调用 `UserContextHolder.clear()`。

## JwtAuthenticationFilter — 认证过滤器

`auto-filter=true` 时自动注册（order=-100）。

流程：
1. 检查路径是否在 `exclude-paths` 中 → 跳过
2. 从 `Authorization` header 提取 `Bearer <token>`
3. 验证 Access Token → 成功则写入 `UserContextHolder`
4. Access Token 过期/无效且 DUAL 模式 → 尝试 Refresh Token
5. `fail-on-invalid=true` → 返回 401 JSON

## UserContextInterceptor — MVC 拦截器

当 Spring MVC 在 classpath 时自动注册。`preHandle` 逻辑：
1. Handler 有 `@AnonymousAccess` → 放行
2. 路径匹配 `exclude-paths` → 放行
3. `UserContextHolder.getUserId() == null` → 返回 **401 JSON**，阻塞请求
4. 否则放行

## @AnonymousAccess — 公开端点

```java
@AnonymousAccess
@PostMapping("/login")
public Result<LoginVO> login(@RequestBody LoginDTO dto) {
    // 不需要 JWT
}
```

可用于方法或类级别。

## TokenPair — Token 对模型

```java
String accessToken = pair.getAccessToken();
String refreshToken = pair.getRefreshToken();
long expiresIn = pair.getExpiresIn();        // Access Token 剩余秒数
long refreshExpiresIn = pair.getRefreshExpiresIn();
```

> **注意：** 本项目**不依赖 Spring Security 的 `SecurityContextHolder`**。认证完全通过自定义 servlet filter + ThreadLocal 完成，与 Spring Security 解耦。如需 Spring Security 的安全头保护，可开启 `ikeu.security-protection.enabled=true`。
