package com.ikeu.components.autoconfigure.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Auto-configuration for SMS services — binds {@link SmsProperties}.
 * <p>
 * When {@code ikeu.sms.enabled=true}, properties are bound and available for
 * injection. To use a specific provider, add the corresponding SDK to your
 * project and define an {@code SmsTemplate} bean:
 *
 * <h3>Aliyun SMS</h3>
 * <pre>{@code
 * @Bean
 * public SmsTemplate smsTemplate(SmsProperties props) {
 *     return new AliyunSmsTemplate(
 *         props.getAliyun().getAccessKeyId(),
 *         props.getAliyun().getAccessKeySecret(),
 *         props.getDefaultSignName(),
 *         props.getAliyun().getRegionId(),
 *         props.getAliyun().getTimeoutSeconds());
 * }
 * }</pre>
 *
 * <h3>Tencent SMS</h3>
 * <pre>{@code
 * @Bean
 * public SmsTemplate smsTemplate(SmsProperties props) {
 *     return new TencentSmsTemplate(
 *         props.getTencent().getSecretId(),
 *         props.getTencent().getSecretKey(),
 *         props.getTencent().getSdkAppId(),
 *         props.getDefaultSignName(),
 *         props.getTencent().getRegion());
 * }
 * }</pre>
 */
@AutoConfiguration
@EnableConfigurationProperties(SmsProperties.class)
@ConditionalOnProperty(prefix = "ikeu.sms", name = "enabled", havingValue = "true")
@Slf4j
public class SmsAutoConfiguration {
}
