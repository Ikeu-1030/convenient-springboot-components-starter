package com.ikeu.components.autoconfigure.oss;

import com.aliyun.oss.OSS;
import com.ikeu.components.oss.template.AliyunOssTemplate;
import com.ikeu.components.oss.template.MinioOssTemplate;
import com.ikeu.components.oss.template.OssTemplate;
import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for OSS. Selects implementation based on {@code ikeu.oss.type}.
 * <p>
 * Activation: {@code ikeu.oss.enabled=true}.
 *
 * <h3>Selection logic</h3>
 * <ul>
 *   <li>{@code type=aliyun} (default) + {@code OSS.class} on classpath → {@link AliyunOssTemplate}</li>
 *   <li>{@code type=minio} + {@code MinioClient.class} on classpath → {@link MinioOssTemplate}</li>
 * </ul>
 * Both beans use {@code destroyMethod="shutdown"} for proper resource cleanup
 * (OSS client shutdown, MinIO no-op placeholder).
 *
 * <h3>Config example</h3>
 * <pre>{@code
 * ikeu:
 *   oss:
 *     enabled: true
 *     type: aliyun
 *     endpoint: "https://oss-cn-hangzhou.aliyuncs.com"
 *     access-key: "your-access-key"
 *     secret-key: "your-secret-key"
 *     bucket: "your-bucket"
 *     cdn-domain: "cdn.example.com"
 * }</pre>
 *
 * @author ikeu
 * @since 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(OssProperties.class)
@ConditionalOnProperty(prefix = "ikeu.oss", name = "enabled", havingValue = "true")
public class OssAutoConfiguration {

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    @ConditionalOnClass(OSS.class)
    @ConditionalOnProperty(prefix = "ikeu.oss", name = "type", havingValue = "aliyun",
            matchIfMissing = true)
    public OssTemplate aliyunOssTemplate(OssProperties properties) {
        return new AliyunOssTemplate(
                properties.getEndpoint(),
                properties.getAccessKey(),
                properties.getSecretKey(),
                properties.getBucket(),
                properties.getCdnDomain());
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    @ConditionalOnClass(MinioClient.class)
    @ConditionalOnProperty(prefix = "ikeu.oss", name = "type", havingValue = "minio")
    public OssTemplate minioOssTemplate(OssProperties properties) {
        return new MinioOssTemplate(
                properties.getEndpoint(),
                properties.getAccessKey(),
                properties.getSecretKey(),
                properties.getBucket(),
                properties.getCdnDomain());
    }
}
