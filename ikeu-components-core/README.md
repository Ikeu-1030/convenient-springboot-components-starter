[English](README.en.md) | 中文

# ikeu-components-core

纯 Java 工具集，零 Spring 依赖。可直接用于任何 Java 17+ 项目。

## 工具清单

### BeanCopyUtils — 属性拷贝

跳过 null 值，最大递归深度 3，IdentityHashMap 防循环引用。

```java
BeanCopyUtils.copyProperties(source, target);
BeanCopyUtils.copyProperties(source, target, 5);  // 自定义深度
List<UserVO> list = BeanCopyUtils.copyList(users, UserVO.class);
```

### BeanConverter — 类型转换 + 多源合并

Entity / DTO / VO 互转，支持自定义转换器注册和 MyBatis-Plus Page 转换。

```java
UserVO vo = BeanConverter.convert(user, UserVO.class);
List<UserVO> list = BeanConverter.convertList(users, UserVO.class);

// 多源合并（后面覆盖前面同名属性）
UserVO vo = BeanConverter.combine(UserVO.class, user, userExt, session);

// 自定义转换器
BeanConverter.register(User.class, UserVO.class, (src, tgt) -> {
    tgt.setDisplayName(src.getNickname() + " (" + src.getUsername() + ")");
    BeanCopyUtils.copyProperties(src, tgt);
    return tgt;
});
```

### DateUtils — 日期时间（java.time）

线程安全，纯 `java.time` API。格式化/解析、日/月起止、加减、区间判断、epoch/Date 互转。

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

**常量：** `PATTERN_DATE` / `PATTERN_DATETIME` / `PATTERN_DATETIME_MS` / `PATTERN_DATE_COMPACT`

### JsonUtils — JSON 序列化

基于 Jackson，预配置 NON_NULL、Java 8 时间、Long→String、未知属性容错。

```java
String json = JsonUtils.toJson(obj);
User user = JsonUtils.fromJson(json, User.class);
List<User> list = JsonUtils.fromJsonList(jsonArr, User.class);
Map<String, Object> map = JsonUtils.fromJsonMap(json);
UserVO vo = JsonUtils.convert(entity, UserVO.class);  // JSON 往返转换
boolean valid = JsonUtils.isValidJson(input);
```

> 使用 `ikeu-components-spring-boot-starter` 时，`JsonUtils` 的 ObjectMapper 会和 Spring Boot 自动配置保持同步。

### StringUtils — 字符串工具

```java
StringUtils.camelToUnderline("userName");     // user_name
StringUtils.underlineToCamel("user_name");    // userName
StringUtils.uuid();                            // 无横线 UUID
StringUtils.randomNumeric(6);                   // 6 位随机数字
StringUtils.randomAlphanumeric(8);             // 8 位字母数字
StringUtils.isMobile("13800138000");           // true
StringUtils.isEmail("test@example.com");       // true
StringUtils.isIdCard("110101199001011234");    // true
```

### HttpClientUtil — HTTP 客户端

基于 Java 11+ `java.net.http.HttpClient`，HTTP/2、同步/异步、JSON 自动序列化、文件下载。

```java
String html = HttpClientUtil.doGet("https://api.example.com/users");
UserVo user = HttpClientUtil.doGet(url, params, headers, UserVo.class);
String resp = HttpClientUtil.doPost(url, body, headers);
HttpClientUtil.doPostForm(url, Map.of("user", "john"), headers);
HttpClientUtil.doPut(url, updateBody, headers);
HttpClientUtil.doDelete(url, headers);

// 异步
CompletableFuture<String> f = HttpClientUtil.doGetAsync(url, null, null);

// 下载
HttpClientUtil.download(url, Path.of("/tmp/file.pdf"));
```

> 超时和代理可通过 `application.yml` 的 `ikeu.http-client.*` 或 `HttpClientUtil.configure()` 配置。

### TreeUtils — 树形结构

实体实现 `TreeNode<ID>` 接口即可。

```java
List<MenuTreeNode> tree = TreeUtils.buildTree(menuList, 0L);
Set<Long> leafIds = TreeUtils.extractLeafIds(tree);
TreeUtils.sortTree(tree, Comparator.comparing(MenuTreeNode::getSort));
```

### SnowflakeIdGenerator — 雪花 ID

```java
SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);  // workerId, datacenterId
long id = generator.nextId();  // 线程安全，时钟回拨阻塞等待
```

### @Sensitive — 敏感数据脱敏

Jackson 序列化时自动掩码。支持 8 种预置策略 + 自定义。

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

| 类型 | 效果 |
|------|------|
| `CHINESE_NAME` | 张** |
| `ID_CARD` | 110***********1234 |
| `PHONE` | 138****5678 |
| `EMAIL` | t***@example.com |
| `BANK_CARD` | 6222********1234 |
| `ADDRESS` | 北京市海淀区****** |
| `PASSWORD` | ******** |
| `CUSTOM` | 由 startInclude/endInclude/maskChar 控制 |

> `@Sensitive` 通过 Jackson 全局注册生效，未注解字段不受影响。
