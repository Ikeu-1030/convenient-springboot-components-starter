package com.ikeu.components.autoconfigure.sms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for SMS integration.
 * <p>
 * Prefix: {@code ikeu.sms}
 */
@ConfigurationProperties(prefix = "ikeu.sms")
@Data
public class SmsProperties {

    /** Enable SMS auto-configuration. Default: false. */
    private boolean enabled = false;

    /** Provider type: {@code aliyun} or {@code tencent}. */
    private String type;

    /** Default sign name used when not specified per-message. */
    private String defaultSignName;

    /** AliCloud SMS specific configuration. */
    private Aliyun aliyun = new Aliyun();

    /** Tencent Cloud SMS specific configuration. */
    private Tencent tencent = new Tencent();

    @Data
    public static class Aliyun {
        private String accessKeyId;
        private String accessKeySecret;
        private String signName;
        private String regionId = "cn-hangzhou";
        /** API call timeout in seconds. */
        private long timeoutSeconds = 10;
    }

    @Data
    public static class Tencent {
        private String secretId;
        private String secretKey;
        private String sdkAppId;
        private String signName;
        private String region = "ap-guangzhou";
    }
}
