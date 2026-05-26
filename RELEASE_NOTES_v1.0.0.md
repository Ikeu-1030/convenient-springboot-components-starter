# ikeu-components v1.0.0 Release Notes

> 🎉 **Initial Release** — Spring Boot 3.2+ Convenience Toolkit

一个专为 Spring Boot 3.2+ 设计的通用工具包，消除日常开发中的样板代码，提供统一 API 响应、JWT 认证、对象存储抽象和常用工具集。

---

## 📦 模块一览

| 模块 | ArtifactId | 说明 |
|------|-----------|------|
| Parent | `ikeu-components-parent` | 父 POM，管理依赖版本 |
| Core | `ikeu-components-core` | 纯 Java 工具（零 Spring 依赖） |
| Web | `ikeu-components-web` | 统一响应、分页、异常、断言、SpringContext |
| Security | `ikeu-components-security` | JWT、密码加密、用户上下文、认证过滤器 |
| OSS | `ikeu-components-oss` | 对象存储抽象（阿里云 OSS / MinIO） |
| Autoconfigure | `ikeu-components-spring-boot-autoconfigure` | 全部自动配置 & `@ConfigurationProperties` |
| Starter | `ikeu-components-spring-boot-starter` | 空 Starter，聚合上述全部模块 |

只需要引入一个依赖：

```xml
<dependency>
    <groupId>com.ikeu.components</groupId>
    <artifactId>ikeu-components-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## ✨ 功能清单

### Core 模块（`ikeu-components-core`）

- **BeanCopyUtils** — 属性拷贝（跳过 null、深拷贝列表、合并非 null 属性），最大递归深度 3 防止死循环
- **BeanConverter** — Entity/DTO/VO 类型转换（`convert` / `convertList` / `convertPage`），多源合并 `combine()`，支持自定义转换器注册
- **DateUtils** — 纯 `java.time` API，线程安全：格式化/解析、日/月起止边界、加减运算、区间重叠判断、epoch 互转、旧 `Date` 互转
- **HttpClientUtil** — 基于 Java 11+ `java.net.http.HttpClient` 的静态 HTTP 客户端，支持 GET/POST/PUT/DELETE、同步/异步、JSON 自动序列化、文件下载、可配置超时与代理
- **JsonUtils** — Jackson `ObjectMapper` 封装：`toJson` / `fromJson` / `fromJsonList` / `fromJsonMap` / `convert` / `isValidJson`，预配置 NON_NULL、Java 8 时间、Long→String
- **SnowflakeIdGenerator** — 雪花 ID 生成器，可配置 workerId/datacenterId（0-31），时钟回拨时阻塞等待
- **StringUtils** — 驼峰/下划线互转、UUID32、随机数字/字母数字串、手机号/邮箱验证
- **TreeUtils** — 扁平列表→树形结构、提取叶子节点 ID、递归排序
- **TreeNode** — 树节点接口

### Web 模块（`ikeu-components-web`）

- **Result\<T\>** — 统一 API 响应（`code` / `message` / `data`），静态工厂：`success` / `error` / `of`
- **PageResult\<T\>** — 分页响应（`total` / `current` / `pages` / `records`），支持 MyBatis-Plus `Page` 转换
- **BusinessException** — 业务异常（带错误码）
- **AssertUtils** — 断言工具（`notNull` / `isTrue` / `notBlank` 等），抛出 `BusinessException`
- **GlobalExceptionHandler** — `@RestControllerAdvice` 全局异常处理，覆盖 `BusinessException` / `MethodArgumentNotValidException` / `IllegalArgumentException` / `Exception`
- **SpringContextHolder** — Spring 上下文持有者（获取 Bean、读取配置属性）

### Security 模块（`ikeu-components-security`）

- **JwtUtils** — 基于 jjwt 0.12.x，支持 HMAC-SHA256（HS256）和 RSA（RS256）算法，两种 Token 模式：
  - **SINGLE 模式**：单 Token（Access Token）
  - **DUAL 模式**：双 Token（Access Token + Refresh Token），独立签名密钥与过期时间
  - Token 刷新保留全部自定义 Claims
- **PasswordUtils** — BCrypt 哈希/匹配，AES 对称加密（Base64 输出，随机 IV，自动密钥长度适配）
- **UserContextHolder** — ThreadLocal 用户上下文（`setUser` / `getUserId` / `getClaim` / `getClaims`）
- **JwtAuthenticationFilter** — JWT 认证过滤器（自动提取 Bearer Token → 验证 → 写入上下文）
- **UserContextClearFilter** — 请求结束后清理 UserContext，防止内存泄漏
- **UserContextInterceptor** — Spring MVC 拦截器，支持 `exclude-paths` 排除路径
- **@AnonymousAccess** — 方法/类级注解，标记公开端点
- **TokenPair** — 双 Token 模式响应模型（`accessToken` / `refreshToken` / `expiresIn`）

### OSS 模块（`ikeu-components-oss`）

- **OssTemplate** — 对象存储抽象接口：`upload` / `download` / `getUrl` / `getPublicUrl` / `delete` / `batchDelete` / `exist`，支持自定义 Pre-signed URL 有效期
- **AliyunOssTemplate** — 阿里云 OSS 实现
- **MinioOssTemplate** — MinIO 实现

### Autoconfigure 模块（`ikeu-components-spring-boot-autoconfigure`）

全部 Bean 使用 `@ConditionalOnMissingBean`，**可被用户自定义 Bean 覆盖**。

| 配置类 | 绑定属性 | 说明 |
|--------|---------|------|
| `JacksonCustomAutoConfiguration` | `ikeu.jackson.*` | 全局 Jackson 配置（日期格式、Long→String、序列化包含策略、时区） |
| `HttpClientAutoConfiguration` | `ikeu.http-client.*` | `HttpClient` Bean 配置（连接/请求/下载超时、代理） |
| `SecurityAutoConfiguration` | `ikeu.jwt.*` | JWT 相关 Bean（JwtUtils、JwtProperties、Filter/Interceptor/Filter 注册） |
| `OssAutoConfiguration` | `ikeu.oss.*` | OSS 实现选择（根据 `ikeu.oss.type` 和 classpath SDK 自动选择） |
| `WebAutoConfiguration` | — | Web 层 Bean（`GlobalExceptionHandler`、`SpringContextHolder`） |

- `@AutoConfiguration` + `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`（Spring Boot 3.x 规范）
- 提供完整 `application-example.yml` 配置参考

---

## 🧪 测试覆盖

- **295 个单元测试**，28 个测试类
- 覆盖所有核心工具类、Web 层、Security 层、Autoconfigure 加载
- JUnit 5 + Mockito

---

## 📋 技术环境

| 项 | 版本 |
|----|------|
| Java | 17+ |
| Spring Boot | 3.2.0 |
| Spring Framework | 6.1.1 |
| jjwt | 0.12.6 |
| Jackson | 2.15.3 |
| Lombok | 1.18.30 |

---

## 🔗 仓库地址

[https://github.com/Ikeu-1030/convenient-springboot-components-starter](https://github.com/Ikeu-1030/convenient-springboot-components-starter)

## 📄 License

Apache-2.0

---

## 🙏 致谢

感谢所有参与开发、测试和反馈的贡献者。这是项目的首个正式版本，欢迎通过 GitHub Issues 提交反馈和建议！