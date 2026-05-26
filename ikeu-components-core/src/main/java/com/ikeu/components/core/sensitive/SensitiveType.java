package com.ikeu.components.core.sensitive;

/**
 * Predefined data masking strategies.
 */
public enum SensitiveType {

    /** Chinese name: keep first character, mask the rest. "张三" → "张*" */
    CHINESE_NAME,

    /** ID card: keep first 3 and last 4 digits. "110101199001011234" → "110***********1234" */
    ID_CARD,

    /** Phone number: keep first 3 and last 4 digits. "13812345678" → "138****5678" */
    PHONE,

    /** Email: keep first char before @ and full domain. "test@example.com" → "t***@example.com" */
    EMAIL,

    /** Bank card: keep first 4 and last 4 digits. "6222021234561234" → "6222********1234" */
    BANK_CARD,

    /** Address: keep first 6 characters, mask the rest. */
    ADDRESS,

    /** Password: mask all characters. */
    PASSWORD,

    /** Custom masking using startInclude/endInclude/maskChar from the annotation. */
    CUSTOM
}
