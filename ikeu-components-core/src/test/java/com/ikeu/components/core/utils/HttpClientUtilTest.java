package com.ikeu.components.core.utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class HttpClientUtilTest {

    private static HttpServer server;
    private static int port;

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/get", new EchoHandler());
        server.createContext("/post", new EchoHandler());
        server.createContext("/put", new EchoHandler());
        server.createContext("/delete", new EchoHandler());
        server.createContext("/error", new ErrorHandler());
        server.createContext("/download", new DownloadHandler());
        server.setExecutor(null);
        server.start();
        port = server.getAddress().getPort();
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    @BeforeEach
    void resetClient() {
        HttpClientUtil.reset();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    // ── GET ──

    @Test
    void doGet_basic() {
        String result = HttpClientUtil.doGet(url("/get"));
        assertNotNull(result);
        assertTrue(result.contains("\"method\":\"GET\""));
    }

    @Test
    void doGet_withParams() {
        String result = HttpClientUtil.doGet(url("/get"),
                Map.of("page", "1", "size", "10"), null);
        assertNotNull(result);
        assertTrue(result.contains("\"query\""));
    }

    @Test
    void doGet_deserializeResponse() {
        Map<String, Object> result = HttpClientUtil.doGet(url("/get"),
                null, null, Map.class);
        assertNotNull(result);
        assertEquals("GET", result.get("method"));
    }

    @Test
    void doGetAsync() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<String> future = HttpClientUtil.doGetAsync(
                url("/get"), null, null);
        String result = future.get(10, TimeUnit.SECONDS);
        assertTrue(result.contains("\"method\":\"GET\""));
    }

    // ── POST ──

    @Test
    void doPost_json() {
        String result = HttpClientUtil.doPost(url("/post"),
                Map.of("name", "test"));
        assertTrue(result.contains("\"method\":\"POST\""));
    }

    @Test
    void doPost_deserialize() {
        Map<String, Object> result = HttpClientUtil.doPost(url("/post"),
                Map.of("key", "value"), null, Map.class);
        assertNotNull(result);
    }

    @Test
    void doPostAsync() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<String> future = HttpClientUtil.doPostAsync(url("/post"),
                Map.of("a", 1), null);
        String result = future.get(10, TimeUnit.SECONDS);
        assertTrue(result.contains("\"method\":\"POST\""));
    }

    // ── POST form ──

    @Test
    void doPostForm() {
        String result = HttpClientUtil.doPostForm(url("/post"),
                Map.of("user", "john", "role", "admin"), null);
        assertNotNull(result);
    }

    // ── PUT ──

    @Test
    void doPut() {
        String result = HttpClientUtil.doPut(url("/put"),
                Map.of("name", "updated"), null);
        assertTrue(result.contains("\"method\":\"PUT\""));
    }

    // ── DELETE ──

    @Test
    void doDelete() {
        String result = HttpClientUtil.doDelete(url("/delete"));
        assertTrue(result.contains("\"method\":\"DELETE\""));
    }

    // ── Error handling ──

    @Test
    void non2xxStillReturns() {
        String result = HttpClientUtil.doGet(url("/error"));
        assertNotNull(result);
    }

    // ── Download ──

    @Test
    void download() throws IOException {
        Path target = Path.of(System.getProperty("java.io.tmpdir"),
                "ikeu-test-download-" + System.currentTimeMillis() + ".txt");
        try {
            Path result = HttpClientUtil.download(url("/download"), target);
            assertNotNull(result);
            assertTrue(java.nio.file.Files.exists(result));
            String content = java.nio.file.Files.readString(result);
            assertTrue(content.contains("download-data"));
        } finally {
            java.nio.file.Files.deleteIfExists(target);
        }
    }

    // ── Configuration ──

    @Test
    void configure_updatesTimeouts() {
        HttpClientUtil.configure(null,
                java.time.Duration.ofSeconds(5),
                java.time.Duration.ofSeconds(15),
                java.time.Duration.ofMinutes(1));
        // Just verify no exception — timeouts are tested implicitly
        String result = HttpClientUtil.doGet(url("/get"));
        assertNotNull(result);
    }

    @Test
    void reset_restoresDefaults() {
        HttpClientUtil.reset();
        String result = HttpClientUtil.doGet(url("/get"));
        assertNotNull(result);
    }

    // ── URL building ──

    @Test
    void buildUrl_encodesParams() {
        String url = HttpClientUtil.buildUrl("http://example.com/api",
                Map.of("name", "hello world", "key", "val&ue"));
        assertTrue(url.contains("hello+world"));
        assertTrue(url.contains("val%26ue"));
    }

    @Test
    void buildUrl_emptyParams() {
        String result = HttpClientUtil.buildUrl("http://example.com", null);
        assertEquals("http://example.com", result);
    }

    @Test
    void buildUrl_existingQuery() {
        String result = HttpClientUtil.buildUrl("http://example.com?a=1",
                Map.of("b", "2"));
        assertTrue(result.startsWith("http://example.com?a=1&"));
    }

    @Test
    void mergeContentType_userWins() {
        String[] headers = HttpClientUtil.mergeContentType(
                Map.of("Content-Type", "text/plain"), "application/json");
        assertTrue(containsPair(headers, "Content-Type", "text/plain"));
    }

    // ── HTTP Handlers ──

    static class EchoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();
            String resp = "{\"method\":\"" + method + "\"";
            resp += ",\"path\":\"" + exchange.getRequestURI().getPath() + "\"";
            if (query != null) {
                resp += ",\"query\":\"" + query + "\"";
            }
            resp += "}";
            byte[] bytes = resp.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    static class ErrorHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String resp = "{\"error\":\"not found\"}";
            byte[] bytes = resp.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(404, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    static class DownloadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String resp = "this-is-download-data-content";
            byte[] bytes = resp.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private static boolean containsPair(String[] arr, String key, String value) {
        for (int i = 0; i < arr.length; i += 2) {
            if (arr[i].equals(key) && arr[i + 1].equals(value)) {
                return true;
            }
        }
        return false;
    }
}