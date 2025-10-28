package com.honyrun.util.common;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/**
 * 日期工具类
 * 提供日期格式化、时区处理、日期计算
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  16:25:00
 * @modified 2025-07-01 16:25:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public final class DateUtil {

    private DateUtil() {
        // 工具类，禁止实例化
    }

    // ==================== 常用日期格式 ====================

    /**
     * 标准日期时间格式
     */
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 标准日期格式
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 标准时间格式
     */
    public static final String TIME_PATTERN = "HH:mm:ss";

    /**
     * 时间戳格式
     */
    public static final String TIMESTAMP_PATTERN = "yyyyMMddHHmmss";

    /**
     * 紧凑日期格式
     */
    public static final String COMPACT_DATE_PATTERN = "yyyyMMdd";

    /**
     * 紧凑时间格式
     */
    public static final String COMPACT_TIME_PATTERN = "HHmmss";

    /**
     * ISO日期时间格式
     */
    public static final String ISO_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * 中文日期格式
     */
    public static final String CHINESE_DATE_PATTERN = "yyyy年MM月dd日";

    /**
     * 中文日期时间格式
     */
    public static final String CHINESE_DATETIME_PATTERN = "yyyy年MM月dd日 HH时mm分ss秒";

    // ==================== 常用格式化器 ====================

    /**
     * 标准日期时间格式化器
     */
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    /**
     * 标准日期格式化器
     */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    /**
     * 标准时间格式化器
     */
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN);

    /**
     * 时间戳格式化器
     */
    public static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN);

    /**
     * 默认时区
     */
    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Asia/Shanghai");

    // ==================== 当前时间获取 ====================

    /**
     * 获取当前LocalDateTime
     *
     * @return 当前LocalDateTime
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 获取当前LocalDate
     *
     * @return 当前LocalDate
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * 获取当前LocalTime
     *
     * @return 当前LocalTime
     */
    public static LocalTime currentTime() {
        return LocalTime.now();
    }

    /**
     * 获取当前时间戳（毫秒）
     *
     * @return 当前时间戳
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 获取当前时间戳（秒）
     *
     * @return 当前时间戳（秒）
     */
    public static long currentTimeSeconds() {
        return Instant.now().getEpochSecond();
    }

    /**
     * 获取当前时间戳字符串
     *
     * @return 当前时间戳字符串（格式：yyyyMMddHHmmss）
     */
    public static String getCurrentTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }

    // ==================== 格式化方法 ====================

    /**
     * 格式化LocalDateTime为字符串
     *
     * @param dateTime LocalDateTime对象
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null || pattern == null) {
            return null;
        }
        try {
            return dateTime.format(DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 格式化LocalDateTime为标准格式字符串
     *
     * @param dateTime LocalDateTime对象
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime) {
        return format(dateTime, DATETIME_PATTERN);
    }

    /**
     * 格式化LocalDate为字符串
     *
     * @param date LocalDate对象
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String format(LocalDate date, String pattern) {
        if (date == null || pattern == null) {
            return null;
        }
        try {
            return date.format(DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 格式化LocalDate为标准格式字符串
     *
     * @param date LocalDate对象
     * @return 格式化后的字符串
     */
    public static String format(LocalDate date) {
        return format(date, DATE_PATTERN);
    }

    /**
     * 格式化LocalTime为字符串
     *
     * @param time LocalTime对象
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String format(LocalTime time, String pattern) {
        if (time == null || pattern == null) {
            return null;
        }
        try {
            return time.format(DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 格式化LocalTime为标准格式字符串
     *
     * @param time LocalTime对象
     * @return 格式化后的字符串
     */
    public static String format(LocalTime time) {
        return format(time, TIME_PATTERN);
    }

    // ==================== 解析方法 ====================

    /**
     * 解析字符串为LocalDateTime
     *
     * @param dateTimeStr 日期时间字符串
     * @param pattern 格式模式
     * @return LocalDateTime对象
     */
    public static LocalDateTime parseDateTime(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || pattern == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 解析字符串为LocalDateTime（标准格式）
     *
     * @param dateTimeStr 日期时间字符串
     * @return LocalDateTime对象
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return parseDateTime(dateTimeStr, DATETIME_PATTERN);
    }

    /**
     * 解析字符串为LocalDate
     *
     * @param dateStr 日期字符串
     * @param pattern 格式模式
     * @return LocalDate对象
     */
    public static LocalDate parseDate(String dateStr, String pattern) {
        if (dateStr == null || pattern == null) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 解析字符串为LocalDate（标准格式）
     *
     * @param dateStr 日期字符串
     * @return LocalDate对象
     */
    public static LocalDate parseDate(String dateStr) {
        return parseDate(dateStr, DATE_PATTERN);
    }

    /**
     * 解析字符串为LocalTime
     *
     * @param timeStr 时间字符串
     * @param pattern 格式模式
     * @return LocalTime对象
     */
    public static LocalTime parseTime(String timeStr, String pattern) {
        if (timeStr == null || pattern == null) {
            return null;
        }
        try {
            return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 解析字符串为LocalTime（标准格式）
     *
     * @param timeStr 时间字符串
     * @return LocalTime对象
     */
    public static LocalTime parseTime(String timeStr) {
        return parseTime(timeStr, TIME_PATTERN);
    }

    // ==================== 转换方法 ====================

    /**
     * LocalDateTime转换为Date
     *
     * @param localDateTime LocalDateTime对象
     * @return Date对象
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(DEFAULT_ZONE_ID).toInstant());
    }

    /**
     * Date转换为LocalDateTime
     *
     * @param date Date对象
     * @return LocalDateTime对象
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(DEFAULT_ZONE_ID).toLocalDateTime();
    }

    /**
     * LocalDateTime转换为时间戳（毫秒）
     *
     * @param localDateTime LocalDateTime对象
     * @return 时间戳（毫秒）
     */
    public static long toTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return 0;
        }
        return localDateTime.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli();
    }

    /**
     * 时间戳（毫秒）转换为LocalDateTime
     *
     * @param timestamp 时间戳（毫秒）
     * @return LocalDateTime对象
     */
    public static LocalDateTime fromTimestamp(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(DEFAULT_ZONE_ID).toLocalDateTime();
    }

    // ==================== 日期计算 ====================

    /**
     * 增加年数
     *
     * @param date 原日期
     * @param years 年数
     * @return 计算后的日期
     */
    public static LocalDate plusYears(LocalDate date, long years) {
        return date != null ? date.plusYears(years) : null;
    }

    /**
     * 增加月数
     *
     * @param date 原日期
     * @param months 月数
     * @return 计算后的日期
     */
    public static LocalDate plusMonths(LocalDate date, long months) {
        return date != null ? date.plusMonths(months) : null;
    }

    /**
     * 增加天数
     *
     * @param date 原日期
     * @param days 天数
     * @return 计算后的日期
     */
    public static LocalDate plusDays(LocalDate date, long days) {
        return date != null ? date.plusDays(days) : null;
    }

    /**
     * 增加小时数
     *
     * @param dateTime 原日期时间
     * @param hours 小时数
     * @return 计算后的日期时间
     */
    public static LocalDateTime plusHours(LocalDateTime dateTime, long hours) {
        return dateTime != null ? dateTime.plusHours(hours) : null;
    }

    /**
     * 增加分钟数
     *
     * @param dateTime 原日期时间
     * @param minutes 分钟数
     * @return 计算后的日期时间
     */
    public static LocalDateTime plusMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime != null ? dateTime.plusMinutes(minutes) : null;
    }

    /**
     * 增加秒数
     *
     * @param dateTime 原日期时间
     * @param seconds 秒数
     * @return 计算后的日期时间
     */
    public static LocalDateTime plusSeconds(LocalDateTime dateTime, long seconds) {
        return dateTime != null ? dateTime.plusSeconds(seconds) : null;
    }

    // ==================== 日期比较 ====================

    /**
     * 判断日期是否在指定范围内
     *
     * @param date 要判断的日期
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 是否在范围内
     */
    public static boolean isBetween(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (date == null || startDate == null || endDate == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * 判断日期时间是否在指定范围内
     *
     * @param dateTime 要判断的日期时间
     * @param startDateTime 开始日期时间
     * @param endDateTime 结束日期时间
     * @return 是否在范围内
     */
    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (dateTime == null || startDateTime == null || endDateTime == null) {
            return false;
        }
        return !dateTime.isBefore(startDateTime) && !dateTime.isAfter(endDateTime);
    }

    /**
     * 计算两个日期之间的天数差
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 天数差
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * 计算两个日期时间之间的小时差
     *
     * @param startDateTime 开始日期时间
     * @param endDateTime 结束日期时间
     * @return 小时差
     */
    public static long hoursBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(startDateTime, endDateTime);
    }

    /**
     * 计算两个日期时间之间的分钟差
     *
     * @param startDateTime 开始日期时间
     * @param endDateTime 结束日期时间
     * @return 分钟差
     */
    public static long minutesBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(startDateTime, endDateTime);
    }

    // ==================== 特殊日期获取 ====================

    /**
     * 获取月初日期
     *
     * @param date 指定日期
     * @return 月初日期
     */
    public static LocalDate getFirstDayOfMonth(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.firstDayOfMonth()) : null;
    }

    /**
     * 获取月末日期
     *
     * @param date 指定日期
     * @return 月末日期
     */
    public static LocalDate getLastDayOfMonth(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.lastDayOfMonth()) : null;
    }

    /**
     * 获取年初日期
     *
     * @param date 指定日期
     * @return 年初日期
     */
    public static LocalDate getFirstDayOfYear(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.firstDayOfYear()) : null;
    }

    /**
     * 获取年末日期
     *
     * @param date 指定日期
     * @return 年末日期
     */
    public static LocalDate getLastDayOfYear(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.lastDayOfYear()) : null;
    }

    /**
     * 获取周一日期
     *
     * @param date 指定日期
     * @return 周一日期
     */
    public static LocalDate getMonday(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) : null;
    }

    /**
     * 获取周日日期
     *
     * @param date 指定日期
     * @return 周日日期
     */
    public static LocalDate getSunday(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)) : null;
    }

    // ==================== 验证方法 ====================

    /**
     * 验证日期字符串格式是否正确
     *
     * @param dateStr 日期字符串
     * @param pattern 格式模式
     * @return 是否格式正确
     */
    public static boolean isValidDate(String dateStr, String pattern) {
        if (dateStr == null || pattern == null) {
            return false;
        }
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * 验证日期时间字符串格式是否正确
     *
     * @param dateTimeStr 日期时间字符串
     * @param pattern 格式模式
     * @return 是否格式正确
     */
    public static boolean isValidDateTime(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || pattern == null) {
            return false;
        }
        try {
            LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * 判断是否为闰年
     *
     * @param year 年份
     * @return 是否为闰年
     */
    public static boolean isLeapYear(int year) {
        return Year.isLeap(year);
    }

    /**
     * 判断日期是否为今天
     *
     * @param date 日期
     * @return 是否为今天
     */
    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(LocalDate.now());
    }

    /**
     * 判断日期是否为昨天
     *
     * @param date 日期
     * @return 是否为昨天
     */
    public static boolean isYesterday(LocalDate date) {
        return date != null && date.equals(LocalDate.now().minusDays(1));
    }

    /**
     * 判断日期是否为明天
     *
     * @param date 日期
     * @return 是否为明天
     */
    public static boolean isTomorrow(LocalDate date) {
        return date != null && date.equals(LocalDate.now().plusDays(1));
    }
}


