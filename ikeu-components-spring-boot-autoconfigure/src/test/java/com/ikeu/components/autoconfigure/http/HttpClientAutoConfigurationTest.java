package com.ikeu.components.autoconfigure.http;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.*;

class HttpClientAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(HttpClientAutoConfiguration.class));

    @Test
    void configuresHttpClientUtil() {
        runner.run(ctx -> {
            assertTrue(ctx.containsBean("ikeuHttpClientConfigurer"));
            // HttpClientUtil is a static utility — verify the bean was created
            // and properties are bound (tested in sub-tests below)
            HttpClientProperties props = ctx.getBean(HttpClientProperties.class);
            assertNotNull(props);
        });
    }

    @Test
    void withCustomTimeouts() {
        runner.withPropertyValues(
                "ikeu.http-client.connect-timeout=15s",
                "ikeu.http-client.request-timeout=45s",
                "ikeu.http-client.download-timeout=3m"
        ).run(ctx -> {
            HttpClientProperties props = ctx.getBean(HttpClientProperties.class);
            assertEquals(java.time.Duration.ofSeconds(15), props.getConnectTimeout());
            assertEquals(java.time.Duration.ofSeconds(45), props.getRequestTimeout());
            assertEquals(java.time.Duration.ofMinutes(3), props.getDownloadTimeout());
        });
    }

    @Test
    void withProxyConfig() {
        runner.withPropertyValues(
                "ikeu.http-client.proxy-host=proxy.example.com",
                "ikeu.http-client.proxy-port=3128"
        ).run(ctx -> {
            HttpClientProperties props = ctx.getBean(HttpClientProperties.class);
            assertEquals("proxy.example.com", props.getProxyHost());
            assertEquals(3128, props.getProxyPort());
        });
    }
}