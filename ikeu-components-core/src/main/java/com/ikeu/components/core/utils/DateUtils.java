package com.ikeu.components.core.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/**
 * Thread-safe date/time utilities using the java.time API.
 * <p>
 * Say goodbye to {@code SimpleDateFormat} thread-safety issues.
 * All methods are stateless and accept/return immutable {@link java.time.LocalDateTime}
 * and {@link java.time.LocalDate} types. Uses {@link java.time.format.DateTimeFormatter}
 * (thread-safe) internally.
 *
 * <h3>Predefined patterns</h3>
 * <table>
 *   <tr><td>{@link #PATTERN_DATE}</td><td>{@code yyyy-MM-dd}</td></tr>
 *   <tr><td>{@link #PATTERN_DATETIME}</td><td>{@code yyyy-MM-dd HH:mm:ss}</td></tr>
 *   <tr><td>{@link #PATTERN_DATETIME_MS}</td><td>{@code yyyy-MM-dd HH:mm:ss.SSS}</td></tr>
 *   <tr><td>{@link #PATTERN_DATE_COMPACT}</td><td>{@code yyyyMMdd}</td></tr>
 * </table>
 *
 * <h3>Usage examples</h3>
 * <pre>{@code
 * // Format
 * String now = DateUtils.format(LocalDateTime.now());       // "2026-05-25 15:30:00"
 * String today = DateUtils.formatDate(LocalDate.now());     // "2026-05-25"
 *
 * // Parse
 * LocalDateTime dt = DateUtils.parse("2026-05-25 15:30:00");
 *
 * // Day boundaries
 * LocalDateTime start = DateUtils.startOfDay(LocalDate.now()); // 00:00:00
 * LocalDateTime end = DateUtils.endOfDay(LocalDate.now());     // 23:59:59
 *
 * // Arithmetic (negative values subtract)
 * LocalDateTime tomorrow = DateUtils.addDays(LocalDateTime.now(), 1);
 *
 * // Overlap check
 * boolean overlap = DateUtils.isOverlap(start1, end1, start2, end2);
 *
 * // Epoch conversion (system default timezone)
 * long millis = DateUtils.toEpochMilli(LocalDateTime.now());
 *
 * // Legacy Date conversion
 * Date legacy = DateUtils.toDate(LocalDateTime.now());
 *
 * // Format conversion
 * DateUtils.convertFormat("2026-05-25", PATTERN_DATE, PATTERN_DATE_COMPACT); // "20260525"
 * }</pre>
 *
 * <h3>Caveats</h3>
 * <ul>
 *   <li>Epoch methods use {@link java.time.ZoneId#systemDefault()}</li>
 *   <li>Null-safe: formatting/parsing returns null on null input;
 *       difference methods return 0</li>
 *   <li>Blank string input returns null on parse</li>
 * </ul>
 *
 * @author ikeu
 * @since 1.0.0
 */
public final class DateUtils {

    public static final String PATTERN_DATE = "yyyy-MM-dd";
    public static final String PATTERN_DATETIME = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_DATETIME_MS = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String PATTERN_DATE_COMPACT = "yyyyMMdd";

    private static final ZoneId ZONE_DEFAULT = ZoneId.systemDefault();

    private DateUtils() {
    }

    // ──────────────────────────────────────────────
    // Formatting
    // ──────────────────────────────────────────────

    /** Format with default datetime pattern "yyyy-MM-dd HH:mm:ss" */
    public static String format(LocalDateTime dateTime) {
        return format(dateTime, PATTERN_DATETIME);
    }

    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /** Format with default date pattern "yyyy-MM-dd" */
    public static String formatDate(LocalDate date) {
        return formatDate(date, PATTERN_DATE);
    }

    public static String formatDate(LocalDate date, String pattern) {
        if (date == null) {
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String formatTime(LocalTime time) {
        if (time == null) {
            return null;
        }
        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    // ──────────────────────────────────────────────
    // Parsing
    // ──────────────────────────────────────────────

    /** Parse with default datetime pattern "yyyy-MM-dd HH:mm:ss" */
    public static LocalDateTime parse(String str) {
        return parse(str, PATTERN_DATETIME);
    }

    public static LocalDateTime parse(String str, String pattern) {
        if (str == null || str.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(str, DateTimeFormatter.ofPattern(pattern));
    }

    /** Parse with default date pattern "yyyy-MM-dd" */
    public static LocalDate parseDate(String str) {
        return parseDate(str, PATTERN_DATE);
    }

    public static LocalDate parseDate(String str, String pattern) {
        if (str == null || str.isBlank()) {
            return null;
        }
        return LocalDate.parse(str, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalTime parseTime(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        return LocalTime.parse(str, DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    // ──────────────────────────────────────────────
    // Format conversion (e.g. "yyyy-MM-dd" → "yyyyMMdd")
    // ──────────────────────────────────────────────

    /**
     * Convert a date string from one pattern to another.
     * @param dateStr  the original date string
     * @param fromPattern  current format
     * @param toPattern  target format
     * @return formatted string, or null if input is blank
     */
    public static String convertFormat(String dateStr, String fromPattern, String toPattern) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        LocalDate date = parseDate(dateStr, fromPattern);
        return date != null ? formatDate(date, toPattern) : null;
    }

    /**
     * Convert a datetime string from one pattern to another.
     */
    public static String convertDateTimeFormat(String dateTimeStr, String fromPattern,
                                               String toPattern) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return null;
        }
        LocalDateTime dateTime = parse(dateTimeStr, fromPattern);
        return dateTime != null ? format(dateTime, toPattern) : null;
    }

    // ──────────────────────────────────────────────
    // Day boundaries
    // ──────────────────────────────────────────────

    public static LocalDateTime startOfDay(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDate().atStartOfDay() : null;
    }

    public static LocalDateTime startOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    public static LocalDateTime endOfDay(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDate().atTime(LocalTime.MAX) : null;
    }

    public static LocalDateTime endOfDay(LocalDate date) {
        return date != null ? date.atTime(LocalTime.MAX) : null;
    }

    // ──────────────────────────────────────────────
    // Month boundaries
    // ──────────────────────────────────────────────

    public static LocalDateTime startOfMonth(LocalDateTime dateTime) {
        return dateTime != null
                ? dateTime.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay()
                : null;
    }

    public static LocalDateTime endOfMonth(LocalDateTime dateTime) {
        return dateTime != null
                ? dateTime.with(TemporalAdjusters.lastDayOfMonth()).toLocalDate().atTime(LocalTime.MAX)
                : null;
    }

    // ──────────────────────────────────────────────
    // Arithmetic
    // ──────────────────────────────────────────────

    public static LocalDateTime addDays(LocalDateTime dateTime, long days) {
        return dateTime != null ? dateTime.plusDays(days) : null;
    }

    public static LocalDateTime addHours(LocalDateTime dateTime, long hours) {
        return dateTime != null ? dateTime.plusHours(hours) : null;
    }

    public static LocalDateTime addMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime != null ? dateTime.plusMinutes(minutes) : null;
    }

    public static LocalDateTime addSeconds(LocalDateTime dateTime, long seconds) {
        return dateTime != null ? dateTime.plusSeconds(seconds) : null;
    }

    public static LocalDateTime addMonths(LocalDateTime dateTime, long months) {
        return dateTime != null ? dateTime.plusMonths(months) : null;
    }

    public static LocalDateTime addWeeks(LocalDateTime dateTime, long weeks) {
        return dateTime != null ? dateTime.plusWeeks(weeks) : null;
    }

    public static LocalDateTime addYears(LocalDateTime dateTime, long years) {
        return dateTime != null ? dateTime.plusYears(years) : null;
    }

    public static LocalDate addDays(LocalDate date, long days) {
        return date != null ? date.plusDays(days) : null;
    }

    // ──────────────────────────────────────────────
    // Difference between dates
    // ──────────────────────────────────────────────

    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
    }

    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start, end);
    }

    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(start, end);
    }

    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(start, end);
    }

    public static long monthsBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.MONTHS.between(start.toLocalDate(), end.toLocalDate());
    }

    // ──────────────────────────────────────────────
    // Epoch millis conversion
    // ──────────────────────────────────────────────

    /** Convert epoch milliseconds to LocalDateTime using system default timezone. */
    public static LocalDateTime fromEpochMilli(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZONE_DEFAULT);
    }

    /** Convert LocalDateTime to epoch milliseconds using system default timezone. */
    public static long toEpochMilli(LocalDateTime dateTime) {
        if (dateTime == null) {
            return 0;
        }
        return dateTime.atZone(ZONE_DEFAULT).toInstant().toEpochMilli();
    }

    /** Convert epoch seconds to LocalDateTime using system default timezone. */
    public static LocalDateTime fromEpochSecond(long epochSecond) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), ZONE_DEFAULT);
    }

    /** Convert LocalDateTime to epoch seconds using system default timezone. */
    public static long toEpochSecond(LocalDateTime dateTime) {
        if (dateTime == null) {
            return 0;
        }
        return dateTime.atZone(ZONE_DEFAULT).toEpochSecond();
    }

    // ──────────────────────────────────────────────
    // Legacy Date conversion
    // ──────────────────────────────────────────────

    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZONE_DEFAULT);
    }

    public static Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.atZone(ZONE_DEFAULT).toInstant());
    }

    public static Date toDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return Date.from(date.atStartOfDay(ZONE_DEFAULT).toInstant());
    }

    // ──────────────────────────────────────────────
    // Overlap check
    // ──────────────────────────────────────────────

    /**
     * Check if two time periods overlap.
     */
    public static boolean isOverlap(LocalDateTime start1, LocalDateTime end1,
                                    LocalDateTime start2, LocalDateTime end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * Check if a target dateTime is within [start, end] (inclusive).
     */
    public static boolean isBetween(LocalDateTime target, LocalDateTime start,
                                    LocalDateTime end) {
        if (target == null || start == null || end == null) {
            return false;
        }
        return !target.isBefore(start) && !target.isAfter(end);
    }

    // ──────────────────────────────────────────────
    // Current time
    // ──────────────────────────────────────────────

    public static LocalDate today() {
        return LocalDate.now();
    }

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    public static String nowStr() {
        return format(now());
    }

    public static String todayStr() {
        return formatDate(today());
    }
}