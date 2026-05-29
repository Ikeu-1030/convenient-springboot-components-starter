English | [中文](README.md)

# ikeu-components-sms

SMS service abstraction supporting Alibaba Cloud SMS and Tencent Cloud SMS. Depends on `ikeu-components-core`.

## Prerequisites

SMS SDKs are **not transitive**. Add the relevant SDK explicitly:

```xml
<!-- Alibaba Cloud SMS -->
<dependency>
    <groupId>com.aliyun</groupId>
    <artifactId>dysmsapi20170525</artifactId>
</dependency>

<!-- Tencent Cloud SMS -->
<dependency>
    <groupId>com.tencentcloudapi</groupId>
    <artifactId>tencentcloud-sdk-java-sms</artifactId>
</dependency>
```

## Configuration

```yaml
ikeu:
  sms:
    enabled: true
    type: aliyun                # aliyun | tencent
    default-sign-name: "MyApp"
    aliyun:
      access-key-id: "xxx"
      access-key-secret: "xxx"
      sign-name: "MyApp"       # optional, overrides default-sign-name
      region-id: cn-hangzhou
      timeout-seconds: 10
    tencent:
      secret-id: "xxx"
      secret-key: "xxx"
      sdk-app-id: "1400000000"
      sign-name: "MyApp"       # optional, overrides default-sign-name
      region: ap-guangzhou
```

> Use environment variables for credentials: `${SMS_ACCESS_KEY}`, `${SMS_SECRET_KEY}`.

## SmsTemplate API

```java
@Autowired
private SmsTemplate smsTemplate;

// Send template SMS to a single phone number
Map<String, String> params = Map.of("code", "123456");
smsTemplate.send("13800138000", "SMS_001", params);

// Batch send (shared template params)
smsTemplate.sendBatch(
    List.of("13800138000", "13900139000"),
    "SMS_002",
    Map.of("name", "John", "time", "2026-05-30"));
```

## SmsException

All SMS errors are wrapped in `SmsException(errorCode, message, cause)`:

```java
try {
    smsTemplate.send("13800138000", "SMS_001", params);
} catch (SmsException e) {
    log.error("SMS failed: code={}, msg={}", e.getErrorCode(), e.getMessage());
}
```

## Implementations

Create your own `SmsTemplate` bean with the corresponding SDK. See `SmsAutoConfiguration` Javadoc for examples:

| Class | SDK | Condition |
|-------|-----|-----------|
| `AliyunSmsTemplate` | `com.aliyun:dysmsapi20170525` | `ikeu.sms.type=aliyun` |
| `TencentSmsTemplate` | `com.tencentcloudapi:tencentcloud-sdk-java-sms` | `ikeu.sms.type=tencent` |

```java
// Alibaba Cloud example
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

## Notes

- Phone numbers should use E.164 format (`+8613800138000`)
- Tencent Cloud SDK uses `Map.values()` ordering for template params — ensure it matches placeholder order
- Batch limits vary by provider (Alibaba 1000, Tencent 200); split larger batches yourself
