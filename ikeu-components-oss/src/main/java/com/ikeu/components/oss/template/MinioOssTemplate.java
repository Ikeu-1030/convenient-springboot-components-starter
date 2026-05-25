package com.ikeu.components.oss.template;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

/**
 * MinIO implementation of {@link OssTemplate}.
 * @author ikeu
 * @since 1.0.0
 */
@Slf4j
public class MinioOssTemplate implements OssTemplate {

    private final MinioClient minioClient;
    private final String defaultBucket;
    private final String cdnDomain;

    public MinioOssTemplate(String endpoint, String accessKey, String secretKey,
                             String defaultBucket, String cdnDomain) {
        this.defaultBucket = defaultBucket;
        this.cdnDomain = cdnDomain;
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Override
    public void upload(String bucket, String objectName, InputStream inputStream, String contentType) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(inputStream, inputStream.available(), -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            log.error("MinIO upload failed: {}", objectName, e);
            throw new RuntimeException("MinIO upload failed", e);
        }
    }

    @Override
    public void upload(String objectName, InputStream inputStream, String contentType) {
        upload(defaultBucket, objectName, inputStream, contentType);
    }

    @Override
    public InputStream download(String bucket, String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("MinIO download failed: {}", objectName, e);
            throw new RuntimeException("MinIO download failed", e);
        }
    }

    @Override
    public InputStream download(String objectName) {
        return download(defaultBucket, objectName);
    }

    @Override
    public String getUrl(String bucket, String objectName, Duration expiration) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .method(Method.GET)
                    .expiry((int) expiration.getSeconds())
                    .build());
        } catch (Exception e) {
            log.error("MinIO getUrl failed: {}", objectName, e);
            throw new RuntimeException("MinIO getUrl failed", e);
        }
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
        return getUrl(bucket, objectName);
    }

    @Override
    public String getPublicUrl(String objectName) {
        return getPublicUrl(defaultBucket, objectName);
    }

    @Override
    public void delete(String bucket, String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("MinIO delete failed: {}", objectName, e);
            throw new RuntimeException("MinIO delete failed", e);
        }
    }

    @Override
    public void delete(String objectName) {
        delete(defaultBucket, objectName);
    }

    @Override
    public void batchDelete(String bucket, List<String> objectNames) {
        try {
            List<DeleteObject> objects = new LinkedList<>();
            for (String name : objectNames) {
                objects.add(new DeleteObject(name));
            }
            Iterable<io.minio.Result<io.minio.messages.DeleteError>> results =
                    minioClient.removeObjects(RemoveObjectsArgs.builder()
                            .bucket(bucket)
                            .objects(objects)
                            .build());
            for (io.minio.Result<io.minio.messages.DeleteError> result : results) {
                try {
                    io.minio.messages.DeleteError error = result.get();
                    log.warn("MinIO delete error: {} - {}", error.objectName(), error.message());
                } catch (Exception ignored) {
                    // result.get() throws if the individual result is an error
                }
            }
        } catch (Exception e) {
            log.error("MinIO batch delete failed", e);
            throw new RuntimeException("MinIO batch delete failed", e);
        }
    }

    @Override
    public void batchDelete(List<String> objectNames) {
        batchDelete(defaultBucket, objectNames);
    }

    @Override
    public boolean exist(String bucket, String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean exist(String objectName) {
        return exist(defaultBucket, objectName);
    }

    /**
     * Cleanup hook called at bean destruction.
     * The MinIO client's underlying OkHttp connection pool is managed by the JVM;
     * this is a no-op placeholder for future resource management.
     */
    public void shutdown() {
        log.debug("MinioOssTemplate shutdown (no active resources to release)");
    }
}
