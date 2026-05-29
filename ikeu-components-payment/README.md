[English](README.en.md) | 中文

# ikeu-components-payment

支付接口抽象层，支持微信支付（APIv3）和支付宝支付。依赖 `ikeu-components-core`。

## 前置条件

支付 SDK **不传递**。使用前需显式添加对应 SDK：

```xml
<!-- 微信支付 APIv3 -->
<dependency>
    <groupId>com.github.wechatpay-apiv3</groupId>
    <artifactId>wechatpay-java</artifactId>
</dependency>

<!-- 支付宝 -->
<dependency>
    <groupId>com.alipay.sdk</groupId>
    <artifactId>alipay-easysdk</artifactId>
</dependency>
```

## 配置

```yaml
ikeu:
  payment:
    enabled: true
    type: wechat              # wechat | alipay
    wechat:
      merchant-id: "1230000109"
      merchant-serial-number: "xxx"
      private-key: |
        -----BEGIN PRIVATE KEY-----
        ...
        -----END PRIVATE KEY-----
      api-v3-key: "your-api-v3-key"
      app-id: "wx1234567890"
      notify-url: "https://api.example.com/notify/wechat"
    alipay:
      app-id: "2021000000000001"
      private-key: "..."
      alipay-public-key: "..."
      gateway-url: "openapi.alipay.com"   # 可选
      notify-url: "https://api.example.com/notify/alipay"
```

> 密钥建议使用环境变量或密钥管理服务，不要硬编码在配置文件中。

## PaymentTemplate API

```java
@Autowired
private PaymentTemplate paymentTemplate;

// 统一下单（返回 QR 码 URL）
PaymentResult result = paymentTemplate.unifiedOrder(
    PaymentRequest.builder()
        .outTradeNo("ORD20260530001")
        .totalAmount(100L)            // 金额：分
        .subject("测试商品")
        .build());

String qrCodeUrl = result.getPayUrl();

// 查询订单
PaymentResult query = paymentTemplate.queryOrder("ORD20260530001");

// 关闭订单
boolean closed = paymentTemplate.closeOrder("ORD20260530001");

// 退款
PaymentResult refund = paymentTemplate.refund(
    RefundRequest.builder()
        .outTradeNo("ORD20260530001")
        .refundAmount(100L)
        .refundReason("用户取消")
        .build());
```

## 支付状态枚举

| 状态 | 说明 |
|------|------|
| `SUCCESS` | 支付成功 |
| `CLOSED` | 订单已关闭 |
| `REFUND` | 已退款 |
| `NOTPAY` | 未支付 |
| `USERPAYING` | 用户支付中 |

## PaymentCallbackHandler — 回调处理模板

业务方继承 `PaymentCallbackHandler`，实现 `handleSuccess` 和 `handleFailure`：

```java
@Component
public class MyPayCallback extends PaymentCallbackHandler {

    public MyPayCallback(PaymentTemplate template) {
        super(template);
    }

    @Override
    protected PaymentResult parseCallback(String data) {
        // 委托给实现类解析（如 WechatPaymentTemplate.parseCallback）
        return ((WechatPaymentTemplate) paymentTemplate).parseCallback(data);
    }

    @Override
    protected void handleSuccess(PaymentResult result) {
        // 更新订单状态、发送通知等
        orderService.markPaid(result.getOutTradeNo());
    }

    @Override
    protected void handleFailure(PaymentResult result) {
        // 记录异常或告警
        log.warn("Payment failed: {}", result.getOutTradeNo());
    }
}
```

在 Controller 中调用：

```java
@PostMapping("/notify/wechat")
public String notify(@RequestBody String body,
                     @RequestHeader("Wechatpay-Signature") String signature) {
    return callbackHandler.handle(body, signature);
}
```

## 实现类

实现类需要用户根据使用的 SDK 自行创建 Bean，参考 `PaymentAutoConfiguration` 的 JavaDoc：

| 类 | SDK | 条件 |
|---|-----|------|
| `WechatPaymentTemplate` | `com.github.wechatpay-apiv3:wechatpay-java` | `ikeu.payment.type=wechat` |
| `AlipayPaymentTemplate` | `com.alipay.sdk:alipay-easysdk` | `ikeu.payment.type=alipay` |

## 注意事项

- 金额单位为分（fen），支付宝实现在内部转换为元（两位小数）
- `verifyCallback` 用于回调签名验证，微信和支付宝验签机制不同但接口统一
- 微信 APIv3 回调需要解密，`WechatPaymentTemplate.parseCallback` 封装了解密逻辑
- `PaymentResult.rawData` 保存厂商原始响应 JSON，便于调试和提取扩展字段
