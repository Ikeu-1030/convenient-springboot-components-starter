English | [中文](README.md)

# ikeu-components-redis

Redis utilities, distributed lock, cache penetration/breakdown protection. Depends on `ikeu-components-core`.

## Prerequisites

Redis dependencies are **not transitive**. Add explicitly:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

## Configuration

```yaml
ikeu:
  redis:
    enabled: true
    lock-prefix: "ikeu:lock:"     # lock key prefix
    use-json-serialization: true  # use JSON serialization for RedisTemplate
```

## RedisUtils — Cache Operations

Convenience wrapper around `RedisTemplate<String, Object>` with automatic JSON serialization/deserialization.

### Basic Get/Set

```java
@Autowired private RedisUtils redisUtils;

redisUtils.set("user:1", user);
redisUtils.set("user:1", user, Duration.ofHours(1));
redisUtils.set("user:1", user, 3600);  // seconds

User user = redisUtils.get("user:1", User.class);
```

### Collections

```java
redisUtils.setList("users", userList, Duration.ofMinutes(10));
List<User> users = redisUtils.getList("users", User.class);

redisUtils.setMap("config", Map.of("key", "val"));
Map<String, Object> config = redisUtils.getMap("config");

redisUtils.setSet("tags", tags, Duration.ofMinutes(5));
Set<String> tags = redisUtils.getSet("tags", String.class);
```

### Key Management

```java
boolean exists = redisUtils.hasKey("user:1");

redisUtils.expire("user:1", Duration.ofMinutes(30));
long ttl = redisUtils.getExpire("user:1");  // -1=persistent, -2=not found

redisUtils.delete("user:1");
redisUtils.delete(List.of("k1", "k2", "k3"));  // returns count deleted

boolean ok = redisUtils.setIfAbsent("key", value, Duration.ofMinutes(1));
```

### Atomic Operations

```java
redisUtils.incr("counter");       // +1
redisUtils.incrBy("counter", 5);  // +5
redisUtils.decr("counter");       // -1
redisUtils.decrBy("counter", 3);  // -3
```

### Hash Operations

```java
redisUtils.hSet("user:1", "name", "John");
redisUtils.hSetAll("user:1", Map.of("name", "John", "age", 30));

String name = redisUtils.hGet("user:1", "name", String.class);
Map<String, Object> all = redisUtils.hGetAll("user:1");

boolean has = redisUtils.hHasKey("user:1", "name");
redisUtils.hDelete("user:1", "name", "age");
```

## RedisDistributedLock — Distributed Lock

`SET NX EX` for atomic acquisition + Lua script for atomic release.

```java
@Autowired private RedisDistributedLock lock;

String lockValue = UUID.randomUUID().toString();
if (lock.tryLock("order:123", lockValue, Duration.ofSeconds(30))) {
    try {
        // critical section
    } finally {
        lock.unlock("order:123", lockValue);
    }
}

// Blocking lock (polls every 100ms)
lock.lock("order:123", lockValue, Duration.ofSeconds(30));
```

> **Key:** `unlock` uses Lua script to verify value match before deleting — prevents releasing another thread's lock.

## RedisLockHelper — Lock + Cache Protection

High-level helper combining `RedisDistributedLock` + `RedisUtils`.

### Functional Auto-Lock

```java
@Autowired private RedisLockHelper lockHelper;

// Auto acquire/release, exception-safe
String result = lockHelper.execute("order:123", Duration.ofSeconds(30),
        () -> doCriticalWork());
```

### Cache Penetration Protection

Caches a short-TTL null marker when DB returns null, preventing mass penetration.

```java
User user = lockHelper.getWithPenetrationProtection(
    "user:1", User.class,
    () -> userMapper.selectById(1L),   // DB query
    Duration.ofMinutes(10),             // cache TTL
    Duration.ofSeconds(30)              // null-value TTL
);
```

### Cache Breakdown Protection

Hot-key mutex lock ensures only one thread rebuilds the cache on expiry.

```java
User user = lockHelper.getWithBreakdownProtection(
    "hot:item:1", User.class,
    () -> userMapper.selectById(1L),
    Duration.ofMinutes(10)
);
```

Internal flow:
1. Check cache → hit returns
2. Miss → try mutex lock `mutex:<key>`
3. Lock acquired → double-check → query DB → write cache → release lock
4. Lock not acquired → spin-wait (default 5s) → re-check cache → timeout falls back to DB

## Notes

- `RedisUtils.getList/getSet` element conversion uses Jackson `ObjectMapper.convertValue`; for complex nested objects, prefer `JsonUtils.convert`
- `RedisDistributedLock` lockValue must be unique per attempt (use `UUID.randomUUID()`) or unlock may delete another's lock
- Breakdown protection's spin-timeout fallback queries DB directly; increase `maxWait` for high-concurrency scenarios
