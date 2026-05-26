English | [中文](README.md)

# ikeu-components-oss

Object storage abstraction supporting Alibaba Cloud OSS and MinIO. Depends on `ikeu-components-core`.

## Prerequisites

OSS SDKs are **not transitive**. Add the relevant SDK explicitly:

```xml
<!-- Alibaba Cloud OSS -->
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

## Configuration

```yaml
ikeu:
  oss:
    enabled: true
    type: minio                 # aliyun | minio
    endpoint: http://localhost:9000
    access-key: minioadmin
    secret-key: minioadmin
    bucket: my-bucket
    cdn-domain: cdn.example.com # optional, used by getPublicUrl
```

> Use environment variables for credentials: `${OSS_ACCESS_KEY}`.

## OssTemplate API

```java
@Autowired
private OssTemplate ossTemplate;

// Upload (default bucket or explicit)
ossTemplate.upload("avatar/001.jpg", inputStream, "image/jpeg");
ossTemplate.upload("other-bucket", "avatar/001.jpg", inputStream, "image/jpeg");

// Download
InputStream is = ossTemplate.download("avatar/001.jpg");

// Pre-signed URL (default 1 hour, customizable)
String signed = ossTemplate.getUrl("avatar/001.jpg");
String signed = ossTemplate.getUrl("avatar/001.jpg", Duration.ofMinutes(30));

// Public URL (CDN if configured, otherwise OSS domain)
String publicUrl = ossTemplate.getPublicUrl("avatar/001.jpg");

// Delete
ossTemplate.delete("avatar/001.jpg");
ossTemplate.batchDelete(List.of("a.txt", "b.txt"));

// Existence check
boolean exists = ossTemplate.exist("avatar/001.jpg");
```

## Implementations

| Class | Condition |
|-------|-----------|
| `AliyunOssTemplate` | `com.aliyun.oss.OSS` on classpath & `ikeu.oss.type=aliyun` |
| `MinioOssTemplate` | `io.minio.MinioClient` on classpath & `ikeu.oss.type=minio` |

> Defaults to aliyun when type is not specified. Both implementations have `shutdown()` for connection cleanup.

## Notes

- `getPublicUrl` depends on `cdn-domain`; falls back to OSS default domain
- Pre-signed URL expiration is controlled by the `Duration` parameter, default 1 hour
- Bucket can be configured globally or overridden per method call
