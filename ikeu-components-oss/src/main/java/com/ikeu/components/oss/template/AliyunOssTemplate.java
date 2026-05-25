package com.ikeu.components.oss.template;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Date;
import java.util.List;

/**
 * Alibaba Cloud OSS implementation of {@link OssTemplate}.
 * @author ikeu
 * @since 1.0.0
 */
@Slf4j
public class AliyunOssTemplate implements OssTemplate {

    private final OSS ossClient;
    private final String endpoint;
    private final String defaultBucket;
    private final String cdnDomain;

    public AliyunOssTemplate(String endpoint, String accessKey, String secretKey,
                              String defaultBucket, String cdnDomain) {
        this.endpoint = endpoint;
        this.defaultBucket = defaultBucket;
        this.cdnDomain = cdnDomain;
        this.ossClient = new OSSClientBuilder().build(endpoint, accessKey, secretKey);
    }

    @Override
    public void upload(String bucket, String objectName, InputStream inputStream, String contentType) {
        PutObjectRequest request = new PutObjectRequest(bucket, objectName, inputStream);
        if (contentType != null && !contentType.isBlank()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            request.setMetadata(metadata);
        }
        ossClient.putObject(request);
    }

    @Override
    public void upload(String objectName, InputStream inputStream, String contentType) {
        upload(defaultBucket, objectName, inputStream, contentType);
    }

    @Override
    public InputStream download(String bucket, String objectName) {
        OSSObject ossObject = ossClient.getObject(bucket, objectName);
        return ossObject.getObjectContent();
    }

    @Override
    public InputStream download(String objectName) {
        return download(defaultBucket, objectName);
    }

    @Override
    public String getUrl(String bucket, String objectName, Duration expiration) {
        Date expiry = new Date(System.currentTimeMillis() + expiration.toMillis());
        URL url = ossClient.generatePresignedUrl(bucket, objectName, expiry);
        return url.toString();
    }

    @Override
    public String getUrl(String bucket, String objectName) {
        return getUrl(bucket, objectName, Duration.ofHours(1));
    }

    @Override
    public String getPublicUrl(String bucket, String objectName) {
        if (cdnDomain != null && !cdnDomain.isBlank()) {
            return String.format("https://%s/%s", cdnDomain, objectName);
        }
        return String.format("https://%s.%s/%s", bucket, endpoint, objectName);
    }

    @Override
    public String getPublicUrl(String objectName) {
        return getPublicUrl(defaultBucket, objectName);
    }

    @Override
    public void delete(String bucket, String objectName) {
        ossClient.deleteObject(bucket, objectName);
    }

    @Override
    public void delete(String objectName) {
        delete(defaultBucket, objectName);
    }

    @Override
    public void batchDelete(String bucket, List<String> objectNames) {
        DeleteObjectsRequest request = new DeleteObjectsRequest(bucket)
                .withKeys(objectNames);
        ossClient.deleteObjects(request);
    }

    @Override
    public void batchDelete(List<String> objectNames) {
        batchDelete(defaultBucket, objectNames);
    }

    @Override
    public boolean exist(String bucket, String objectName) {
        return ossClient.doesObjectExist(bucket, objectName);
    }

    @Override
    public boolean exist(String objectName) {
        return exist(defaultBucket, objectName);
    }

    /**
     * Shut down the underlying OSS client, releasing network connections.
     * Should be called when the bean is destroyed.
     */
    public void shutdown() {
        ossClient.shutdown();
        log.info("AliyunOssTemplate OSS client shut down");
    }
}
