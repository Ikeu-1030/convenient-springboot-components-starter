package com.ikeu.components.autoconfigure.payment;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for payment gateway integration.
 * <p>
 * Prefix: {@code ikeu.payment}
 */
@ConfigurationProperties(prefix = "ikeu.payment")
@Data
public class PaymentProperties {

    /** Enable payment auto-configuration. Default: false. */
    private boolean enabled = false;

    /** Provider type: {@code wechat} or {@code alipay}. */
    private String type;

    /** WeChat Pay specific configuration. */
    private Wechat wechat = new Wechat();

    /** Alipay specific configuration. */
    private Alipay alipay = new Alipay();

    @Data
    public static class Wechat {
        /** WeChat Pay merchant ID (mchid). */
        private String merchantId;
        /** Merchant API certificate serial number. */
        private String merchantSerialNumber;
        /** Path or content of the merchant private key (PEM format). */
        private String privateKey;
        /** APIv3 key for callback decryption. */
        private String apiV3Key;
        /** WeChat app ID. */
        private String appId;
        /** Default callback/notify URL. */
        private String notifyUrl;
    }

    @Data
    public static class Alipay {
        /** Alipay app ID. */
        private String appId;
        /** Merchant private key (PKCS8 format). */
        private String privateKey;
        /** Alipay public key for signature verification. */
        private String alipayPublicKey;
        /** Alipay gateway URL. Default: openapi.alipay.com. */
        private String gatewayUrl;
        /** Default callback/notify URL. */
        private String notifyUrl;
    }
}
