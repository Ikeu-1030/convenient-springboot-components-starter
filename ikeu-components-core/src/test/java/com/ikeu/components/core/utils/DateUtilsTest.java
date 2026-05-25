package com.ikeu.components.core.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    // ── Formatting ──

    @Test
    void format_defaultPattern() {
        LocalDateTime dt = LocalDateTime.of(2026, 5, 25, 15, 30, 0);
        assertEquals("2026-05-25 15:30:00", DateUtils.format(dt));
    }

    @Test
    void format_customPattern() {
        LocalDateTime dt = LocalDateTime.of(2026, 5, 25, 15, 30, 0);
        assertEquals("2026/05/25", DateUtils.format(dt, "yyyy/MM/dd"));
    }

    @Test
    void format_nullDateTime() {
        assertNull(DateUtils.format(null));
    }

    @Test
    void formatDate_defaultPattern() {
        LocalDate d = LocalDate.of(2026, 5, 25);
        assertEquals("2026-05-25", DateUtils.formatDate(d));
    }

    @Test
    void formatDate_customPattern() {
        LocalDate d = LocalDate.of(2026, 5, 25);
        assertEquals("20260525", DateUtils.formatDate(d, "yyyyMMdd"));
    }

    @Test
    void formatDate_null() {
        assertNull(DateUtils.formatDate(null));
    }

    @Test
    void formatTime_basic() {
        LocalTime t = LocalTime.of(15, 30, 45);
        assertEquals("15:30:45", DateUtils.formatTime(t));
    }

    @Test
    void formatTime_null() {
        assertNull(DateUtils.formatTime(null));
    }

    // ── Parsing ──

    @Test
    void parse_defaultPattern() {
        LocalDateTime dt = DateUtils.parse("2026-05-25 15:30:00");
        assertEquals(LocalDateTime.of(2026, 5, 25, 15, 30, 0), dt);
    }

    @Test
    void parse_customPattern() {
        LocalDateTime dt = DateUtils.parse("2026/05/25 15:30", "yyyy/MM/dd HH:mm");
        assertEquals(LocalDateTime.of(2026, 5, 25, 15, 30, 0), dt);
    }

    @Test
    void parse_nullString() {
        assertNull(DateUtils.parse(null));
    }

    @Test
    void parse_blankString() {
        assertNull(DateUtils.parse("   "));
    }

    @Test
    void parseDate_defaultPattern() {
        LocalDate d = DateUtils.parseDate("2026-05-25");
        assertEquals(LocalDate.of(2026, 5, 25), d);
    }

    @Test
    void parseDate_null() {
        assertNull(DateUtils.parseDate(null));
    }

    @Test
    void parseTime_basic() {
        LocalTime t = DateUtils.parseTime("15:30:45");
        assertEquals(LocalTime.of(15, 30, 45), t);
    }

    // ── Format conversion ──

    @Test
    void convertFormat_basic() {
        String result = DateUtils.convertFormat("2026-05-25", "yyyy-MM-dd", "yyyyMMdd");
        assertEquals("20260525", result);
    }

    @Test
    void convertFormat_nullInput() {
        assertNull(DateUtils.convertFormat(null, "yyyy-MM-dd", "yyyyMMdd"));
    }

    @Test
    void convertDateTimeFormat_basic() {
        String result = DateUtils.convertDateTimeFormat(
                "2026-05-25 15:30:00", "yyyy-MM-dd HH:mm:ss", "yyyyMMddHHmmss");
        assertEquals("20260525153000", result);
    }

    // ── Day boundaries ──

    @Test
    void startOfDay_LocalDateTime() {
        LocalDateTime dt = LocalDateTime.of(2026, 5, 25, 15, 30, 0);
        assertEquals(LocalDateTime.of(2026, 5, 25, 0, 0, 0), DateUtils.startOfDay(dt));
    }

    @Test
    void endOfDay_LocalDateTime() {
        LocalDateTime dt = LocalDateTime.of(2026, 5, 25, 15, 30, 0);
        assertEquals(LocalDateTime.of(2026, 5, 25, 23, 59, 59, 999_999_999),
                DateUtils.endOfDay(dt));
    }

    @Test
    void startOfDay_LocalDate() {
        LocalDate d = LocalDate.of(2026, 5, 25);
        assertEquals(LocalDateTime.of(2026, 5, 25, 0, 0, 0), DateUtils.startOfDay(d));
    }

    @Test
    void endOfDay_LocalDate() {
        LocalDate d = LocalDate.of(2026, 5, 25);
        assertEquals(LocalDateTime.of(2026, 5, 25, 23, 59, 59, 999_999_999),
                DateUtils.endOfDay(d));
    }

    @Test
    void startOfDay_null() {
        assertNull(DateUtils.startOfDay((LocalDateTime) null));
        assertNull(DateUtils.startOfDay((LocalDate) null));
    }

    // ── Month boundaries ──

    @Test
    void startOfMonth() {
        LocalDateTime dt = LocalDateTime.of(2026, 5, 15, 12, 0, 0);
        assertEquals(LocalDateTime.of(2026, 5, 1, 0, 0, 0),
                DateUtils.startOfMonth(dt));
    }

    @Test
    void endOfMonth() {
        LocalDateTime dt = LocalDateTime.of(2026, 3, 15, 12, 0, 0);
        assertEquals(LocalDateTime.of(2026, 3, 31, 23, 59, 59, 999_999_999),
                DateUtils.endOfMonth(dt));
    }

    // ── Arithmetic ──

    @Test
    void addDays_positive() {
        LocalDateTime dt = LocalDateTime.of(2026, 5, 25, 10, 0, 0);
        assertEquals(LocalDateTime.of(2026, 5, 26, 10, 0, 0), DateUtils.addDays(dt, 1));
    }

    @Test
    void addDays_negative() {
        LocalDateTime dt = LocalDateTime.of(2026, 5, 25, 10, 0, 0);
        assertEquals(LocalDateTime.of(2026, 5, 24, 10, 0, 0), DateUtils.addDays(dt, -1));
    }

    @Test
    void addHours() {
        LocalDateTime dt = LocalDateTime.of(2026, 5, 25, 10, 0, 0);
        assertEquals(LocalDateTime.of(2026, 5, 25, 13, 0, 0), DateUtils.addHours(dt, 3));
    }

    @Test
    void addMinutes() {
        LocalDateTime dt = LocalDateTime.of(2026, 5, 25, 10, 0, 0);
        assertEquals(LocalDateTime.of(2026, 5, 25, 10, 30, 0), DateUtils.addMinutes(dt, 30));
    }

    @Test
    void addSeconds() {
        LocalDateTime dt = LocalDateTime.of(2026, 5, 25, 10, 0, 0);
        assertEquals(LocalDateTime.of(2026, 5, 25, 10, 0, 45), DateUtils.addSeconds(dt, 45));
    }

    @Test
    void addMonths() {
        LocalDateTime dt = LocalDateTime.of(2026, 1, 31, 10, 0, 0);
        assertEquals(LocalDateTime.of(2026, 2, 28, 10, 0, 0), DateUtils.addMonths(dt, 1));
    }

    @Test
    void addWeeks() {
        LocalDateTime dt = LocalDateTime.of(2026, 5, 25, 10, 0, 0);
        assertEquals(LocalDateTime.of(2026, 6, 1, 10, 0, 0), DateUtils.addWeeks(dt, 1));
    }

    @Test
    void addYears() {
        LocalDateTime dt = LocalDateTime.of(2026, 5, 25, 10, 0, 0);
        assertEquals(LocalDateTime.of(2027, 5, 25, 10, 0, 0), DateUtils.addYears(dt, 1));
    }

    @Test
    void addDays_null() {
        assertNull(DateUtils.addDays((LocalDateTime) null, 1));
    }

    @Test
    void addDays_LocalDate() {
        LocalDate d = LocalDate.of(2026, 5, 25);
        assertEquals(LocalDate.of(2026, 5, 27), DateUtils.addDays(d, 2));
    }

    // ── Difference ──

    @Test
    void daysBetween() {
        LocalDate start = LocalDate.of(2026, 5, 20);
        LocalDate end = LocalDate.of(2026, 5, 25);
        assertEquals(5, DateUtils.daysBetween(start, end));
    }

    @Test
    void daysBetween_nullStart() {
        assertEquals(0, DateUtils.daysBetween(null, LocalDate.now()));
    }

    @Test
    void daysBetween_nullEnd() {
        assertEquals(0, DateUtils.daysBetween(LocalDate.now(), null));
    }

    @Test
    void hoursBetween() {
        LocalDateTime start = LocalDateTime.of(2026, 5, 25, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 25, 14, 0, 0);
        assertEquals(4, DateUtils.hoursBetween(start, end));
    }

    @Test
    void minutesBetween() {
        LocalDateTime start = LocalDateTime.of(2026, 5, 25, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 25, 10, 30, 0);
        assertEquals(30, DateUtils.minutesBetween(start, end));
    }

    @Test
    void monthsBetween() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 1, 0, 0, 0);
        assertEquals(5, DateUtils.monthsBetween(start, end));
    }

    // ── Epoch conversion ──

    @Test
    void epochMilliRoundTrip() {
        LocalDateTime original = LocalDateTime.of(2026, 5, 25, 15, 30, 0);
        long millis = DateUtils.toEpochMilli(original);
        LocalDateTime restored = DateUtils.fromEpochMilli(millis);
        assertEquals(original, restored);
    }

    @Test
    void epochSecondRoundTrip() {
        LocalDateTime original = LocalDateTime.of(2026, 5, 25, 15, 30, 0);
        long seconds = DateUtils.toEpochSecond(original);
        LocalDateTime restored = DateUtils.fromEpochSecond(seconds);
        assertEquals(original, restored);
    }

    @Test
    void toEpochMilli_null() {
        assertEquals(0, DateUtils.toEpochMilli(null));
    }

    @Test
    void toEpochSecond_null() {
        assertEquals(0, DateUtils.toEpochSecond(null));
    }

    // ── Legacy Date conversion ──

    @Test
    void toDate_fromLocalDateTime() {
        LocalDateTime ldt = LocalDateTime.of(2026, 5, 25, 15, 30, 0);
        Date date = DateUtils.toDate(ldt);
        assertNotNull(date);
        LocalDateTime restored = DateUtils.toLocalDateTime(date);
        assertEquals(ldt, restored);
    }

    @Test
    void toDate_fromLocalDate() {
        LocalDate ld = LocalDate.of(2026, 5, 25);
        Date date = DateUtils.toDate(ld);
        assertNotNull(date);
    }

    @Test
    void toLocalDateTime_null() {
        assertNull(DateUtils.toLocalDateTime(null));
    }

    @Test
    void toDate_null() {
        assertNull(DateUtils.toDate((LocalDateTime) null));
    }

    // ── Overlap & between ──

    @Test
    void isOverlap_overlapping() {
        LocalDateTime s1 = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
        LocalDateTime e1 = LocalDateTime.of(2026, 1, 10, 0, 0, 0);
        LocalDateTime s2 = LocalDateTime.of(2026, 1, 5, 0, 0, 0);
        LocalDateTime e2 = LocalDateTime.of(2026, 1, 15, 0, 0, 0);
        assertTrue(DateUtils.isOverlap(s1, e1, s2, e2));
    }

    @Test
    void isOverlap_nonOverlapping() {
        LocalDateTime s1 = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
        LocalDateTime e1 = LocalDateTime.of(2026, 1, 5, 0, 0, 0);
        LocalDateTime s2 = LocalDateTime.of(2026, 1, 6, 0, 0, 0);
        LocalDateTime e2 = LocalDateTime.of(2026, 1, 10, 0, 0, 0);
        assertFalse(DateUtils.isOverlap(s1, e1, s2, e2));
    }

    @Test
    void isOverlap_nullInput() {
        LocalDateTime t = LocalDateTime.now();
        assertFalse(DateUtils.isOverlap(null, t, t, t));
        assertFalse(DateUtils.isOverlap(t, null, t, t));
    }

    @Test
    void isBetween_inRange() {
        LocalDateTime target = LocalDateTime.of(2026, 5, 15, 0, 0, 0);
        LocalDateTime start = LocalDateTime.of(2026, 5, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 31, 0, 0, 0);
        assertTrue(DateUtils.isBetween(target, start, end));
    }

    @Test
    void isBetween_outOfRange() {
        LocalDateTime target = LocalDateTime.of(2026, 6, 1, 0, 0, 0);
        LocalDateTime start = LocalDateTime.of(2026, 5, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 31, 0, 0, 0);
        assertFalse(DateUtils.isBetween(target, start, end));
    }

    @Test
    void isBetween_nullInput() {
        LocalDateTime t = LocalDateTime.now();
        assertFalse(DateUtils.isBetween(null, t, t));
    }

    // ── Current time ──

    @Test
    void today_returnsToday() {
        assertEquals(LocalDate.now(), DateUtils.today());
    }

    @Test
    void now_returnsCurrentTime() {
        LocalDateTime before = LocalDateTime.now();
        LocalDateTime now = DateUtils.now();
        LocalDateTime after = LocalDateTime.now();
        assertFalse(now.isBefore(before.minusSeconds(1)));
        assertFalse(now.isAfter(after.plusSeconds(1)));
    }

    @Test
    void nowStr_returnsFormatted() {
        String result = DateUtils.nowStr();
        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void todayStr_returnsFormatted() {
        String result = DateUtils.todayStr();
        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"));
    }
}