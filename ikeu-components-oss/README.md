[English](README.en.md) | 中文

# ikeu-components-oss

对象存储抽象层，支持阿里云 OSS 和 MinIO。依赖 `ikeu-components-core`。

## 前置条件

OSS SDK **不传递**。使用前需显式添加对应 SDK：

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

## 配置

```yaml
ikeu:
  oss:
    enabled: true
    type: minio                 # aliyun | minio
    endpoint: http://localhost:9000
    access-key: minioadmin
    secret-key: minioadmin
    bucket: my-bucket
    cdn-domain: cdn.example.com # 可选，用于 getPublicUrl
```

> `access-key` / `secret-key` 建议使用环境变量：`${OSS_ACCESS_KEY}`。

## OssTemplate API

```java
@Autowired
private OssTemplate ossTemplate;

// 上传（默认 bucket 或指定 bucket）
ossTemplate.upload("avatar/001.jpg", inputStream, "image/jpeg");
ossTemplate.upload("other-bucket", "avatar/001.jpg", inputStream, "image/jpeg");

// 下载
InputStream is = ossTemplate.download("avatar/001.jpg");

// 预签名 URL（默认 1 小时，可自定义）
String signed = ossTemplate.getUrl("avatar/001.jpg");
String signed = ossTemplate.getUrl("avatar/001.jpg", Duration.ofMinutes(30));

// 公开 URL（有 CDN 走 CDN，否则走 OSS 域名）
String publicUrl = ossTemplate.getPublicUrl("avatar/001.jpg");

// 删除
ossTemplate.delete("avatar/001.jpg");
ossTemplate.batchDelete(List.of("a.txt", "b.txt"));

// 检查存在
boolean exists = ossTemplate.exist("avatar/001.jpg");
```

## 实现类

| 类 | 条件 |
|---|------|
| `AliyunOssTemplate` | classpath 有 `com.aliyun.oss.OSS` 且 `ikeu.oss.type=aliyun` |
| `MinioOssTemplate` | classpath 有 `io.minio.MinioClient` 且 `ikeu.oss.type=minio` |

> 未指定 type 时默认使用 aliyun。两个实现类都有 `shutdown()` 方法用于释放连接。

## 注意事项

- `getPublicUrl` 依赖 `cdn-domain` 配置；未配置时返回 OSS 默认域名
- 预签名 URL 的过期时间由 `getUrl` 的 `Duration` 参数控制，默认 1 小时
- Bucket 可配置默认值，也可每个方法单独指定
