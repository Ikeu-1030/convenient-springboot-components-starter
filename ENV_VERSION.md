# ENV_VERSION

## Build Environment

| Item | Version |
|------|---------|
| Java | 17 |
| Maven | 3.8+ |
| Spring Boot | 3.2.0 |
| Spring Framework | 6.1.1 |

---

## Compile Dependencies

| GroupId | ArtifactId | Version | Scope |
|---------|------------|---------|-------|
| com.fasterxml.jackson.core | jackson-databind | 2.15.3 | compile |
| com.fasterxml.jackson.core | jackson-core | 2.15.3 | compile |
| com.fasterxml.jackson.core | jackson-annotations | 2.15.3 | compile |
| com.fasterxml.jackson.datatype | jackson-datatype-jsr310 | 2.15.3 | compile |
| io.jsonwebtoken | jjwt-api | 0.12.6 | compile |
| io.jsonwebtoken | jjwt-impl | 0.12.6 | runtime |
| io.jsonwebtoken | jjwt-jackson | 0.12.6 | runtime |
| org.slf4j | slf4j-api | 2.0.9 | compile |
| org.springframework.boot | spring-boot-autoconfigure | 3.2.0 | compile |
| org.springframework.boot | spring-boot | 3.2.0 | compile |
| org.springframework | spring-web | 6.1.1 | compile |
| org.springframework | spring-context | 6.1.1 | compile |
| org.springframework | spring-core | 6.1.1 | compile |

---

## Provided Dependencies

*User project must supply at runtime.*

| GroupId | ArtifactId | Version | Scope |
|---------|------------|---------|-------|
| org.springframework | spring-webmvc | 6.1.1 | provided |
| org.springframework.security | spring-security-core | 6.2.0 | provided |
| jakarta.servlet | jakarta.servlet-api | 6.0.0 | provided |
| org.projectlombok | lombok | 1.18.30 | optional |

---

## Optional Dependencies

*Not included transitively — user must explicitly add to `pom.xml`.*

| GroupId | ArtifactId | Version | Scope |
|---------|------------|---------|-------|
| com.aliyun.oss | aliyun-sdk-oss | 3.17.4 | optional |
| io.minio | minio | 8.5.9 | optional |
| org.springframework.boot | spring-boot-configuration-processor | 3.2.0 | optional |

### OSS 使用说明

`ikeu-components-oss` 提供了 `AliyunOssTemplate` 和 `MinioOssTemplate`，但默认不传递对应 SDK。
如需使用 OSS 功能，请在项目中显式声明对应 SDK：

```xml
<!-- 阿里云 OSS -->
<dependency>
    <groupId>com.aliyun.oss</groupId>
    <artifactId>aliyun-sdk-oss</artifactId>
</dependency>

<!-- MinIO -->
<dependency>
    <groupId>io.minio</groupId>
    <artifactId>minio</artifactId>
</dependency>
```

然后在 `application.yml` 中启用：

```yaml
ikeu:
  oss:
    enabled: true
    type: minio              # aliyun | minio
    endpoint: http://localhost:9000
    access-key: minioadmin
    secret-key: minioadmin
    bucket: my-bucket
```

---

## Test Dependencies

| GroupId | ArtifactId | Version | Scope |
|---------|------------|---------|-------|
| org.springframework.boot | spring-boot-starter-test | 3.2.0 | test |
| org.junit.jupiter | junit-jupiter | 5.10.x | test |
| org.mockito | mockito-core | 5.7.x | test |
| org.mockito | mockito-junit-jupiter | 5.7.x | test |
| com.aliyun.oss | aliyun-sdk-oss | 3.17.4 | test |
| io.minio | minio | 8.5.9 | test |

---

## Notes

- Lombok version is managed by `spring-boot-starter-parent` and applied via `maven-compiler-plugin` annotation processor path.
- Jackson, Spring Framework, and Micrometer versions are all managed transitively by Spring Boot — do not override without compatibility testing.
- jjwt 0.12.x uses the new `Jwts.parser().verifyWith()` API — not backward-compatible with 0.11.x.
- OSS SDKs (`aliyun-sdk-oss`, `minio`) are `optional` in production modules but `test` scoped in `ikeu-components-oss` — available for unit testing without user declaration.