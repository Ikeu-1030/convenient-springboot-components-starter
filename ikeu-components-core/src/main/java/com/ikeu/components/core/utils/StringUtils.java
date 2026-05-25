package com.ikeu.components.core.utils;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * String manipulation utilities.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * // Case conversion
 * StringUtils.camelToUnderline("userName");     // "user_name"
 * StringUtils.underlineToCamel("user_name");    // "userName"
 *
 * // UUID without dashes
 * StringUtils.uuid();  // "a1b2c3d4e5f6789012345678901234ab"
 *
 * // Random strings
 * StringUtils.randomNumeric(6);        // "384729"
 * StringUtils.randomAlphanumeric(16);  // "aB3xK9mP2qR7vY1w"
 *
 * // Validation
 * StringUtils.isMobile("13800138000");  // true (China mobile)
 * StringUtils.isEmail("a@b.com");       // true
 * StringUtils.isIdCard("110101199003071234");  // format check only, no checksum
 * }</pre>
 *
 * <h3>Caveats</h3>
 * <ul>
 *   <li>{@code camelToUnderline}: consecutive uppercase letters each get an underscore
 *       (e.g. "XMLParser" → "x_m_l_parser"). Use only for single-word camelCase
 *       like "userName" or "orderId".</li>
 *   <li>{@code isIdCard}: format check only (18 digits/X). Does NOT validate the
 *       checksum digit or date-of-birth portion. Use a dedicated ID card library
 *       for full validation.</li>
 * </ul>
 *
 * @author ikeu
 * @since 1.0.0
 */
public final class StringUtils {

    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.\\w+$");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^\\d{17}[\\dXx]$");

    private StringUtils() {
    }

    /**
     * Convert camelCase string to underline_case.
     */
    public static String camelToUnderline(String str) {
        if (str == null || str.isBlank()) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Convert underline_case string to camelCase.
     */
    public static String underlineToCamel(String str) {
        if (str == null || str.isBlank()) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        boolean upperNext = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '_') {
                upperNext = true;
            } else if (upperNext) {
                sb.append(Character.toUpperCase(c));
                upperNext = false;
            } else {
                sb.append(i == 0 ? Character.toLowerCase(c) : c);
            }
        }
        return sb.toString();
    }

    /**
     * Generate a UUID without dashes.
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Generate a random numeric string of given length.
     */
    public static String randomNumeric(int length) {
        if (length <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Generate a random alphanumeric string of given length.
     */
    public static String randomAlphanumeric(int length) {
        if (length <= 0) {
            return "";
        }
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static boolean isMobile(String str) {
        return str != null && MOBILE_PATTERN.matcher(str).matches();
    }

    public static boolean isEmail(String str) {
        return str != null && EMAIL_PATTERN.matcher(str).matches();
    }

    public static boolean isIdCard(String str) {
        return str != null && ID_CARD_PATTERN.matcher(str).matches();
    }
}
