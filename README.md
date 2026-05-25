# ikeu-components — Spring Boot Convenience Toolkit

A Spring Boot 3.2+ general-purpose toolkit that eliminates boilerplate in daily development. Provides unified API response, JWT authentication, object storage abstraction, and common utilities — all out of the box.

## Module Overview

```
ikeu-components-parent
├── ikeu-components-core                     Pure Java utilities (zero Spring dependency)
├── ikeu-components-web                      Unified response, paging, exception, assertions, SpringContext
├── ikeu-components-security                 JWT, password encryption, user context, auth filter/interceptor
├── ikeu-components-oss                      Object storage abstraction (Alibaba Cloud OSS / MinIO)
├── ikeu-components-spring-boot-autoconfigure All auto-configuration & @ConfigurationProperties
└── ikeu-components-spring-boot-starter       Empty starter — just pull this one dependency
```

## Installation

> This project is **not published to Maven Central**. Choose one of the two methods below.

### Option 1: Local Install

```bash
git clone git@github.com:Ikeu-1030/convenient-springboot-components-starter.git
cd convenient-springboot-components-starter
mvn clean install -DskipTests
```

**Maven**
```xml
<dependency>
    <groupId>com.ikeu.components</groupId>
    <artifactId>ikeu-components-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle**
```groovy
implementation 'com.ikeu.components:ikeu-components-spring-boot-starter:1.0.0'
```

### Option 2: JitPack (pull directly from GitHub)

**Maven**
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.Ikeu-1030</groupId>
    <artifactId>convenient-springboot-components-starter</artifactId>
    <version>main-SNAPSHOT</version>
</dependency>
```

**Gradle**
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.Ikeu-1030:convenient-springboot-components-starter:main-SNAPSHOT'
}
```

---

## Quick Start

### Minimal Configuration

Enable the modules you need in `application.yml`:

```yaml
ikeu:
  jwt:
    enabled: true
    secret: "your-256-bit-secret-at-least-32-characters"
```

### Your First Endpoint

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping("/{id}")
    public Result<UserVO> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        UserVO vo = BeanConverter.convert(user, UserVO.class);
        return Result.success(vo);
    }
}
```

Response format:

```json
{
  "code": 200,
  "message": "success",
  "data": { "id": 1, "name": "John" }
}
```

---

## Full Configuration Reference

```yaml
ikeu:
  # ── Jackson JSON Configuration ──
  jackson:
    date-pattern: "yyyy-MM-dd HH:mm:ss"  # Default date format
    long-as-string: true                 # Serialize Long as String (JS precision)
    serialization-inclusion: non_null    # non_null | non_default | non_absent | non_empty | always
    time-zone: Asia/Shanghai             # Timezone for date formatting

  # ── HTTP Client Configuration ──
  http-client:
    connect-timeout: 10s                 # Connection timeout
    request-timeout: 30s                 # Per-request timeout
    download-timeout: 5m                 # Download timeout
    # proxy-host: 127.0.0.1              # Proxy (both host and port required)
    # proxy-port: 8888

  # ── JWT Authentication ──
  jwt:
    enabled: false                  # Enable JWT auto-configuration (REQUIRED, default: false)
    mode: single                    # Token mode: single (one token) or dual (access + refresh)
    secret: "change-me-32chars..."  # HMAC-SHA256 signing key (≥ 32 characters recommended)
    expiration: 24h                 # Token expiration (supports ms/s/m/h/d suffixes)
    algorithm: HS256                # Signing algorithm: HS256 or RS256
    header-name: Authorization      # HTTP header to extract the token from
    token-prefix: "Bearer "         # Token prefix before the actual token string
    # ── Access token overrides (inherits from shared defaults) ──
    # access-secret: "access-secret-32chars-min"
    # access-expiration: 2h
    # access-header-name: Authorization
    # access-token-prefix: "Bearer "
    # ── Refresh token overrides (only used in DUAL mode) ──
    # refresh-secret: "refresh-secret-32chars-min"
    # refresh-expiration: 7d
    # refresh-header-name: X-Refresh-Token
    # refresh-token-prefix: "Bearer "
    auto-filter: true               # Auto-register JwtAuthenticationFilter
    fail-on-invalid: false          # Return 401 on invalid/missing token (false = let app decide)
    exclude-paths:                  # Paths excluded from JWT validation (Ant-style patterns)
      - /public/**
      - /actuator/health
      - /api/v1/auth/login
      - /api/v1/auth/refresh

  # ── Object Storage ──
  oss:
    enabled: false
    type: aliyun                    # aliyun or minio
    endpoint: "https://oss-cn-hangzhou.aliyuncs.com"
    access-key: "your-access-key"
    secret-key: "your-secret-key"
    bucket: "your-bucket"
    cdn-domain: "cdn.example.com"   # Optional, used by getPublicUrl
```

---

## Feature Guide

### 1. Unified Response — `Result<T>`

| Static method | Description | Example |
|---|---|---|
| `Result.success(data)` | Success with data, code=200 | `Result.success(userVO)` |
| `Result.success(msg, data)` | Success with message | `Result.success("Login OK", tokenVO)` |
| `Result.error(code, msg)` | Business error with code | `Result.error(404, "User not found")` |
| `Result.error(msg)` | Error with default code 500 | `Result.error("Operation failed")` |
| `Result.of(data)` | Auto-detect: non-null → success, null → error | `Result.of(maybeNull)` |

```java
@GetMapping("/{id}")
public Result<UserVO> get(@PathVariable Long id) {
    UserVO vo = userService.getById(id);
    return Result.success(vo);
}

@PostMapping
public Result<Void> create(@Valid @RequestBody UserDTO dto) {
    userService.create(dto);
    return Result.success();
}
```

### 2. Paging Response — `PageResult<T>`

```java
@GetMapping
public Result<PageResult<UserVO>> list(UserPageQuery query) {
    IPage<User> page = userService.page(query);
    List<UserVO> vos = BeanConverter.convertList(page.getRecords(), UserVO.class);
    return Result.success(PageResult.of(page, vos));
}
```

Response:

```json
{
  "code": 200,
  "data": {
    "total": 150,
    "current": 1,
    "pages": 15,
    "records": [ ... ]
  }
}
```

### 3. Business Exceptions & Global Handler

Throw `BusinessException` — `GlobalExceptionHandler` catches it automatically and returns a `Result`:

```java
AssertUtils.notNull(user, "User not found");
AssertUtils.isTrue(balance >= amount, "Insufficient balance");
AssertUtils.notBlank(username, "Username is required");

throw new BusinessException(404, "Order not found");
throw new BusinessException("Operation failed"); // code defaults to 500
```

The global handler covers four exception types:

| Exception | HTTP Status | Behavior |
|---|---|---|
| `BusinessException` | 200 | Uses the exception's code and message |
| `MethodArgumentNotValidException` | 400 | Extracts field errors, grouped by field name |
| `IllegalArgumentException` | 400 | Returns the exception message |
| `Exception` (catch-all) | 500 | Logs the full stack trace, returns generic message |

> `GlobalExceptionHandler` is auto-imported via `WebAutoConfiguration`. No extra configuration needed.

### 4. Bean Copy — `BeanCopyUtils` & `BeanConverter`

**Standard copy**

```java
// Source → target (nulls skipped, max depth 3 to prevent infinite loops)
BeanCopyUtils.copyProperties(source, target);

// Copy a list
List<UserVO> vos = BeanCopyUtils.copyList(users, UserVO.class);
```

**Multi-source merge — `BeanConverter.combine()`**

```java
// Merge non-null properties from multiple sources into a new target instance.
// Later sources override earlier ones for matching property names.
UserVO vo = BeanConverter.combine(UserVO.class, user, userExt, sessionInfo);

// With a post-merge callback
UserVO vo = BeanConverter.combine(UserVO.class, combined -> {
    combined.setFullName(combined.getFirstName() + " " + combined.getLastName());
}, user, userExt);
```

**Type conversion with custom mappings**

```java
// Basic conversion
UserVO vo = BeanConverter.convert(user, UserVO.class);
List<UserVO> list = BeanConverter.convertList(users, UserVO.class);

// MyBatis-Plus Page conversion
Map<String, Object> pageMap = BeanConverter.convertPage(mybatisPlusPage, UserVO.class);

// Register a custom converter (typically in @PostConstruct)
BeanConverter.register(User.class, UserVO.class, (src, tgt) -> {
    tgt.setDisplayName(src.getNickname() + " (" + src.getUsername() + ")");
    BeanCopyUtils.copyProperties(src, tgt);
    return tgt;
});
```

### 5. JWT Authentication

#### 5.1 Token Modes — SINGLE vs DUAL

| | SINGLE | DUAL |
|---|--------|------|
| Tokens | 1 (access) | 2 (access + refresh) |
| Signing keys | 1 | 2 (independent) |
| Access TTL | `expiration` (e.g. 24h) | `access-expiration` (e.g. 2h) |
| Refresh TTL | — | `refresh-expiration` (e.g. 7d) |
| Headers | `Authorization` | `Authorization` + `X-Refresh-Token` |

```yaml
# DUAL mode config example
ikeu:
  jwt:
    enabled: true
    mode: dual
    access-secret: "access-key-at-least-32-characters-!"
    access-expiration: 2h
    refresh-secret: "refresh-key-at-least-32-characters"
    refresh-expiration: 7d
```

**Fallback chain**: each token's secret/expiration/header falls back to the shared default when not explicitly set. E.g. `access-secret` → `jwt.secret` → hardcoded default.

#### 5.2 Token Generation & Parsing

```java
@Autowired
private JwtUtils jwtUtils;

// ── SINGLE mode ──
String token = jwtUtils.generateAccessToken(userId, Map.of("role", "admin"));
String userId = jwtUtils.getUserIdFromAccessToken(token);
boolean expired = jwtUtils.isAccessTokenExpired(token);

// ── DUAL mode — token pair ──
TokenPair pair = jwtUtils.generateTokenPair(userId, Map.of("role", "admin"));
// pair.getAccessToken()   → short-lived (e.g. 2h)
// pair.getRefreshToken()  → long-lived (e.g. 7d)
// pair.getExpiresIn()    → seconds until access token expires

// Refresh using a valid refresh token
TokenPair newPair = jwtUtils.refreshAccessToken(pair.getRefreshToken());
```

#### 5.2 JwtAuthenticationFilter (auto-registered)

When `ikeu.jwt.auto-filter=true` (the default), `JwtAuthenticationFilter` is registered automatically:

1. Extracts `Bearer <token>` from the configured header (default: `Authorization`)
2. Validates the token
3. Writes userId and claims into `UserContextHolder`
4. Cleans up after the request completes

```yaml
ikeu:
  jwt:
    exclude-paths:           # Paths that skip JWT validation entirely
      - /api/v1/auth/login
      - /api/v1/auth/register
      - /public/**
```

#### 5.3 UserContextInterceptor (Spring MVC)

When Spring MVC is on the classpath, `UserContextInterceptor` is auto-registered. It provides method-level auth control.

`preHandle` logic:

1. Handler has `@AnonymousAccess` → skip
2. Path matches `exclude-paths` → skip
3. `UserContextHolder.getUserId() == null` → returns **401 JSON** and blocks the request
4. Otherwise → allows through

`afterCompletion` → clears `UserContextHolder`

#### 5.4 `@AnonymousAccess` Annotation

Mark public endpoints that do **not** require authentication:

```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @AnonymousAccess
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        // No JWT required
    }

    @GetMapping("/profile")  // ← requires a valid JWT
    public Result<UserVO> profile() {
        String userId = UserContextHolder.getUserId();
        return Result.success(userService.getById(userId));
    }
}
```

`@AnonymousAccess` can also be placed on the class level — then all methods in that class are public.

### 6. User Context — `UserContextHolder`

ThreadLocal-based holder for the current user, available throughout the request lifecycle:

```java
// Get current user ID (auto-populated by JwtAuthenticationFilter)
String userId = UserContextHolder.getUserId();

// Access JWT claims
String role = UserContextHolder.getClaim("role");
Map<String, Object> allClaims = UserContextHolder.getClaims();

// Manual usage (non-JWT scenarios)
UserContextHolder.setUser(user);
UserContextHolder.setUserId("12345");
```

> Context is cleared after every request. `UserContextClearFilter` + `UserContextInterceptor` provide double safety.

### 7. Password Utilities — `PasswordUtils`

```java
// BCrypt hashing
String encoded = PasswordUtils.encode("123456");          // $2a$10$...
boolean match = PasswordUtils.matches("123456", encoded); // true

// AES symmetric encryption (e.g. phone numbers, sensitive fields)
String encrypted = PasswordUtils.encryptAes("13800138000", "my-16byte-key!!");    // min 16 bytes
String decrypted = PasswordUtils.decryptAes(encrypted, "my-16byte-key!!");        // "13800138000"
```

> Output is Base64-encoded with random IV prepended. Key length is automatically adjusted for AES-128/192/256.

### 8. Object Storage (OSS)

> Enabling OSS requires the corresponding SDK dependency on your classpath.

```yaml
ikeu:
  oss:
    enabled: true
    type: aliyun
    endpoint: "https://oss-cn-hangzhou.aliyuncs.com"
    access-key: "${OSS_ACCESS_KEY}"    # Use env vars for credentials
    secret-key: "${OSS_SECRET_KEY}"
    bucket: "my-bucket"
    cdn-domain: "cdn.example.com"
```

```java
@Autowired
private OssTemplate ossTemplate;

// Upload (default bucket or explicit bucket)
ossTemplate.upload("avatar/001.jpg", inputStream, "image/jpeg");
ossTemplate.upload("my-bucket", "avatar/001.jpg", inputStream, "image/jpeg");

// Download
InputStream is = ossTemplate.download("avatar/001.jpg");

// Pre-signed URL (default 1-hour expiration, customizable)
String signedUrl = ossTemplate.getUrl("avatar/001.jpg", Duration.ofMinutes(30));

// Public CDN URL (uses cdn-domain if configured, falls back to OSS domain)
String publicUrl = ossTemplate.getPublicUrl("avatar/001.jpg");

// Delete
ossTemplate.delete("avatar/001.jpg");
ossTemplate.batchDelete(List.of("a.txt", "b.txt"));

// Check existence
boolean exists = ossTemplate.exist("avatar/001.jpg");
```

### 9. JSON Auto-Configuration — `JacksonCustomAutoConfiguration`

The starter auto-configures Jackson globally via two mechanisms:

1. **`Jackson2ObjectMapperBuilderCustomizer`** — hooks into Spring Boot's ObjectMapper creation pipeline, applying date format, Long→String, null exclusion, Java 8 time support
2. **`WebMvcConfigurer`** — replaces ObjectMapper in all `MappingJackson2HttpMessageConverter` instances (automated `extendMessageConverters`)

All configurable via `application.yml`:

```yaml
ikeu:
  jackson:
    date-pattern: "yyyy-MM-dd HH:mm:ss"
    long-as-string: true   # Long → String in JSON (JS precision > 2^53)
    serialization-inclusion: non_null
    time-zone: Asia/Shanghai
```

**Compatibility**: if you define your own `ObjectMapper` bean or `Jackson2ObjectMapperBuilderCustomizer`, they combine additively. The configured mapper is synced to `JsonUtils` automatically via `@EventListener(ApplicationReadyEvent)`.

### 10. JSON Utilities — `JsonUtils`

```java
// Serialize
String json = JsonUtils.toJson(obj);
String pretty = JsonUtils.toJsonPretty(obj);    // indented
byte[] bytes = JsonUtils.toJsonBytes(obj);

// Deserialize
User user = JsonUtils.fromJson(json, User.class);
List<User> list = JsonUtils.fromJsonList(jsonArr, User.class);
Map<String, Object> map = JsonUtils.fromJsonMap(json);

// DTO conversion (JSON round-trip)
UserVo vo = JsonUtils.convert(entity, UserVo.class);

// Validation
boolean valid = JsonUtils.isValidJson(input);
```

Pre-configured: `NON_NULL` inclusion, unknown-property tolerance, Java 8 time with custom formatters, `Long`→`String` for JS safety, date format `yyyy-MM-dd HH:mm:ss`.

### 11. Date Utilities — `DateUtils`

Thread-safe, pure `java.time` API. Say goodbye to `SimpleDateFormat`.

```java
// Format / parse (default patterns available)
String s = DateUtils.format(LocalDateTime.now());        // "2026-05-25 15:30:00"
String d = DateUtils.formatDate(LocalDate.now());        // "2026-05-25"
LocalDateTime dt = DateUtils.parse("2026-05-25 15:30:00");

// Day / month boundaries
LocalDateTime start = DateUtils.startOfDay(LocalDate.now());  // 00:00:00
LocalDateTime end = DateUtils.endOfDay(LocalDate.now());      // 23:59:59
LocalDateTime monthStart = DateUtils.startOfMonth(dt);
LocalDateTime monthEnd = DateUtils.endOfMonth(dt);

// Arithmetic (negative = subtract)
LocalDateTime future = DateUtils.addDays(dt, 7);
LocalDateTime nextMonth = DateUtils.addMonths(dt, 1);
long days = DateUtils.daysBetween(start, end);
long hours = DateUtils.hoursBetween(start, end);

// Overlap / range check
boolean overlap = DateUtils.isOverlap(aStart, aEnd, bStart, bEnd);
boolean between = DateUtils.isBetween(target, start, end);

// Epoch conversion (system default timezone)
long millis = DateUtils.toEpochMilli(dt);
LocalDateTime dt = DateUtils.fromEpochMilli(1700000000000L);

// Legacy Date conversion
Date legacy = DateUtils.toDate(dt);
LocalDateTime modern = DateUtils.toLocalDateTime(new Date());

// Format conversion
DateUtils.convertFormat("2026-05-25", PATTERN_DATE, PATTERN_DATE_COMPACT); // "20260525"
```

### 12. HTTP Client — `HttpClientUtil`

Static HTTP client using Java 11+ `java.net.http.HttpClient`. HTTP/2, sync/async, JSON auto-serialization.

```java
// GET
String html = HttpClientUtil.doGet("https://api.example.com/users");
String result = HttpClientUtil.doGet(url, Map.of("page", "1"), null);
List<UserVo> users = HttpClientUtil.doGet(url, params, headers, UserVo.class);

// POST JSON (body auto-serialized via JsonUtils)
String resp = HttpClientUtil.doPost(url, new CreateUserReq("John"));
UserVo user = HttpClientUtil.doPost(url, body, headers, UserVo.class);

// POST form-encoded
String resp = HttpClientUtil.doPostForm(url, Map.of("user", "john"), headers);

// PUT / DELETE
HttpClientUtil.doPut(url, updateBody, headers);
HttpClientUtil.doDelete(url, headers);

// Async
CompletableFuture<String> f = HttpClientUtil.doGetAsync(url, null, null);
f.thenAccept(System.out::println);

// Download
HttpClientUtil.download("https://cdn.example.com/file.pdf", Path.of("/tmp/file.pdf"));
```

Configurable via `application.yml` (see `ikeu.http-client` above) or programmatically:

```java
HttpClientUtil.configure(customClient, Duration.ofSeconds(5), null, null);
```

### 13. String Utilities — `StringUtils`

```java
String s = StringUtils.camelToUnderline("userName");     // → user_name
String s = StringUtils.underlineToCamel("user_name");    // → userName
String uuid = StringUtils.uuid32();                      // UUID without dashes
String code = StringUtils.randomNumeric(6);               // 6-digit random number
String s = StringUtils.randomAlphanumeric(8);             // 8-char alphanumeric
boolean v = StringUtils.isMobile("13800138000");
boolean v = StringUtils.isEmail("test@example.com");
```

### 14. Tree Utilities — `TreeUtils`

Your entity must implement `TreeNode<ID>`:

```java
// Build tree from flat list (rootParentId is the parentId value for top-level nodes)
List<MenuTreeNode> tree = TreeUtils.buildTree(menuList, 0L);

// Extract all leaf node IDs
Set<Long> leafIds = TreeUtils.extractLeafIds(tree);

// Sort recursively
TreeUtils.sortTree(tree, Comparator.comparing(MenuTreeNode::getSort));
```

### 15. Snowflake ID Generator

```java
// workerId + datacenterId (each 0–31)
SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);
long id = generator.nextId();  // thread-safe, blocks on clock drift
```

### 16. Spring Context Holder — `SpringContextHolder`

```java
UserService service = SpringContextHolder.getBean(UserService.class);
Object bean = SpringContextHolder.getBean("beanName");
String val = SpringContextHolder.getProperty("server.port");
String val = SpringContextHolder.getProperty("custom.key", "defaultValue");
```

---

## Overriding Auto-Configured Beans

Every auto-configured bean uses `@ConditionalOnMissingBean`, so you can replace any of them:

```java
@Configuration
public class MySecurityConfig {

    /** Custom JwtUtils — e.g. switch to RS256 */
    @Bean
    public JwtUtils jwtUtils(JwtProperties props) {
        return new JwtUtils(props.getSecret(), props.getExpiration());
    }

    /** Replace the JWT filter entirely */
    @Bean("jwtFilterRegistration")
    public FilterRegistrationBean<Filter> myJwtFilter(JwtUtils jwtUtils, JwtProperties props) {
        // Your custom filter
    }
}
```

---

## Authentication Flow

```
Request
  │
  ├─ UserContextClearFilter (order=MAX) — outermost, clears context in finally
  │    │
  │    ├─ JwtAuthenticationFilter (order=-100) — extracts JWT → populates UserContextHolder
  │    │
  │    ├─ UserContextInterceptor.preHandle — checks @AnonymousAccess / excludePaths / userId
  │    │    └─ not authenticated → 401 JSON
  │    │
  │    ├─ Controller
  │    │
  │    └─ UserContextInterceptor.afterCompletion — clears context
  │
  └─ finally: UserContextHolder.clear()
```

---

## Requirements

- Java 17+
- Spring Boot 3.2.x
- Lombok (compile-time)

## License

Apache-2.0
