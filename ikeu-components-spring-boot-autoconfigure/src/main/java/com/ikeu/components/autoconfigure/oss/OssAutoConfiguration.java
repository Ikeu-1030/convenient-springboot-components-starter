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
 * Auto-configuration for OSS. Selects implementation based on ikeu.oss.type.
 */
@AutoConfiguration
@EnableConfigurationProperties(OssProperties.class)
@ConditionalOnProperty(prefix = "ikeu.oss", name = "enabled", havingValue = "true")
public class OssAutoConfiguration {

    @Bean
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

    @Bean
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
