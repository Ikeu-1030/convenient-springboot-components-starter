English | [中文](README.md)

# ikeu-components-security

JWT authentication, password encryption, user context. Depends on `ikeu-components-web`.

## JwtUtils — JWT Token Utility

### Two Modes

| | SINGLE (default) | DUAL |
|---|------------|------|
| Tokens | 1 (Access) | 2 (Access + Refresh) |
| Signing Keys | 1 shared | 2 independent |
| Access TTL | `expiration` | `access-expiration` |
| Refresh TTL | — | `refresh-expiration` |

### Configuration

```yaml
ikeu:
  jwt:
    enabled: true
    mode: dual                     # single | dual
    secret: "access-key-at-least-32-characters!!"
    expiration: 2h
    header-name: Authorization
    token-prefix: "Bearer "
    # DUAL mode refresh config
    refresh-secret: "refresh-key-at-least-32-characters"
    refresh-expiration: 7d
    refresh-header-name: X-Refresh-Token
    # Filter config
    auto-filter: true
    fail-on-invalid: false
    exclude-paths:
      - /public/**
      - /api/v1/auth/login
      - /api/v1/auth/refresh
```

> Each token's secret/expiration/header uses a fallback chain: dedicated config → shared config → hardcoded default.

### API

```java
@Autowired private JwtUtils jwtUtils;

// SINGLE mode
String token = jwtUtils.generateAccessToken(userId, Map.of("role", "admin"));
String uid = jwtUtils.getUserIdFromAccessToken(token);
boolean expired = jwtUtils.isAccessTokenExpired(token);

// DUAL mode
TokenPair pair = jwtUtils.generateTokenPair(userId, Map.of("role", "admin"));
// pair.getAccessToken()    → short-lived
// pair.getRefreshToken()   → long-lived
// pair.getExpiresIn()     → seconds

// Refresh
TokenPair newPair = jwtUtils.refreshAccessToken(pair.getRefreshToken());
```

## PasswordUtils — Password & Encryption

```java
// BCrypt
String encoded = PasswordUtils.encode("123456");
boolean match = PasswordUtils.matches("123456", encoded);

// AES symmetric encryption (Base64 output, random IV)
String encrypted = PasswordUtils.encryptAes("13800138000", "my-16byte-key!!");
String decrypted = PasswordUtils.decryptAes(encrypted, "my-16byte-key!!");
```

> AES key auto-adapts: <16 bytes padded, 16→AES-128, 24→AES-192, 32→AES-256.

## UserContextHolder — Per-Request User Context

ThreadLocal-based, automatically cleared at request end.

```java
String userId = UserContextHolder.getUserId();
User user = UserContextHolder.getUser();
String role = UserContextHolder.getClaim("role");
Map<String, Object> claims = UserContextHolder.getClaims();

// Manual usage (non-JWT scenarios)
UserContextHolder.setUserId("12345");
UserContextHolder.setUser(user);
```

> **Must clean up:** `UserContextClearFilter` (order=MAX) and `UserContextInterceptor.afterCompletion` provide dual-cleanup. Always call `UserContextHolder.clear()` in `finally` when setting manually.

## JwtAuthenticationFilter — Auth Filter

Auto-registered when `auto-filter=true` (order=-100).

Flow:
1. Check if path matches `exclude-paths` → skip
2. Extract `Bearer <token>` from `Authorization` header
3. Validate Access Token → write to `UserContextHolder`
4. Access Token expired/invalid & DUAL mode → try Refresh Token
5. `fail-on-invalid=true` → return 401 JSON

## UserContextInterceptor — MVC Interceptor

Auto-registered when Spring MVC is on classpath. `preHandle` logic:
1. Handler has `@AnonymousAccess` → allow
2. Path matches `exclude-paths` → allow
3. `UserContextHolder.getUserId() == null` → return **401 JSON**, block
4. Otherwise → allow

## @AnonymousAccess — Public Endpoint

```java
@AnonymousAccess
@PostMapping("/login")
public Result<LoginVO> login(@RequestBody LoginDTO dto) {
    // No JWT required
}
```

Can be placed on method or class level.

## TokenPair — Token Pair Model

```java
String accessToken = pair.getAccessToken();
String refreshToken = pair.getRefreshToken();
long expiresIn = pair.getExpiresIn();         // Access token seconds remaining
long refreshExpiresIn = pair.getRefreshExpiresIn();
```

> **Note:** This project does **not** use Spring Security's `SecurityContextHolder`. Authentication is entirely handled by custom servlet filters + ThreadLocal. For Spring Security header protection, enable `ikeu.security-protection.enabled=true`.
