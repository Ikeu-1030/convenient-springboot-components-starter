package com.ikeu.components.autoconfigure.oss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for OSS, prefix "ikeu.oss".
 */
@Data
@ConfigurationProperties(prefix = "ikeu.oss")
public class OssProperties {

    /** OSS type: aliyun or minio. */
    private String type;

    /** OSS endpoint URL. */
    private String endpoint;

    /** Access key. */
    private String accessKey;

    /** Secret key. */
    private String secretKey;

    /** Default bucket name. */
    private String bucket;

    /** CDN domain for public URLs. */
    private String cdnDomain;
}
