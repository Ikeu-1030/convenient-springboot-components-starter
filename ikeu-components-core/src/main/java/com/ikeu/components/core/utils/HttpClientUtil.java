package com.ikeu.components.core.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP client utility using Java 11+ {@link java.net.http.HttpClient}.
 * <p>
 * Thread-safe singleton with HTTP/2 support. Covers GET, POST (JSON/form),
 * PUT, DELETE, file download, and async variants. Uses {@link JsonUtils}
 * for JSON serialization/deserialization.
 *
 * <h3>Defaults</h3>
 * <ul>
 *   <li>Connect timeout: 10s</li>
 *   <li>Request timeout: 30s</li>
 *   <li>Download timeout: 5min</li>
 * </ul>
 * All configurable via {@link #configure(HttpClient, Duration, Duration, Duration)}
 * or {@code application.yml} (when using the starter's auto-configuration).
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * // GET
 * String html = HttpClientUtil.doGet("https://api.example.com/users");
 * String result = HttpClientUtil.doGet("https://api.example.com/users",
 *         Map.of("page", "1", "size", "10"), null);
 * List<UserVo> users = HttpClientUtil.doGet(url, params, headers, UserVo.class);
 *
 * // POST JSON (body auto-serialized via JsonUtils)
 * String resp = HttpClientUtil.doPost(url, new CreateUserReq("John"));
 * UserVo user = HttpClientUtil.doPost(url, body, headers, UserVo.class);
 *
 * // POST form-encoded
 * String resp = HttpClientUtil.doPostForm(url, Map.of("user", "john"), null);
 *
 * // PUT / DELETE
 * HttpClientUtil.doPut(url, updateBody, headers);
 * HttpClientUtil.doDelete(url, headers);
 *
 * // Async
 * CompletableFuture<String> future = HttpClientUtil.doGetAsync(url, params, headers);
 * future.thenAccept(System.out::println);
 *
 * // File download
 * HttpClientUtil.download("https://cdn.example.com/report.pdf", Path.of("/tmp/report.pdf"));
 * }</pre>
 *
 * <h3>Notes</h3>
 * <ul>
 *   <li>Headers: user-provided Content-Type takes priority over defaults</li>
 *   <li>Query params: automatically URL-encoded</li>
 *   <li>Status handling: 2xx returned as-is; non-2xx logged as warning</li>
 *   <li>Call {@link #configure} early (e.g. from auto-configuration) before any requests</li>
 * </ul>
 *
 * @author ikeu
 * @since 1.0.0
 */
@Slf4j
public final class HttpClientUtil {

    private static volatile HttpClient client;
    private static volatile Duration connectTimeout = Duration.ofSeconds(10);
    private static volatile Duration requestTimeout = Duration.ofSeconds(30);
    private static volatile Duration downloadTimeout = Duration.ofMinutes(5);

    static {
        client = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    private HttpClientUtil() {
    }

    // ──────────────────────────────────────────────
    // Configuration
    // ──────────────────────────────────────────────

    /**
     * Replace the underlying HttpClient and timeouts.
     * Call this early (e.g. from auto-configuration) to use a custom
     * HttpClient with proxy, SSL context, or executor settings.
     *
     * @param customClient    custom HttpClient, or null to keep current
     * @param connectTimeout  connection timeout, or null to keep current
     * @param requestTimeout  per-request timeout, or null to keep current
     * @param downloadTimeout download timeout, or null to keep current
     */
    public static synchronized void configure(HttpClient customClient,
                                               Duration connectTimeout,
                                               Duration requestTimeout,
                                               Duration downloadTimeout) {
        if (customClient != null) {
            client = customClient;
            log.info("HttpClientUtil HttpClient replaced with custom instance");
        }
        if (connectTimeout != null) {
            HttpClientUtil.connectTimeout = connectTimeout;
        }
        if (requestTimeout != null) {
            HttpClientUtil.requestTimeout = requestTimeout;
        }
        if (downloadTimeout != null) {
            HttpClientUtil.downloadTimeout = downloadTimeout;
        }
    }

    /** Reset client and timeouts to defaults. */
    public static synchronized void reset() {
        connectTimeout = Duration.ofSeconds(10);
        requestTimeout = Duration.ofSeconds(30);
        downloadTimeout = Duration.ofMinutes(5);
        client = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        log.info("HttpClientUtil reset to defaults");
    }

    // ──────────────────────────────────────────────
    // GET
    // ──────────────────────────────────────────────

    /** GET with no parameters, return response body as String. */
    public static String doGet(String url) {
        return doGet(url, null, null);
    }

    /** GET with query parameters and custom headers. */
    public static String doGet(String url, Map<String, String> params,
                                Map<String, String> headers) {
        return execute(buildGetRequest(url, params, headers));
    }

    /** GET and deserialize response to target type. */
    public static <T> T doGet(String url, Map<String, String> params,
                               Map<String, String> headers, Class<T> responseType) {
        String body = doGet(url, params, headers);
        return body != null ? JsonUtils.fromJson(body, responseType) : null;
    }

    /** Async GET. */
    public static CompletableFuture<String> doGetAsync(String url,
                                                        Map<String, String> params,
                                                        Map<String, String> headers) {
        return executeAsync(buildGetRequest(url, params, headers));
    }

    // ──────────────────────────────────────────────
    // POST (JSON)
    // ──────────────────────────────────────────────

    /** POST JSON body, return response body as String. */
    public static String doPost(String url, Object body) {
        return doPost(url, body, null);
    }

    /** POST JSON body with custom headers. */
    public static String doPost(String url, Object body, Map<String, String> headers) {
        return execute(buildJsonPostRequest(url, body, headers));
    }

    /** POST JSON and deserialize response to target type. */
    public static <T> T doPost(String url, Object body, Map<String, String> headers,
                                Class<T> responseType) {
        String response = doPost(url, body, headers);
        return response != null ? JsonUtils.fromJson(response, responseType) : null;
    }

    /** Async POST JSON. */
    public static CompletableFuture<String> doPostAsync(String url, Object body,
                                                         Map<String, String> headers) {
        return executeAsync(buildJsonPostRequest(url, body, headers));
    }

    // ──────────────────────────────────────────────
    // POST (form-urlencoded)
    // ──────────────────────────────────────────────

    /** POST as {@code application/x-www-form-urlencoded}. */
    public static String doPostForm(String url, Map<String, String> formData,
                                     Map<String, String> headers) {
        return execute(buildFormPostRequest(url, formData, headers));
    }

    // ──────────────────────────────────────────────
    // PUT (JSON)
    // ──────────────────────────────────────────────

    /** PUT with JSON body. */
    public static String doPut(String url, Object body, Map<String, String> headers) {
        return execute(buildJsonPutRequest(url, body, headers));
    }

    /** PUT JSON and deserialize response. */
    public static <T> T doPut(String url, Object body, Map<String, String> headers,
                               Class<T> responseType) {
        String response = doPut(url, body, headers);
        return response != null ? JsonUtils.fromJson(response, responseType) : null;
    }

    // ──────────────────────────────────────────────
    // DELETE
    // ──────────────────────────────────────────────

    /** DELETE request. */
    public static String doDelete(String url) {
        return doDelete(url, null);
    }

    /** DELETE with custom headers. */
    public static String doDelete(String url, Map<String, String> headers) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .DELETE()
                .timeout(requestTimeout)
                .headers(toHeaderArray(headers))
                .build();
        return execute(request);
    }

    // ──────────────────────────────────────────────
    // File download
    // ──────────────────────────────────────────────

    /** Download a file from URL to target path. */
    public static Path download(String url, Path targetPath) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(downloadTimeout)
                    .build();
            HttpResponse<InputStream> response = client.send(request,
                    HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Download failed, status: " + response.statusCode());
            }
            Files.createDirectories(targetPath.getParent());
            try (InputStream body = response.body()) {
                Files.copy(body, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("Downloaded {} → {}", url, targetPath);
            return targetPath;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Download failed: " + url, e);
        }
    }

    // ──────────────────────────────────────────────
    // Internal execution
    // ──────────────────────────────────────────────

    private static String execute(HttpRequest request) {
        try {
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());
            log.debug("HTTP {} {} → {}", request.method(), request.uri(), response.statusCode());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("HTTP {} {} returned status {}: {}",
                        request.method(), request.uri(), response.statusCode(), response.body());
            }
            return response.body();
        } catch (IOException e) {
            log.error("HTTP {} {} failed", request.method(), request.uri(), e);
            throw new RuntimeException("HTTP request failed: " + request.uri(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("HTTP request interrupted: " + request.uri(), e);
        }
    }

    private static CompletableFuture<String> executeAsync(HttpRequest request) {
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    log.debug("HTTP {} {} → {}", request.method(), request.uri(),
                            response.statusCode());
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        log.warn("HTTP {} {} returned status {}: {}",
                                request.method(), request.uri(), response.statusCode(),
                                response.body());
                    }
                    return response.body();
                });
    }

    // ──────────────────────────────────────────────
    // Request builders
    // ──────────────────────────────────────────────

    private static final String CONTENT_TYPE = "Content-Type";

    private static HttpRequest buildGetRequest(String url, Map<String, String> params,
                                                Map<String, String> headers) {
        String fullUrl = buildUrl(url, params);
        return HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .GET()
                .timeout(requestTimeout)
                .headers(toHeaderArray(headers))
                .build();
    }

    private static HttpRequest buildJsonPostRequest(String url, Object body,
                                                     Map<String, String> headers) {
        String json = body instanceof String ? (String) body : JsonUtils.toJson(body);
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .headers(mergeContentType(headers, "application/json"))
                .POST(json == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(json))
                .timeout(requestTimeout)
                .build();
    }

    private static HttpRequest buildJsonPutRequest(String url, Object body,
                                                    Map<String, String> headers) {
        String json = body instanceof String ? (String) body : JsonUtils.toJson(body);
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .headers(mergeContentType(headers, "application/json"))
                .PUT(json == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(json))
                .timeout(requestTimeout)
                .build();
    }

    private static HttpRequest buildFormPostRequest(String url,
                                                     Map<String, String> formData,
                                                     Map<String, String> headers) {
        String encoded = encodeForm(formData);
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .headers(mergeContentType(headers, "application/x-www-form-urlencoded"))
                .POST(HttpRequest.BodyPublishers.ofString(encoded))
                .timeout(requestTimeout)
                .build();
    }

    // ──────────────────────────────────────────────
    // Header helpers
    // ──────────────────────────────────────────────

    /**
     * Merge a default Content-Type into headers. If the user's headers already
     * contain a Content-Type, it wins; otherwise the default is added.
     */
    static String[] mergeContentType(Map<String, String> headers, String defaultContentType) {
        if (headers != null && headers.containsKey(CONTENT_TYPE)) {
            return toHeaderArray(headers);
        }
        if (headers == null || headers.isEmpty()) {
            return new String[]{CONTENT_TYPE, defaultContentType};
        }
        String[] result = new String[headers.size() * 2 + 2];
        result[0] = CONTENT_TYPE;
        result[1] = defaultContentType;
        int i = 2;
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            result[i++] = entry.getKey();
            result[i++] = entry.getValue();
        }
        return result;
    }

    /** Convert header Map to alternating key-value String array. */
    static String[] toHeaderArray(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return new String[0];
        }
        String[] result = new String[headers.size() * 2];
        int i = 0;
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            result[i++] = entry.getKey();
            result[i++] = entry.getValue();
        }
        return result;
    }

    // ──────────────────────────────────────────────
    // URL utilities
    // ──────────────────────────────────────────────

    /** Build URL with URL-encoded query parameters. */
    static String buildUrl(String baseUrl, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return baseUrl;
        }
        StringJoiner joiner = new StringJoiner("&");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            joiner.add(urlEncode(entry.getKey()) + "=" + urlEncode(entry.getValue()));
        }
        String separator = baseUrl.contains("?") ? "&" : "?";
        return baseUrl + separator + joiner;
    }

    /** Form-encode a map. */
    static String encodeForm(Map<String, String> formData) {
        if (formData == null || formData.isEmpty()) {
            return "";
        }
        StringJoiner joiner = new StringJoiner("&");
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            joiner.add(urlEncode(entry.getKey()) + "=" + urlEncode(entry.getValue()));
        }
        return joiner.toString();
    }

    static String urlEncode(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}