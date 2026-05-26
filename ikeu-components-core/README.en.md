English | [中文](README.md)

# ikeu-components-core

Pure Java utilities with zero Spring dependencies. Can be used in any Java 17+ project.

## Utilities

### BeanCopyUtils — Property Copying

Skips null values, max recursion depth 3, IdentityHashMap cycle detection.

```java
BeanCopyUtils.copyProperties(source, target);
BeanCopyUtils.copyProperties(source, target, 5);  // custom depth
List<UserVO> list = BeanCopyUtils.copyList(users, UserVO.class);
```

### BeanConverter — Type Conversion & Multi-Source Merge

Entity / DTO / VO conversion with custom converter registration and MyBatis-Plus Page support.

```java
UserVO vo = BeanConverter.convert(user, UserVO.class);
List<UserVO> list = BeanConverter.convertList(users, UserVO.class);

// Multi-source merge (later sources override earlier ones)
UserVO vo = BeanConverter.combine(UserVO.class, user, userExt, session);

// Custom converter
BeanConverter.register(User.class, UserVO.class, (src, tgt) -> {
    tgt.setDisplayName(src.getNickname() + " (" + src.getUsername() + ")");
    BeanCopyUtils.copyProperties(src, tgt);
    return tgt;
});
```

### DateUtils — Date & Time (java.time)

Thread-safe, pure `java.time` API. Format/parse, day/month boundaries, arithmetic, overlap check, epoch/Date conversion.

```java
DateUtils.format(LocalDateTime.now());              // "2026-05-26 15:30:00"
DateUtils.parse("2026-05-26 15:30:00");
DateUtils.startOfDay(LocalDate.now());              // 00:00:00
DateUtils.endOfMonth(dt);
DateUtils.addDays(dt, 7);
DateUtils.daysBetween(start, end);
DateUtils.isOverlap(aStart, aEnd, bStart, bEnd);
DateUtils.convertFormat("2026-05-26", PATTERN_DATE, PATTERN_DATE_COMPACT); // "20260526"
```

**Constants:** `PATTERN_DATE` / `PATTERN_DATETIME` / `PATTERN_DATETIME_MS` / `PATTERN_DATE_COMPACT`

### JsonUtils — JSON Serialization

Jackson-based, pre-configured with NON_NULL, Java 8 time, Long→String, unknown-property tolerance.

```java
String json = JsonUtils.toJson(obj);
User user = JsonUtils.fromJson(json, User.class);
List<User> list = JsonUtils.fromJsonList(jsonArr, User.class);
Map<String, Object> map = JsonUtils.fromJsonMap(json);
UserVO vo = JsonUtils.convert(entity, UserVO.class);  // JSON round-trip conversion
boolean valid = JsonUtils.isValidJson(input);
```

> When using `ikeu-components-spring-boot-starter`, `JsonUtils`'s ObjectMapper stays in sync with Spring Boot auto-configuration.

### StringUtils

```java
StringUtils.camelToUnderline("userName");     // user_name
StringUtils.underlineToCamel("user_name");    // userName
StringUtils.uuid();                            // UUID without dashes
StringUtils.randomNumeric(6);                   // 6-digit random number
StringUtils.randomAlphanumeric(8);             // 8-char alphanumeric
StringUtils.isMobile("13800138000");           // true
StringUtils.isEmail("test@example.com");       // true
StringUtils.isIdCard("110101199001011234");    // true
```

### HttpClientUtil — HTTP Client

Java 11+ `java.net.http.HttpClient`, HTTP/2, sync/async, JSON auto-serialization, file download.

```java
String html = HttpClientUtil.doGet("https://api.example.com/users");
UserVo user = HttpClientUtil.doGet(url, params, headers, UserVo.class);
String resp = HttpClientUtil.doPost(url, body, headers);
HttpClientUtil.doPostForm(url, Map.of("user", "john"), headers);
HttpClientUtil.doPut(url, updateBody, headers);
HttpClientUtil.doDelete(url, headers);

// Async
CompletableFuture<String> f = HttpClientUtil.doGetAsync(url, null, null);

// Download
HttpClientUtil.download(url, Path.of("/tmp/file.pdf"));
```

> Configure timeouts and proxy via `ikeu.http-client.*` in `application.yml` or `HttpClientUtil.configure()`.

### TreeUtils — Tree Structures

Entities implement the `TreeNode<ID>` interface.

```java
List<MenuTreeNode> tree = TreeUtils.buildTree(menuList, 0L);
Set<Long> leafIds = TreeUtils.extractLeafIds(tree);
TreeUtils.sortTree(tree, Comparator.comparing(MenuTreeNode::getSort));
```

### SnowflakeIdGenerator — Distributed ID

```java
SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);  // workerId, datacenterId
long id = generator.nextId();  // thread-safe, blocks on clock drift
```

### @Sensitive — Data Masking

Auto-masks String fields during Jackson serialization. 8 built-in strategies + custom.

```java
public class UserVO {
    @Sensitive(SensitiveType.PHONE)
    private String phone;          // 138****5678

    @Sensitive(SensitiveType.ID_CARD)
    private String idCard;         // 110***********1234

    @Sensitive(value = SensitiveType.CUSTOM, startInclude = 2, endInclude = 2)
    private String custom;         // AB****GH
}
```

| Type | Result |
|------|--------|
| `CHINESE_NAME` | 张** |
| `ID_CARD` | 110***********1234 |
| `PHONE` | 138****5678 |
| `EMAIL` | t***@example.com |
| `BANK_CARD` | 6222********1234 |
| `ADDRESS` | 北京市海淀区****** |
| `PASSWORD` | ******** |
| `CUSTOM` | Controlled by startInclude/endInclude/maskChar |

> `@Sensitive` is globally registered in Jackson. Unannotated fields are unaffected.
