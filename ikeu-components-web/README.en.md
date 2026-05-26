English | [中文](README.md)

# ikeu-components-web

Unified web response, exception handling, assertion utilities. Depends on `ikeu-components-core`. Spring Web is `provided` scope.

## Result\<T\> — Unified API Response

```java
@GetMapping("/{id}")
public Result<UserVO> get(@PathVariable Long id) {
    return Result.success(userService.getById(id));
}

@PostMapping
public Result<Void> create(@Valid @RequestBody UserDTO dto) {
    userService.create(dto);
    return Result.success();
}
```

| Static Method | Description |
|---------------|-------------|
| `Result.success(data)` | Success, code=200 |
| `Result.success(msg, data)` | Success with message |
| `Result.error(code, msg)` | Business error |
| `Result.error(msg)` | Business error (code=500) |
| `Result.of(data)` | Auto-detect: non-null→success, null→error |
| `result.isSuccess()` | Check success status |

Response format: `{"code": 200, "message": "success", "data": {...}}`

## PageResult\<T\> — Paging Response

```java
@GetMapping
public Result<PageResult<UserVO>> list(UserPageQuery query) {
    IPage<User> page = userService.page(query);
    List<UserVO> vos = BeanConverter.convertList(page.getRecords(), UserVO.class);
    return Result.success(PageResult.of(page, vos));
}
```

Response: `{"code":200, "data":{"total":150, "current":1, "pages":15, "records":[...]}}`

## BusinessException — Business Exception

```java
throw new BusinessException(404, "User not found");
throw new BusinessException("Operation failed");  // code defaults to 500
```

## AssertUtils — Parameter Assertions

```java
AssertUtils.notNull(user, "User not found");
AssertUtils.isTrue(balance >= amount, "Insufficient balance");
AssertUtils.notBlank(username, "Username is required");
AssertUtils.notEmpty(list, "List must not be empty");
```

All assertions throw `BusinessException` on failure.

## GlobalExceptionHandler — Global Exception Handler

Auto-imported, no manual configuration needed. Covers:

| Exception Type | HTTP Status | Behavior |
|---------------|-------------|----------|
| `BusinessException` | 200 | Uses exception's code and message |
| `MethodArgumentNotValidException` | 400 | Extracts field errors grouped by field |
| `IllegalArgumentException` | 400 | Returns exception message |
| `Exception` (catch-all) | 500 | Logs stack trace, returns "Internal Server Error" |

## SpringContextHolder — Bean/Config Access

```java
UserService service = SpringContextHolder.getBean(UserService.class);
String port = SpringContextHolder.getProperty("server.port");
String custom = SpringContextHolder.getProperty("app.key", "default");
```

> **Note:** `SpringContextHolder` is unavailable in non-Spring environments (e.g., unit tests without a running container).
