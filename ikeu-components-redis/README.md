[English](README.en.md) | 中文

# ikeu-components-redis

Redis 工具类、分布式锁、缓存穿透/击穿防护。依赖 `ikeu-components-core`。

## 前置条件

Redis 依赖**不传递**。使用前需添加：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

## 配置

```yaml
ikeu:
  redis:
    enabled: true
    lock-prefix: "ikeu:lock:"     # 锁 key 前缀
    use-json-serialization: true  # RedisTemplate 使用 JSON 序列化
```

## RedisUtils — 缓存操作

对 `RedisTemplate<String, Object>` 的便捷封装，自动 JSON 序列化/反序列化。

### 基础存取

```java
@Autowired private RedisUtils redisUtils;

redisUtils.set("user:1", user);
redisUtils.set("user:1", user, Duration.ofHours(1));
redisUtils.set("user:1", user, 3600);  // 秒

User user = redisUtils.get("user:1", User.class);
```

### 集合操作

```java
redisUtils.setList("users", userList, Duration.ofMinutes(10));
List<User> users = redisUtils.getList("users", User.class);

redisUtils.setMap("config", Map.of("key", "val"));
Map<String, Object> config = redisUtils.getMap("config");

redisUtils.setSet("tags", tags, Duration.ofMinutes(5));
Set<String> tags = redisUtils.getSet("tags", String.class);
```

### 键管理

```java
boolean exists = redisUtils.hasKey("user:1");

redisUtils.expire("user:1", Duration.ofMinutes(30));
long ttl = redisUtils.getExpire("user:1");  // -1=永不过期, -2=不存在

redisUtils.delete("user:1");
redisUtils.delete(List.of("k1", "k2", "k3"));  // 返回删除数

boolean ok = redisUtils.setIfAbsent("key", value, Duration.ofMinutes(1));
```

### 原子操作

```java
redisUtils.incr("counter");       // +1
redisUtils.incrBy("counter", 5);  // +5
redisUtils.decr("counter");       // -1
redisUtils.decrBy("counter", 3);  // -3
```

### 哈希操作

```java
redisUtils.hSet("user:1", "name", "John");
redisUtils.hSetAll("user:1", Map.of("name", "John", "age", 30));

String name = redisUtils.hGet("user:1", "name", String.class);
Map<String, Object> all = redisUtils.hGetAll("user:1");

boolean has = redisUtils.hHasKey("user:1", "name");
redisUtils.hDelete("user:1", "name", "age");
```

## RedisDistributedLock — 分布式锁

基于 `SET NX EX` 原子加锁 + Lua 脚本原子释放。

```java
@Autowired private RedisDistributedLock lock;

String lockValue = UUID.randomUUID().toString();
if (lock.tryLock("order:123", lockValue, Duration.ofSeconds(30))) {
    try {
        // 临界区
    } finally {
        lock.unlock("order:123", lockValue);
    }
}

// 阻塞式加锁（每 100ms 轮询）
lock.lock("order:123", lockValue, Duration.ofSeconds(30));
```

> **关键：** `unlock` 通过 Lua 脚本校验 value 匹配才删除，防止释放他人持有的锁。

## RedisLockHelper — 锁 + 缓存防护

组合 `RedisDistributedLock` + `RedisUtils` 的高层封装。

### 函数式自动加锁

```java
@Autowired private RedisLockHelper lockHelper;

// 自动获取/释放锁，异常安全
String result = lockHelper.execute("order:123", Duration.ofSeconds(30),
        () -> doCriticalWork());
```

### 缓存穿透防护

DB 返回 null 时缓存短 TTL 标记值，防止大量请求穿透到 DB。

```java
User user = lockHelper.getWithPenetrationProtection(
    "user:1", User.class,
    () -> userMapper.selectById(1L),   // DB 查询
    Duration.ofMinutes(10),             // 正常缓存 TTL
    Duration.ofSeconds(30)              // null 值 TTL
);
```

### 缓存击穿防护

热点 key 过期时，用分布式互斥锁保证只有一个线程重建缓存。

```java
User user = lockHelper.getWithBreakdownProtection(
    "hot:item:1", User.class,
    () -> userMapper.selectById(1L),
    Duration.ofMinutes(10)
);
```

内部流程：
1. 查缓存 → 命中返回
2. 未命中 → 尝试获取互斥锁 `mutex:<key>`
3. 获取锁 → 双重检查 → 查 DB → 写缓存 → 释放锁
4. 未获取锁 → 自旋等待（默认 5s）→ 重新查缓存 → 超时则直接查 DB

## 注意事项

- `RedisUtils.getList/getSet` 的元素类型转换依赖 Jackson `ObjectMapper.convertValue`，复杂嵌套对象建议用 `JsonUtils.convert`
- `RedisDistributedLock` 的 `lockValue` 必须每次使用不同值（推荐 `UUID.randomUUID().toString()`），否则 unlock 可能误删
- 缓存击穿防护的 fallback（自旋超时）会直接查 DB，高并发时建议调大 `maxWait`
