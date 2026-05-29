[English](README.en.md) | 中文

# ikeu-components-sms

短信服务抽象层，支持阿里云短信和腾讯云短信。依赖 `ikeu-components-core`。

## 前置条件

SMS SDK **不传递**。使用前需显式添加对应 SDK：

```xml
<!-- 阿里云短信 -->
<dependency>
    <groupId>com.aliyun</groupId>
    <artifactId>dysmsapi20170525</artifactId>
</dependency>

<!-- 腾讯云短信 -->
<dependency>
    <groupId>com.tencentcloudapi</groupId>
    <artifactId>tencentcloud-sdk-java-sms</artifactId>
</dependency>
```

## 配置

```yaml
ikeu:
  sms:
    enabled: true
    type: aliyun                # aliyun | tencent
    default-sign-name: "XX平台"
    aliyun:
      access-key-id: "xxx"
      access-key-secret: "xxx"
      sign-name: "XX平台"       # 可选，覆盖 default-sign-name
      region-id: cn-hangzhou
      timeout-seconds: 10
    tencent:
      secret-id: "xxx"
      secret-key: "xxx"
      sdk-app-id: "1400000000"
      sign-name: "XX平台"       # 可选，覆盖 default-sign-name
      region: ap-guangzhou
```

> `access-key-id` / `secret-key` 建议使用环境变量：`${SMS_ACCESS_KEY}`、`${SMS_SECRET_KEY}`。

## SmsTemplate API

```java
@Autowired
private SmsTemplate smsTemplate;

// 发送模板短信
Map<String, String> params = Map.of("code", "123456");
smsTemplate.send("13800138000", "SMS_001", params);

// 批量发送（相同模板参数）
smsTemplate.sendBatch(
    List.of("13800138000", "13900139000"),
    "SMS_002",
    Map.of("name", "张三", "time", "2026-05-30"));
```

## SmsException

所有 SMS 异常统一包装为 `SmsException(errorCode, message, cause)`：

```java
try {
    smsTemplate.send("13800138000", "SMS_001", params);
} catch (SmsException e) {
    log.error("SMS failed: code={}, msg={}", e.getErrorCode(), e.getMessage());
}
```

## 实现类

实现类需要用户根据使用的 SDK 自行创建 Bean，参考 `SmsAutoConfiguration` 的 JavaDoc：

| 类 | SDK | 条件 |
|---|-----|------|
| `AliyunSmsTemplate` | `com.aliyun:dysmsapi20170525` | `ikeu.sms.type=aliyun` |
| `TencentSmsTemplate` | `com.tencentcloudapi:tencentcloud-sdk-java-sms` | `ikeu.sms.type=tencent` |

```java
// 阿里云实现示例
@Bean
public SmsTemplate smsTemplate(SmsProperties props) {
    return new AliyunSmsTemplate(
        props.getAliyun().getAccessKeyId(),
        props.getAliyun().getAccessKeySecret(),
        props.getDefaultSignName(),
        props.getAliyun().getRegionId(),
        props.getAliyun().getTimeoutSeconds());
}
```

## 注意事项

- 手机号建议使用 E.164 格式（`+8613800138000`）
- 模板参数值顺序：腾讯云 SDK 按 `Map.values()` 顺序排列，需与模板占位符顺序一致
- 批量发送时各厂商单次最大数量不同（阿里云 1000，腾讯云 200），超量需自行分片
