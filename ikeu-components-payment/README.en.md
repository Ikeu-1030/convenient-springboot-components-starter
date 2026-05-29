English | [中文](README.md)

# ikeu-components-payment

Payment gateway abstraction supporting WeChat Pay (APIv3) and Alipay. Depends on `ikeu-components-core`.

## Prerequisites

Payment SDKs are **not transitive**. Add the relevant SDK explicitly:

```xml
<!-- WeChat Pay APIv3 -->
<dependency>
    <groupId>com.github.wechatpay-apiv3</groupId>
    <artifactId>wechatpay-java</artifactId>
</dependency>

<!-- Alipay -->
<dependency>
    <groupId>com.alipay.sdk</groupId>
    <artifactId>alipay-easysdk</artifactId>
</dependency>
```

## Configuration

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
      gateway-url: "openapi.alipay.com"   # optional
      notify-url: "https://api.example.com/notify/alipay"
```

> Store credentials in environment variables or a secrets manager — never hardcode.

## PaymentTemplate API

```java
@Autowired
private PaymentTemplate paymentTemplate;

// Unified order (returns QR code URL)
PaymentResult result = paymentTemplate.unifiedOrder(
    PaymentRequest.builder()
        .outTradeNo("ORD20260530001")
        .totalAmount(100L)            // amount in cents
        .subject("Test Product")
        .build());

String qrCodeUrl = result.getPayUrl();

// Query order
PaymentResult query = paymentTemplate.queryOrder("ORD20260530001");

// Close order
boolean closed = paymentTemplate.closeOrder("ORD20260530001");

// Refund
PaymentResult refund = paymentTemplate.refund(
    RefundRequest.builder()
        .outTradeNo("ORD20260530001")
        .refundAmount(100L)
        .refundReason("User cancelled")
        .build());
```

## Payment Status

| Status | Description |
|--------|-------------|
| `SUCCESS` | Payment succeeded |
| `CLOSED` | Order closed |
| `REFUND` | Refunded |
| `NOTPAY` | Not yet paid |
| `USERPAYING` | User is paying |

## PaymentCallbackHandler — Callback Template

Extend `PaymentCallbackHandler` and implement `handleSuccess` / `handleFailure`:

```java
@Component
public class MyPayCallback extends PaymentCallbackHandler {

    public MyPayCallback(PaymentTemplate template) {
        super(template);
    }

    @Override
    protected PaymentResult parseCallback(String data) {
        // Delegate to implementation (e.g. WechatPaymentTemplate.parseCallback)
        return ((WechatPaymentTemplate) paymentTemplate).parseCallback(data);
    }

    @Override
    protected void handleSuccess(PaymentResult result) {
        orderService.markPaid(result.getOutTradeNo());
    }

    @Override
    protected void handleFailure(PaymentResult result) {
        log.warn("Payment failed: {}", result.getOutTradeNo());
    }
}
```

Controller usage:

```java
@PostMapping("/notify/wechat")
public String notify(@RequestBody String body,
                     @RequestHeader("Wechatpay-Signature") String signature) {
    return callbackHandler.handle(body, signature);
}
```

## Implementations

Create your own `PaymentTemplate` bean with the corresponding SDK. See `PaymentAutoConfiguration` Javadoc for examples:

| Class | SDK | Condition |
|-------|-----|-----------|
| `WechatPaymentTemplate` | `com.github.wechatpay-apiv3:wechatpay-java` | `ikeu.payment.type=wechat` |
| `AlipayPaymentTemplate` | `com.alipay.sdk:alipay-easysdk` | `ikeu.payment.type=alipay` |

## Notes

- Amounts are in the smallest currency unit (cents); Alipay internally converts to yuan (2 decimal places)
- `verifyCallback` provides a unified interface for callback signature verification (differs per vendor)
- WeChat APIv3 callbacks require decryption — `WechatPaymentTemplate.parseCallback` handles this
- `PaymentResult.rawData` stores the raw vendor response JSON for debugging and extended field access
