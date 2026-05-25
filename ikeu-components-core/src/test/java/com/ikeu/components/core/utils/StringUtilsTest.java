package com.ikeu.components.core.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    void camelToUnderline_basic() {
        assertEquals("user_name", StringUtils.camelToUnderline("userName"));
    }

    @Test
    void camelToUnderline_singleWord() {
        assertEquals("name", StringUtils.camelToUnderline("name"));
    }

    @Test
    void camelToUnderline_alreadyUnderline() {
        assertEquals("user_name", StringUtils.camelToUnderline("user_name"));
    }

    @Test
    void camelToUnderline_nullInput() {
        assertNull(StringUtils.camelToUnderline(null));
    }

    @Test
    void camelToUnderline_blankInput() {
        assertEquals("", StringUtils.camelToUnderline(""));
    }

    @Test
    void underlineToCamel_basic() {
        assertEquals("userName", StringUtils.underlineToCamel("user_name"));
    }

    @Test
    void underlineToCamel_singleWord() {
        assertEquals("name", StringUtils.underlineToCamel("name"));
    }

    @Test
    void underlineToCamel_nullInput() {
        assertNull(StringUtils.underlineToCamel(null));
    }

    @Test
    void underlineToCamel_blankInput() {
        assertEquals("", StringUtils.underlineToCamel(""));
    }

    @Test
    void uuid_noDashes_32Chars() {
        String uuid = StringUtils.uuid();
        assertEquals(32, uuid.length());
        assertFalse(uuid.contains("-"));
    }

    @Test
    void uuid_uniquePerCall() {
        String uuid1 = StringUtils.uuid();
        String uuid2 = StringUtils.uuid();
        assertNotEquals(uuid1, uuid2);
    }

    @Test
    void randomNumeric_correctLength() {
        String result = StringUtils.randomNumeric(8);
        assertEquals(8, result.length());
        assertTrue(result.matches("\\d{8}"));
    }

    @Test
    void randomNumeric_zeroLength() {
        assertEquals("", StringUtils.randomNumeric(0));
    }

    @Test
    void randomNumeric_negativeLength() {
        assertEquals("", StringUtils.randomNumeric(-1));
    }

    @Test
    void randomAlphanumeric_correctLength() {
        String result = StringUtils.randomAlphanumeric(16);
        assertEquals(16, result.length());
        assertTrue(result.matches("[A-Za-z0-9]{16}"));
    }

    @Test
    void randomAlphanumeric_zeroLength() {
        assertEquals("", StringUtils.randomAlphanumeric(0));
    }

    @Test
    void isMobile_valid() {
        assertTrue(StringUtils.isMobile("13800138000"));
        assertTrue(StringUtils.isMobile("15912345678"));
    }

    @Test
    void isMobile_invalid() {
        assertFalse(StringUtils.isMobile("12345678901"));
        assertFalse(StringUtils.isMobile("1380013800"));
        assertFalse(StringUtils.isMobile("abc"));
    }

    @Test
    void isMobile_null() {
        assertFalse(StringUtils.isMobile(null));
    }

    @Test
    void isEmail_valid() {
        assertTrue(StringUtils.isEmail("test@example.com"));
        assertTrue(StringUtils.isEmail("a.b@c.co"));
    }

    @Test
    void isEmail_invalid() {
        assertFalse(StringUtils.isEmail("not-email"));
        assertFalse(StringUtils.isEmail(""));
    }

    @Test
    void isEmail_null() {
        assertFalse(StringUtils.isEmail(null));
    }

    @Test
    void isIdCard_validFormat() {
        assertTrue(StringUtils.isIdCard("110101199003071234"));
        assertTrue(StringUtils.isIdCard("11010119900307123X"));
        assertTrue(StringUtils.isIdCard("11010119900307123x"));
    }

    @Test
    void isIdCard_invalidFormat() {
        assertFalse(StringUtils.isIdCard("12345"));
        assertFalse(StringUtils.isIdCard("11010119900307123Y"));
    }

    @Test
    void isIdCard_null() {
        assertFalse(StringUtils.isIdCard(null));
    }
}