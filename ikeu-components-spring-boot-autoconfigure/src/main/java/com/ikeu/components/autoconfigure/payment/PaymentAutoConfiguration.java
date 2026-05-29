package com.ikeu.components.autoconfigure.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Auto-configuration for payment gateways — binds {@link PaymentProperties}.
 * <p>
 * When {@code ikeu.payment.enabled=true}, properties are bound and available for
 * injection. To use a specific provider, add the corresponding SDK to your
 * project and define a {@code PaymentTemplate} bean:
 *
 * <h3>WeChat Pay</h3>
 * <pre>{@code
 * @Bean
 * public PaymentTemplate paymentTemplate(PaymentProperties props) {
 *     return new WechatPaymentTemplate(
 *         props.getWechat().getMerchantId(),
 *         props.getWechat().getPrivateKey(),
 *         props.getWechat().getMerchantSerialNumber(),
 *         props.getWechat().getApiV3Key(),
 *         props.getWechat().getAppId(),
 *         props.getWechat().getNotifyUrl());
 * }
 * }</pre>
 *
 * <h3>Alipay</h3>
 * <pre>{@code
 * @Bean
 * public PaymentTemplate paymentTemplate(PaymentProperties props) {
 *     return new AlipayPaymentTemplate(
 *         props.getAlipay().getAppId(),
 *         props.getAlipay().getPrivateKey(),
 *         props.getAlipay().getAlipayPublicKey(),
 *         props.getAlipay().getGatewayUrl(),
 *         props.getAlipay().getNotifyUrl());
 * }
 * }</pre>
 */
@AutoConfiguration
@EnableConfigurationProperties(PaymentProperties.class)
@ConditionalOnProperty(prefix = "ikeu.payment", name = "enabled", havingValue = "true")
@Slf4j
public class PaymentAutoConfiguration {
}
