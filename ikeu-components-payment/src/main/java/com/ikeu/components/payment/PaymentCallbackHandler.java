package com.ikeu.components.payment;

import com.ikeu.components.payment.model.PaymentResult;
import lombok.extern.slf4j.Slf4j;

/**
 * Template-method handler for payment gateway callbacks.
 * <p>
 * Business code extends this class, implements {@link #handleSuccess} and
 * {@link #handleFailure}, and calls {@link #handle(String, String)} from the
 * callback endpoint. Signature verification and response parsing are done
 * by the injected {@link PaymentTemplate} before delegating to the subclass.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * @RestController
 * public class PayNotifyController {
 *     @Autowired
 *     private PaymentCallbackHandler callbackHandler;
 *
 *     @PostMapping("/notify/wechat")
 *     public String notify(HttpServletRequest request) {
 *         String body = ...;           // read request body
 *         String signature = ...;      // read signature header
 *         return callbackHandler.handle(body, signature);
 *     }
 * }
 * }</pre>
 */
@Slf4j
public abstract class PaymentCallbackHandler {

    private final PaymentTemplate paymentTemplate;

    protected PaymentCallbackHandler(PaymentTemplate paymentTemplate) {
        this.paymentTemplate = paymentTemplate;
    }

    /**
     * Verify signature, parse the result, and dispatch to business handlers.
     *
     * @param callbackData raw callback body (JSON / form)
     * @param signature    signature from the callback header
     * @return a response string for the payment gateway (e.g. "SUCCESS" for WeChat, "success" for Alipay)
     */
    public String handle(String callbackData, String signature) {
        if (!paymentTemplate.verifyCallback(callbackData, signature)) {
            log.warn("Payment callback signature verification failed");
            return failResponse();
        }
        try {
            PaymentResult result = parseCallback(callbackData);
            if (result.isSuccess()) {
                handleSuccess(result);
            } else {
                handleFailure(result);
            }
        } catch (Exception e) {
            log.error("Error processing payment callback", e);
            handleException(e);
            return failResponse();
        }
        return successResponse();
    }

    /**
     * Parse the raw callback body into a {@link PaymentResult}.
     * Override if the default parsing is insufficient.
     */
    protected abstract PaymentResult parseCallback(String callbackData);

    /** Called when the payment gateway reports a successful payment. */
    protected abstract void handleSuccess(PaymentResult result);

    /** Called when the payment status is anything other than success. */
    protected abstract void handleFailure(PaymentResult result);

    /** Called when an unexpected exception occurs during callback processing. */
    protected void handleException(Exception e) {
        log.error("Unhandled callback exception", e);
    }

    /** Returned to the gateway on success. Override to customize. */
    protected String successResponse() {
        return "SUCCESS";
    }

    /** Returned to the gateway on failure. Override to customize. */
    protected String failResponse() {
        return "FAIL";
    }
}
