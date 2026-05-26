You are an expert Java and Spring Boot developer, specializing in building internal libraries and custom Spring Boot starters. Your task is to create a comprehensive Spring Boot convenience toolkit project, designed to be published on GitHub and used across multiple projects. The project must follow the official Spring Boot starter structure, separating autoconfiguration from the starter module, and include all the utility classes and autoconfiguration needed to eliminate boilerplate code in daily development.

**Project Structure Requirement:**
The project named with groupId `com.ikeu.components`,
it should be a multi-module Maven project with the following modules:
- `-parent`: parent POM managing dependency versions.
- `-core`: pure Java utility classes (no Spring dependency), e.g., enhanced bean copying, JSON, date/time, string, tree builder, Snowflake ID generator.
- `-web`: includes unified response `Result<T>`, paging `PageResult<T>`, business exception, assertion utilities, and `SpringContextHolder`. Dependencies: Spring Web (provided), common-core.
- `-security`: JWT utilities, password encryption (BCrypt, AES), and user context holder. Dependencies: common-web, jjwt, Spring Security (provided).
- `-oss`: abstract OSS template interface and implementations for Alibaba Cloud OSS and MinIO. Dependencies: common-core, OSS SDKs (optional).
- `-autoconfigure`: contains ALL auto-configuration classes, `@ConfigurationProperties` classes, and auto-configuration imports file. It depends on all the above functional modules and `spring-boot-autoconfigure`.
- `-spring-boot-starter`: an empty POM that simply aggregates dependencies on `common-autoconfigure`. This is the only artifact end-users need to import.

**Functional Requirements (detailed):**
1. **Bean & Object Operations (in common-core):**
    - `BeanCopyUtils`: Copy properties ignoring null, deep copy lists, merge non-null properties, copy with custom converter.
    - `DtoConverter`: Generic interface or static methods for converting between Entity, DTO, VO, supporting single object, list, and MyBatis-Plus `Page` conversion. Adapt to MapStruct-like behavior or reflection-based.

2. **Web Layer (in common-web):**
    - `Result<T>`: Generic API response with `code`, `message`, `data`. Provide static factories: `success(T data)`, `error(int code, String message)`, `of(T data)` (auto judge).
    - `PageResult<T>`: `total`, `current`, `pages`, `records`. Factory to convert from MyBatis-Plus `Page`.
    - `BusinessException`: Custom runtime exception with error code and message.
    - `AssertUtils`: Static methods like `notNull`, `isTrue`, `notEmpty` that throw `BusinessException`.
    - `GlobalExceptionHandler`: `@RestControllerAdvice` handling `BusinessException`, validation errors, and generic exceptions, returning `Result`.
    - `SpringContextHolder`: implements `ApplicationContextAware`, provides static methods to get beans and environment.

3. **Security (in common-security):**
    - `JwtUtils`: Uses `jjwt` (0.12.x). Methods: `generateToken(userId, claims, duration)`, `parseToken(token)`, `getUserId(token)`, `isExpired(token)`, `refreshToken(token, newDuration)`. Support HMAC-SHA256 and RSA algorithms configurable via `@ConfigurationProperties`.
    - `PasswordUtils`: BCrypt hashing and matching; AES symmetric encryption for sensitive data (e.g., phone numbers).
    - `UserContextHolder`: ThreadLocal-based holder for current login user; provide `setUser`, `getUser`, `getUserId`.
    - `JwtAuthenticationFilter`: A servlet filter that extracts and validates JWT from Authorization header and sets the user context. This filter should be auto-configured but not enforced; users can opt-in via property.

4. **OSS (in common-oss):**
    - `OssTemplate` interface: `upload`, `download`, `getUrl`, `getPublicUrl`, `delete`, `batchDelete`, `exist`.
    - `AliyunOssTemplate` and `MinioOssTemplate` implementations.
    - `OssProperties`: `@ConfigurationProperties("common.oss")` with fields for type (aliyun/minio), endpoint, accessKey, secretKey, bucket, cdnDomain.
    - Auto-configuration class selecting implementation based on `common.oss.type` and availability of SDK classes (`@ConditionalOnClass`).

5. **Common Utilities (in common-core):**
    - `DateUtils`: format, parse, start/end of day, add/subtract days, days between, time period overlap check.
    - `StringUtils`: camel/underline conversion, UUID without dashes, random numeric/alphanumeric string, regex validators for mobile, email, id card.
    - `JsonUtils`: based on Jackson `ObjectMapper`, with null exclusion and date format configuration. Methods: `toJson`, `fromJson`, `fromJsonList`.
    - `TreeUtils`: build tree from flat list given parentId field, extract leaf node IDs, sort tree.
    - `SnowflakeIdGenerator`: configurable workerId/datacenterId, thread-safe ID generation.

**Technical Requirements:**
- Spring Boot version: **3.2.x**. Java version: **17**.
- Use **Lombok** (`@Data`, `@Slf4j`, `@Builder`, etc.) to reduce boilerplate.
- All autoconfiguration classes should be placed in `common-autoconfigure`, annotated with `@AutoConfiguration` (Spring Boot 3.x), and registered in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
- Use `@EnableConfigurationProperties` to bind property classes, and `@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty` for conditional bean registration.
- Each functional module should have clean separation: business logic in the module, configuration in autoconfigure.
- Provide an example `application.yml` snippet with all configurable properties (under `common.jwt`, `common.oss`, etc.).
- Add JavaDoc to public APIs and important methods.
- Write basic unit tests using JUnit 5 and Mockito for key utilities and autoconfiguration loading.

**Output Instructions:**
- First, provide the full project directory tree.
- Then, for each module, provide the complete `pom.xml` and every Java source file. If a file is too large, you may summarize repeated parts, but ensure all critical logic is present.
- Explain the design rationale where necessary, especially for autoconfiguration ordering and conditional beans.

**Additional Precision Requirements:**
- The `BeanCopyUtils` must be able to handle nested property copy with a configurable depth to prevent infinite loops.
- The `DtoConverter` should have an option to register custom mappings at startup (e.g., via a static map or MapStruct interface).
- The JWT token should include standard claims (sub, iat, exp) plus custom claims. When refreshing, the new token should preserve all custom claims.
- The `UserContextHolder` must be cleared after request processing to prevent memory leaks; an auto-configured `Filter` or `HandlerInterceptor` should do this.
- The OSS template must support specifying a custom expiration for pre-signed URLs and a default bucket that can be overridden per method call.
- The Snowflake ID generator must avoid clock drift issues by blocking and waiting if the clock moves backwards.
- All auto-configured beans should be overridable by user-defined beans.
- Ensure thread-safety for all utility classes (stateless or properly synchronized).
- The `common-spring-boot-starter` must have no Java source files, only a `pom.xml` that pulls in `common-autoconfigure`.
- The final project should be ready to build with `mvn clean install` and publish to Maven Central if needed.