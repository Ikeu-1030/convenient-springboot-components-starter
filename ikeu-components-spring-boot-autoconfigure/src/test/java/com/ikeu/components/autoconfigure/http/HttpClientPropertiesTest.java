package com.ikeu.components.autoconfigure.http;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class HttpClientPropertiesTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Configuration
    @EnableConfigurationProperties(HttpClientProperties.class)
    static class TestConfig {}

    @Test
    void defaultValues() {
        runner.run(ctx -> {
            HttpClientProperties props = ctx.getBean(HttpClientProperties.class);
            assertEquals(Duration.ofSeconds(10), props.getConnectTimeout());
            assertEquals(Duration.ofSeconds(30), props.getRequestTimeout());
            assertEquals(Duration.ofMinutes(5), props.getDownloadTimeout());
            assertNull(props.getProxyHost());
            assertNull(props.getProxyPort());
        });
    }

    @Test
    void customTimeouts() {
        runner.withPropertyValues(
                "ikeu.http-client.connect-timeout=5s",
                "ikeu.http-client.request-timeout=60s",
                "ikeu.http-client.download-timeout=10m"
        ).run(ctx -> {
            HttpClientProperties props = ctx.getBean(HttpClientProperties.class);
            assertEquals(Duration.ofSeconds(5), props.getConnectTimeout());
            assertEquals(Duration.ofSeconds(60), props.getRequestTimeout());
            assertEquals(Duration.ofMinutes(10), props.getDownloadTimeout());
        });
    }

    @Test
    void proxyConfig() {
        runner.withPropertyValues(
                "ikeu.http-client.proxy-host=192.168.1.1",
                "ikeu.http-client.proxy-port=8080"
        ).run(ctx -> {
            HttpClientProperties props = ctx.getBean(HttpClientProperties.class);
            assertEquals("192.168.1.1", props.getProxyHost());
            assertEquals(8080, props.getProxyPort());
        });
    }
}