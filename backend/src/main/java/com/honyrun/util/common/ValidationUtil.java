package com.honyrun.util.common;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 验证工具类
 * 提供参数验证、格式检查、业务规则验证
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  16:45:00
 * @modified 2025-07-01 16:45:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public final class ValidationUtil {

    private ValidationUtil() {
        // 工具类，禁止实例化
    }

    // ==================== 基础验证方法 ====================

    /**
     * 验证对象是否不为null
     *
     * @param obj 对象
     * @param message 错误消息
     * @throws IllegalArgumentException 如果对象为null
     */
    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证字符串是否不为空
     *
     * @param str 字符串
     * @param message 错误消息
     * @throws IllegalArgumentException 如果字符串为空
     */
    public static void notEmpty(String str, String message) {
        if (StringUtil.isEmpty(str)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证字符串是否不为空白
     *
     * @param str 字符串
     * @param message 错误消息
     * @throws IllegalArgumentException 如果字符串为空白
     */
    public static void notBlank(String str, String message) {
        if (StringUtil.isBlank(str)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证集合是否不为空
     *
     * @param collection 集合
     * @param message 错误消息
     * @throws IllegalArgumentException 如果集合为空
     */
    public static void notEmpty(Collection<?> collection, String message) {
        if (CollectionUtil.isEmpty(collection)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证Map是否不为空
     *
     * @param map Map对象
     * @param message 错误消息
     * @throws IllegalArgumentException 如果Map为空
     */
    public static void notEmpty(Map<?, ?> map, String message) {
        if (CollectionUtil.isEmpty(map)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证数组是否不为空
     *
     * @param array 数组
     * @param message 错误消息
     * @throws IllegalArgumentException 如果数组为空
     */
    public static void notEmpty(Object[] array, String message) {
        if (CollectionUtil.isEmpty(array)) {
            throw new IllegalArgumentException(message);
        }
    }

    // ==================== 条件验证方法 ====================

    /**
     * 验证条件是否为真
     *
     * @param condition 条件
     * @param message 错误消息
     * @throws IllegalArgumentException 如果条件为假
     */
    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证条件是否为假
     *
     * @param condition 条件
     * @param message 错误消息
     * @throws IllegalArgumentException 如果条件为真
     */
    public static void isFalse(boolean condition, String message) {
        if (condition) {
            throw new IllegalArgumentException(message);
        }
    }

    // ==================== 数值验证方法 ====================

    /**
     * 验证数值是否为正数
     *
     * @param number 数值
     * @param message 错误消息
     * @throws IllegalArgumentException 如果数值不为正数
     */
    public static void isPositive(Number number, String message) {
        notNull(number, message);
        if (number.doubleValue() <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证数值是否为非负数
     *
     * @param number 数值
     * @param message 错误消息
     * @throws IllegalArgumentException 如果数值为负数
     */
    public static void isNonNegative(Number number, String message) {
        notNull(number, message);
        if (number.doubleValue() < 0) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证数值是否在指定范围内
     *
     * @param number 数值
     * @param min 最小值
     * @param max 最大值
     * @param message 错误消息
     * @throws IllegalArgumentException 如果数值不在范围内
     */
    public static void inRange(Number number, Number min, Number max, String message) {
        notNull(number, message);
        notNull(min, message);
        notNull(max, message);

        double minValue = min.doubleValue();
        double maxValue = max.doubleValue();

        if (number.doubleValue() < minValue || number.doubleValue() > maxValue) {
            throw new IllegalArgumentException(message);
        }
    }

    // ==================== 字符串长度验证方法 ====================

    /**
     * 验证字符串长度是否在指定范围内
     *
     * @param str 字符串
     * @param minLength 最小长度
     * @param maxLength 最大长度
     * @param message 错误消息
     * @throws IllegalArgumentException 如果长度不在范围内
     */
    public static void lengthBetween(String str, int minLength, int maxLength, String message) {
        notNull(str, message);
        int length = str.length();
        if (length < minLength || length > maxLength) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证字符串最小长度
     *
     * @param str 字符串
     * @param minLength 最小长度
     * @param message 错误消息
     * @throws IllegalArgumentException 如果长度小于最小值
     */
    public static void minLength(String str, int minLength, String message) {
        notNull(str, message);
        if (str.length() < minLength) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证字符串最大长度
     *
     * @param str 字符串
     * @param maxLength 最大长度
     * @param message 错误消息
     * @throws IllegalArgumentException 如果长度大于最大值
     */
    public static void maxLength(String str, int maxLength, String message) {
        notNull(str, message);
        if (str.length() > maxLength) {
            throw new IllegalArgumentException(message);
        }
    }

    // ==================== 格式验证方法 ====================

    /**
     * 验证字符串是否匹配正则表达式
     *
     * @param str 字符串
     * @param pattern 正则表达式
     * @param message 错误消息
     * @throws IllegalArgumentException 如果不匹配
     */
    public static void matches(String str, String pattern, String message) {
        notBlank(str, message);
        notBlank(pattern, message);
        if (!Pattern.matches(pattern, str)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证邮箱格式
     *
     * @param email 邮箱
     * @param message 错误消息
     * @throws IllegalArgumentException 如果格式不正确
     */
    public static void validEmail(String email, String message) {
        if (!StringUtil.isValidEmail(email)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证手机号格式
     *
     * @param phone 手机号
     * @param message 错误消息
     * @throws IllegalArgumentException 如果格式不正确
     */
    public static void validPhone(String phone, String message) {
        if (!StringUtil.isValidPhone(phone)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证身份证号格式
     *
     * @param idCard 身份证号
     * @param message 错误消息
     * @throws IllegalArgumentException 如果格式不正确
     */
    public static void validIdCard(String idCard, String message) {
        if (!StringUtil.isValidIdCard(idCard)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证用户名格式
     *
     * @param username 用户名
     * @param message 错误消息
     * @throws IllegalArgumentException 如果格式不正确
     */
    public static void validUsername(String username, String message) {
        if (!StringUtil.isValidUsername(username)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证密码格式
     *
     * @param password 密码
     * @param message 错误消息
     * @throws IllegalArgumentException 如果格式不正确
     */
    public static void validPassword(String password, String message) {
        if (!StringUtil.isValidPassword(password)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证用户名格式（静态方法）
     *
     * @param username 用户名
     * @return 是否有效
     */
    public static boolean isValidUsername(String username) {
        return StringUtil.isValidUsername(username);
    }

    /**
     * 验证密码格式（静态方法）
     *
     * @param password 密码
     * @return 是否有效
     */
    public static boolean isValidPassword(String password) {
        return StringUtil.isValidPassword(password);
    }

    /**
     * 验证IP地址格式
     *
     * @param ip IP地址
     * @param message 错误消息
     * @throws IllegalArgumentException 如果格式不正确
     */
    public static void validIp(String ip, String message) {
        if (!StringUtil.isValidIp(ip)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证URL格式
     *
     * @param url URL
     * @param message 错误消息
     * @throws IllegalArgumentException 如果格式不正确
     */
    public static void validUrl(String url, String message) {
        if (!StringUtil.isValidUrl(url)) {
            throw new IllegalArgumentException(message);
        }
    }

    // ==================== 日期验证方法 ====================

    /**
     * 验证日期是否在指定范围内
     *
     * @param date 日期
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param message 错误消息
     * @throws IllegalArgumentException 如果日期不在范围内
     */
    public static void dateBetween(LocalDate date, LocalDate startDate, LocalDate endDate, String message) {
        notNull(date, message);
        notNull(startDate, message);
        notNull(endDate, message);

        if (!DateUtil.isBetween(date, startDate, endDate)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证日期时间是否在指定范围内
     *
     * @param dateTime 日期时间
     * @param startDateTime 开始日期时间
     * @param endDateTime 结束日期时间
     * @param message 错误消息
     * @throws IllegalArgumentException 如果日期时间不在范围内
     */
    public static void dateTimeBetween(LocalDateTime dateTime, LocalDateTime startDateTime,
                                     LocalDateTime endDateTime, String message) {
        notNull(dateTime, message);
        notNull(startDateTime, message);
        notNull(endDateTime, message);

        if (!DateUtil.isBetween(dateTime, startDateTime, endDateTime)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证日期是否不早于今天
     *
     * @param date 日期
     * @param message 错误消息
     * @throws IllegalArgumentException 如果日期早于今天
     */
    public static void notBeforeToday(LocalDate date, String message) {
        notNull(date, message);
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证日期是否不晚于今天
     *
     * @param date 日期
     * @param message 错误消息
     * @throws IllegalArgumentException 如果日期晚于今天
     */
    public static void notAfterToday(LocalDate date, String message) {
        notNull(date, message);
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(message);
        }
    }

    // ==================== 集合验证方法 ====================

    /**
     * 验证集合大小是否在指定范围内
     *
     * @param collection 集合
     * @param minSize 最小大小
     * @param maxSize 最大大小
     * @param message 错误消息
     * @throws IllegalArgumentException 如果大小不在范围内
     */
    public static void sizeBetween(Collection<?> collection, int minSize, int maxSize, String message) {
        notNull(collection, message);
        int size = collection.size();
        if (size < minSize || size > maxSize) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证集合最小大小
     *
     * @param collection 集合
     * @param minSize 最小大小
     * @param message 错误消息
     * @throws IllegalArgumentException 如果大小小于最小值
     */
    public static void minSize(Collection<?> collection, int minSize, String message) {
        notNull(collection, message);
        if (collection.size() < minSize) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证集合最大大小
     *
     * @param collection 集合
     * @param maxSize 最大大小
     * @param message 错误消息
     * @throws IllegalArgumentException 如果大小大于最大值
     */
    public static void maxSize(Collection<?> collection, int maxSize, String message) {
        notNull(collection, message);
        if (collection.size() > maxSize) {
            throw new IllegalArgumentException(message);
        }
    }

    // ==================== 业务验证方法 ====================

    /**
     * 验证用户ID是否有效
     *
     * @param userId 用户ID
     * @param message 错误消息
     * @throws IllegalArgumentException 如果用户ID无效
     */
    public static void validUserId(Long userId, String message) {
        notNull(userId, message);
        isPositive(userId, message);
    }

    /**
     * 验证分页参数
     *
     * @param page 页码
     * @param size 页大小
     * @param maxSize 最大页大小
     * @param message 错误消息
     * @throws IllegalArgumentException 如果参数无效
     */
    public static void validPageParams(int page, int size, int maxSize, String message) {
        isNonNegative(page, message);
        isPositive(size, message);
        if (size > maxSize) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证金额是否有效
     *
     * @param amount 金额
     * @param message 错误消息
     * @throws IllegalArgumentException 如果金额无效
     */
    public static void validAmount(BigDecimal amount, String message) {
        notNull(amount, message);
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证金额精度
     *
     * @param amount 金额
     * @param scale 小数位数
     * @param message 错误消息
     * @throws IllegalArgumentException 如果精度不正确
     */
    public static void validAmountScale(BigDecimal amount, int scale, String message) {
        notNull(amount, message);
        if (amount.scale() > scale) {
            throw new IllegalArgumentException(message);
        }
    }

    // ==================== 文件验证方法 ====================

    /**
     * 验证文件名是否有效
     *
     * @param filename 文件名
     * @param message 错误消息
     * @throws IllegalArgumentException 如果文件名无效
     */
    public static void validFilename(String filename, String message) {
        notBlank(filename, message);
        // 检查是否包含非法字符
        String illegalChars = "\\/:*?\"<>|";
        for (char c : illegalChars.toCharArray()) {
            if (filename.indexOf(c) != -1) {
                throw new IllegalArgumentException(message);
            }
        }
    }

    /**
     * 验证文件扩展名
     *
     * @param filename 文件名
     * @param allowedExtensions 允许的扩展名数组
     * @param message 错误消息
     * @throws IllegalArgumentException 如果扩展名不被允许
     */
    public static void validFileExtension(String filename, String[] allowedExtensions, String message) {
        notBlank(filename, message);
        notEmpty(allowedExtensions, message);

        String extension = getFileExtension(filename);
        if (StringUtil.isEmpty(extension)) {
            throw new IllegalArgumentException(message);
        }

        boolean allowed = false;
        for (String allowedExt : allowedExtensions) {
            if (extension.equalsIgnoreCase(allowedExt)) {
                allowed = true;
                break;
            }
        }

        if (!allowed) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 验证文件大小
     *
     * @param fileSize 文件大小（字节）
     * @param maxSize 最大大小（字节）
     * @param message 错误消息
     * @throws IllegalArgumentException 如果文件过大
     */
    public static void validFileSize(long fileSize, long maxSize, String message) {
        isNonNegative(fileSize, message);
        isPositive(maxSize, message);
        if (fileSize > maxSize) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名（不包含点号）
     */
    private static String getFileExtension(String filename) {
        if (StringUtil.isEmpty(filename)) {
            return null;
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return null;
        }
        return filename.substring(lastDotIndex + 1);
    }

    // ==================== 安全验证方法 ====================

    /**
     * 验证字符串是否包含SQL注入风险
     *
     * @param input 输入字符串
     * @param message 错误消息
     * @throws IllegalArgumentException 如果包含SQL注入风险
     */
    public static void noSqlInjection(String input, String message) {
        if (StringUtil.isEmpty(input)) {
            return;
        }

        String lowerInput = input.toLowerCase();
        String[] sqlKeywords = {
            "select", "insert", "update", "delete", "drop", "create", "alter",
            "exec", "execute", "union", "script", "javascript", "vbscript",
            "where", "from", "table", "database", "schema"
        };

        // 使用词边界检查，避免将普通单词（如 "normal" 中的 "or"）误判为关键词
        String[] tokens = lowerInput.split("\\W+");
        for (String token : tokens) {
            for (String keyword : sqlKeywords) {
                if (token.equals(keyword)) {
                    throw new IllegalArgumentException(message);
                }
            }
        }

        // 检查SQL注入常见模式
        String[] sqlPatterns = {
            "'", "\"", ";", "--", "/*", "*/", "=", "1=1", "1='1", "'='", "or 1=1", "or '1'='1"
        };

        for (String pattern : sqlPatterns) {
            if (lowerInput.contains(pattern)) {
                throw new IllegalArgumentException(message);
            }
        }
    }

    /**
     * 验证字符串是否包含XSS风险
     *
     * @param input 输入字符串
     * @param message 错误消息
     * @throws IllegalArgumentException 如果包含XSS风险
     */
    public static void noXss(String input, String message) {
        if (StringUtil.isEmpty(input)) {
            return;
        }

        String lowerInput = input.toLowerCase();
        String[] xssPatterns = {
            "<script", "</script>", "javascript:", "vbscript:", "onload=", "onerror=",
            "onclick=", "onmouseover=", "onfocus=", "onblur=", "onchange=", "onsubmit=",
            "alert(", "eval(", "expression(", "<iframe", "<object", "<embed", "<form",
            "';", "')", "';", "//", "/*", "*/"
        };

        for (String pattern : xssPatterns) {
            if (lowerInput.contains(pattern)) {
                throw new IllegalArgumentException(message);
            }
        }
    }

    // ==================== 静默验证方法（返回boolean） ====================

    /**
     * 静默验证对象是否不为null
     *
     * @param obj 对象
     * @return 是否通过验证
     */
    public static boolean isNotNull(Object obj) {
        return obj != null;
    }

    /**
     * 静默验证字符串是否不为空
     *
     * @param str 字符串
     * @return 是否通过验证
     */
    public static boolean isNotEmpty(String str) {
        return StringUtil.isNotEmpty(str);
    }

    /**
     * 静默验证字符串是否不为空白
     *
     * @param str 字符串
     * @return 是否通过验证
     */
    public static boolean isNotBlank(String str) {
        return StringUtil.isNotBlank(str);
    }

    /**
     * 静默验证数值是否为正数
     *
     * @param number 数值
     * @return 是否通过验证
     */
    public static boolean isPositive(Number number) {
        return number != null && number.doubleValue() > 0;
    }

    /**
     * 静默验证数值是否为非负数
     *
     * @param number 数值
     * @return 是否通过验证
     */
    public static boolean isNonNegative(Number number) {
        return number != null && number.doubleValue() >= 0;
    }

    /**
     * 静默验证字符串长度是否在指定范围内
     *
     * @param str 字符串
     * @param minLength 最小长度
     * @param maxLength 最大长度
     * @return 是否通过验证
     */
    public static boolean isLengthBetween(String str, int minLength, int maxLength) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        return length >= minLength && length <= maxLength;
    }
}


