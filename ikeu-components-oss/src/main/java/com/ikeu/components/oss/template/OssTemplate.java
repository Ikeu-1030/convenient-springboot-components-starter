package com.ikeu.components.oss.template;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;

/**
 * Abstract OSS template defining standard object storage operations.
 * <p>
 * Implementations: {@link AliyunOssTemplate} (Alibaba Cloud OSS),
 * {@link MinioOssTemplate} (MinIO).
 *
 * <h3>Methods</h3>
 * <ul>
 *   <li>Overloads with explicit {@code bucket} param override the default bucket</li>
 *   <li>Overloads without {@code bucket} use the configured default</li>
 *   <li>{@code getUrl} produces a pre-signed URL with configurable expiration</li>
 *   <li>{@code getPublicUrl} returns a CDN or direct access URL</li>
 * </ul>
 *
 * <h3>Caveat</h3>
 * The {@code InputStream} passed to {@code upload()} is NOT closed by the
 * implementation. The caller is responsible for closing the stream after the
 * upload completes.
 *
 * @author ikeu
 * @since 1.0.0
 */
public interface OssTemplate {

    /**
     * Upload an object to the specified bucket.
     */
    void upload(String bucket, String objectName, InputStream inputStream, String contentType);

    /**
     * Upload to the default bucket.
     */
    void upload(String objectName, InputStream inputStream, String contentType);

    /**
     * Download an object from the specified bucket.
     */
    InputStream download(String bucket, String objectName);

    /**
     * Download from the default bucket.
     */
    InputStream download(String objectName);

    /**
     * Generate a pre-signed URL with custom expiration.
     */
    String getUrl(String bucket, String objectName, Duration expiration);

    /**
     * Generate a pre-signed URL with default expiration.
     */
    String getUrl(String bucket, String objectName);

    /**
     * Generate a public CDN URL.
     */
    String getPublicUrl(String bucket, String objectName);

    /**
     * Generate a public CDN URL from the default bucket.
     */
    String getPublicUrl(String objectName);

    /**
     * Delete an object from the specified bucket.
     */
    void delete(String bucket, String objectName);

    /**
     * Delete from the default bucket.
     */
    void delete(String objectName);

    /**
     * Batch delete objects from the specified bucket.
     */
    void batchDelete(String bucket, List<String> objectNames);

    /**
     * Batch delete from the default bucket.
     */
    void batchDelete(List<String> objectNames);

    /**
     * Check if an object exists in the specified bucket.
     */
    boolean exist(String bucket, String objectName);

    /**
     * Check if an object exists in the default bucket.
     */
    boolean exist(String objectName);

    /**
     * Shut down the underlying OSS client, releasing network connections.
     */
    void shutdown();
}
