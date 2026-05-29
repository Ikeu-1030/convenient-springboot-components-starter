package com.ikeu.components.sms;

import com.ikeu.components.sms.exception.SmsException;

import java.util.List;
import java.util.Map;

/**
 * SMS template abstraction for sending template-based messages.
 * <p>
 * Implementations handle vendor-specific API calls and translate
 * errors into {@link SmsException}.
 */
public interface SmsTemplate {

    /**
     * Send a template SMS to a single phone number.
     *
     * @param phoneNumber    target phone number (E.164 format recommended)
     * @param templateCode   vendor template code
     * @param templateParams template variable values keyed by placeholder name
     * @throws SmsException on send failure
     */
    void send(String phoneNumber, String templateCode, Map<String, String> templateParams);

    /**
     * Send a template SMS to multiple phone numbers in one batch.
     *
     * @param phoneNumbers   target phone numbers
     * @param templateCode   vendor template code
     * @param templateParams template variable values shared across all recipients
     * @throws SmsException on send failure
     */
    void sendBatch(List<String> phoneNumbers, String templateCode, Map<String, String> templateParams);
}
