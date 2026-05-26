[English](README.en.md) | 中文

# ikeu-components-web

统一 Web 响应、异常处理、断言工具。依赖 `ikeu-components-core`，Spring Web 为 `provided`。

## Result\<T\> — 统一 API 响应

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

| 静态方法 | 说明 |
|---------|------|
| `Result.success(data)` | 成功，code=200 |
| `Result.success(msg, data)` | 成功带消息 |
| `Result.error(code, msg)` | 业务错误 |
| `Result.error(msg)` | 业务错误（code=500） |
| `Result.of(data)` | 自动判断：非 null→成功，null→error |
| `result.isSuccess()` | 判断是否成功 |

响应格式：`{"code": 200, "message": "success", "data": {...}}`

## PageResult\<T\> — 分页响应

```java
@GetMapping
public Result<PageResult<UserVO>> list(UserPageQuery query) {
    IPage<User> page = userService.page(query);
    List<UserVO> vos = BeanConverter.convertList(page.getRecords(), UserVO.class);
    return Result.success(PageResult.of(page, vos));
}
```

响应：`{"code":200, "data":{"total":150, "current":1, "pages":15, "records":[...]}}`

## BusinessException — 业务异常

```java
throw new BusinessException(404, "用户不存在");
throw new BusinessException("操作失败");  // code 默认 500
```

## AssertUtils — 参数断言

```java
AssertUtils.notNull(user, "用户不存在");
AssertUtils.isTrue(balance >= amount, "余额不足");
AssertUtils.notBlank(username, "用户名不能为空");
AssertUtils.notEmpty(list, "列表不能为空");
```

全部断言失败时抛出 `BusinessException`。

## GlobalExceptionHandler — 全局异常处理

自动导入，无需手动配置。覆盖：

| 异常类型 | HTTP 状态 | 行为 |
|---------|----------|------|
| `BusinessException` | 200 | 使用异常的 code 和 message |
| `MethodArgumentNotValidException` | 400 | 提取字段校验错误，按字段分组 |
| `IllegalArgumentException` | 400 | 返回异常消息 |
| `Exception`（兜底） | 500 | 打印堆栈，返回 "Internal Server Error" |

## SpringContextHolder — 获取 Bean / 配置

```java
UserService service = SpringContextHolder.getBean(UserService.class);
String port = SpringContextHolder.getProperty("server.port");
String custom = SpringContextHolder.getProperty("app.key", "default");
```

> **注意：** `SpringContextHolder` 在非 Web 环境（如单元测试未启动 Spring 容器）中不可用。
