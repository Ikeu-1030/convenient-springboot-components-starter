package com.ikeu.components.autoconfigure.http;

import com.ikeu.components.core.utils.HttpClientUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Auto-configuration wiring {@link HttpClientProperties} into
 * {@link HttpClientUtil}, so the static HTTP client respects
 * {@code application.yml} settings.
 * <p>
 * Configures proxy support out of the box when {@code ikeu.http-client.proxy-host}
 * and {@code proxy-port} are both set.
 *
 * <h3>Config example</h3>
 * <pre>{@code
 * ikeu:
 *   http-client:
 *     connect-timeout: 10s
 *     request-timeout: 30s
 *     download-timeout: 5m
 *     proxy-host: 127.0.0.1
 *     proxy-port: 8888
 * }</pre>
 *
 * @author ikeu
 * @since 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(HttpClientProperties.class)
public class HttpClientAutoConfiguration {

    @Bean
    public Void ikeuHttpClientConfigurer(HttpClientProperties props) {
        HttpClient.Builder clientBuilder = HttpClient.newBuilder()
                .connectTimeout(props.getConnectTimeout())
                .followRedirects(HttpClient.Redirect.NORMAL);

        // Proxy
        if (props.getProxyHost() != null && !props.getProxyHost().isBlank()
                && props.getProxyPort() != null && props.getProxyPort() > 0) {
            clientBuilder.proxy(ProxySelector.of(
                    new InetSocketAddress(props.getProxyHost(), props.getProxyPort())));
        }

        HttpClient customClient = clientBuilder.build();
        HttpClientUtil.configure(customClient,
                props.getConnectTimeout(),
                props.getRequestTimeout(),
                props.getDownloadTimeout());
        return null;
    }
}