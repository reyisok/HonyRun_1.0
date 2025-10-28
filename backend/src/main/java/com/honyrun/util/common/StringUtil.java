package com.honyrun.util.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 * 提供字符串处理、验证、转换工具
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  16:30:00
 * @modified 2025-07-01 16:30:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public final class StringUtil {

    private StringUtil() {
        // 工具类，禁止实例化
    }

    // ==================== 常用正则表达式 ====================

    /**
     * 邮箱正则表达式
     */
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    /**
     * 手机号正则表达式
     */
    public static final String PHONE_REGEX = "^1[3-9]\\d{9}$";

    /**
     * 身份证号正则表达式
     */
    public static final String ID_CARD_REGEX = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$";

    /**
     * 用户名正则表达式（字母、数字、下划线、连字符）
     */
    public static final String USERNAME_REGEX = "^[a-zA-Z0-9_-]{3,50}$";

    /**
     * 密码正则表达式
     * 支持多种密码格式：
     * 1. 1-3位纯数字
     * 2. 9位及以上纯数字
     * 3. 字母后跟数字
     * 4. 数字后跟字母
     * 5. 纯字母至少5位
     * 6. 至少一个特殊字符（含#），长度3-100
     */
    public static final String PASSWORD_REGEX =
            "^(" +
            "[0-9]{1,3}" +                 // 1-3位纯数字
            "|" +
            "[0-9]{9,}" +                  // 9位及以上纯数字
            "|" +
            "[a-zA-Z]+[0-9]+" +            // 字母后跟数字
            "|" +
            "[0-9]+[a-zA-Z]+" +            // 数字后跟字母
            "|" +
            "[a-zA-Z]{5,}" +               // 纯字母至少5位
            "|" +
            "(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{3,100}" + // 至少一个特殊字符（含#），长度3-100
            ")$";

    /**
     * IP地址正则表达式
     */
    public static final String IP_REGEX = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";

    /**
     * URL正则表达式
     */
    public static final String URL_REGEX = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";

    /**
     * 数字正则表达式
     */
    public static final String NUMBER_REGEX = "^-?\\d+(\\.\\d+)?$";

    /**
     * 整数正则表达式
     */
    public static final String INTEGER_REGEX = "^-?\\d+$";

    /**
     * 正整数正则表达式
     */
    public static final String POSITIVE_INTEGER_REGEX = "^[1-9]\\d*$";

    // ==================== 编译后的Pattern对象 ====================

    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);
    private static final Pattern ID_CARD_PATTERN = Pattern.compile(ID_CARD_REGEX);
    private static final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_REGEX);
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);
    private static final Pattern IP_PATTERN = Pattern.compile(IP_REGEX);
    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);
    private static final Pattern NUMBER_PATTERN = Pattern.compile(NUMBER_REGEX);
    private static final Pattern INTEGER_PATTERN = Pattern.compile(INTEGER_REGEX);
    private static final Pattern POSITIVE_INTEGER_PATTERN = Pattern.compile(POSITIVE_INTEGER_REGEX);

    // ==================== 基础判断方法 ====================

    /**
     * 判断字符串是否为空或null
     *
     * @param str 字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断字符串是否不为空
     *
     * @param str 字符串
     * @return 是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 判断字符串是否为空白（null、空字符串或只包含空白字符）
     *
     * @param str 字符串
     * @return 是否为空白
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断字符串是否不为空白
     *
     * @param str 字符串
     * @return 是否不为空白
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 判断所有字符串是否都为空
     *
     * @param strs 字符串数组
     * @return 是否都为空
     */
    public static boolean isAllEmpty(String... strs) {
        if (strs == null || strs.length == 0) {
            return true;
        }
        for (String str : strs) {
            if (isNotEmpty(str)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否有任意字符串为空
     *
     * @param strs 字符串数组
     * @return 是否有任意为空
     */
    public static boolean isAnyEmpty(String... strs) {
        if (strs == null || strs.length == 0) {
            return true;
        }
        for (String str : strs) {
            if (isEmpty(str)) {
                return true;
            }
        }
        return false;
    }

    // ==================== 字符串处理方法 ====================

    /**
     * 安全的trim操作，null返回null
     *
     * @param str 字符串
     * @return trim后的字符串
     */
    public static String trim(String str) {
        return str != null ? str.trim() : null;
    }

    /**
     * 安全的trim操作，null返回空字符串
     *
     * @param str 字符串
     * @return trim后的字符串
     */
    public static String trimToEmpty(String str) {
        return str != null ? str.trim() : "";
    }

    /**
     * 安全的trim操作，空字符串返回null
     *
     * @param str 字符串
     * @return trim后的字符串
     */
    public static String trimToNull(String str) {
        String trimmed = trim(str);
        return isEmpty(trimmed) ? null : trimmed;
    }

    /**
     * 字符串默认值处理
     *
     * @param str 字符串
     * @param defaultValue 默认值
     * @return 处理后的字符串
     */
    public static String defaultIfEmpty(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }

    /**
     * 字符串默认值处理（空白字符串）
     *
     * @param str 字符串
     * @param defaultValue 默认值
     * @return 处理后的字符串
     */
    public static String defaultIfBlank(String str, String defaultValue) {
        return isBlank(str) ? defaultValue : str;
    }

    /**
     * 截取字符串，超出长度用省略号表示
     *
     * @param str 字符串
     * @param maxLength 最大长度
     * @return 截取后的字符串
     */
    public static String truncate(String str, int maxLength) {
        if (isEmpty(str) || maxLength <= 0) {
            return str;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        if (maxLength <= 3) {
            return "...";
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * 左填充字符串
     *
     * @param str 原字符串
     * @param size 目标长度
     * @param padChar 填充字符
     * @return 填充后的字符串
     */
    public static String leftPad(String str, int size, char padChar) {
        if (str == null) {
            return null;
        }
        int pads = size - str.length();
        if (pads <= 0) {
            return str;
        }
        return repeat(padChar, pads) + str;
    }

    /**
     * 右填充字符串
     *
     * @param str 原字符串
     * @param size 目标长度
     * @param padChar 填充字符
     * @return 填充后的字符串
     */
    public static String rightPad(String str, int size, char padChar) {
        if (str == null) {
            return null;
        }
        int pads = size - str.length();
        if (pads <= 0) {
            return str;
        }
        return str + repeat(padChar, pads);
    }

    /**
     * 重复字符
     *
     * @param ch 字符
     * @param repeat 重复次数
     * @return 重复后的字符串
     */
    public static String repeat(char ch, int repeat) {
        if (repeat <= 0) {
            return "";
        }
        char[] chars = new char[repeat];
        Arrays.fill(chars, ch);
        return new String(chars);
    }

    /**
     * 重复字符串
     *
     * @param str 字符串
     * @param repeat 重复次数
     * @return 重复后的字符串
     */
    public static String repeat(String str, int repeat) {
        if (str == null || repeat <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(str.length() * repeat);
        for (int i = 0; i < repeat; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    // ==================== 字符串转换方法 ====================

    /**
     * 首字母大写
     *
     * @param str 字符串
     * @return 首字母大写的字符串
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 首字母小写
     *
     * @param str 字符串
     * @return 首字母小写的字符串
     */
    public static String uncapitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * 驼峰命名转下划线
     *
     * @param camelCase 驼峰命名字符串
     * @return 下划线分隔的字符串
     */
    public static String camelToSnake(String camelCase) {
        if (isEmpty(camelCase)) {
            return camelCase;
        }
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * 下划线转驼峰命名
     *
     * @param snakeCase 下划线分隔的字符串
     * @return 驼峰命名字符串
     */
    public static String snakeToCamel(String snakeCase) {
        if (isEmpty(snakeCase)) {
            return snakeCase;
        }
        String[] parts = snakeCase.toLowerCase().split("_");
        StringBuilder camelCase = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            camelCase.append(capitalize(parts[i]));
        }
        return camelCase.toString();
    }

    /**
     * 反转字符串
     *
     * @param str 字符串
     * @return 反转后的字符串
     */
    public static String reverse(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return new StringBuilder(str).reverse().toString();
    }

    // ==================== 字符串验证方法 ====================

    /**
     * 验证邮箱格式
     *
     * @param email 邮箱字符串
     * @return 是否为有效邮箱
     */
    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) {
            return false;
        }
        // 限制邮箱总长度不超过254字符（RFC 5321标准）
        if (email.length() > 254) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 验证手机号格式
     *
     * @param phone 手机号字符串
     * @return 是否为有效手机号
     */
    public static boolean isValidPhone(String phone) {
        return isNotEmpty(phone) && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 验证身份证号格式
     *
     * @param idCard 身份证号字符串
     * @return 是否为有效身份证号
     */
    public static boolean isValidIdCard(String idCard) {
        return isNotEmpty(idCard) && ID_CARD_PATTERN.matcher(idCard).matches();
    }

    /**
     * 验证用户名格式
     *
     * @param username 用户名字符串
     * @return 是否为有效用户名
     */
    public static boolean isValidUsername(String username) {
        return isNotEmpty(username) && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * 验证密码格式
     *
     * @param password 密码字符串
     * @return 是否为有效密码
     */
    public static boolean isValidPassword(String password) {
        return isNotEmpty(password) && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * 验证IP地址格式
     *
     * @param ip IP地址字符串
     * @return 是否为有效IP地址
     */
    public static boolean isValidIp(String ip) {
        return isNotEmpty(ip) && IP_PATTERN.matcher(ip).matches();
    }

    /**
     * 验证URL格式
     *
     * @param url URL字符串
     * @return 是否为有效URL
     */
    public static boolean isValidUrl(String url) {
        return isNotEmpty(url) && URL_PATTERN.matcher(url).matches();
    }

    /**
     * 验证是否为数字
     *
     * @param str 字符串
     * @return 是否为数字
     */
    public static boolean isNumber(String str) {
        return isNotEmpty(str) && NUMBER_PATTERN.matcher(str).matches();
    }

    /**
     * 验证是否为整数
     *
     * @param str 字符串
     * @return 是否为整数
     */
    public static boolean isInteger(String str) {
        return isNotEmpty(str) && INTEGER_PATTERN.matcher(str).matches();
    }

    /**
     * 验证是否为正整数
     *
     * @param str 字符串
     * @return 是否为正整数
     */
    public static boolean isPositiveInteger(String str) {
        return isNotEmpty(str) && POSITIVE_INTEGER_PATTERN.matcher(str).matches();
    }

    // ==================== 字符串分割和连接 ====================

    /**
     * 安全的字符串分割
     *
     * @param str 字符串
     * @param delimiter 分隔符
     * @return 分割后的数组
     */
    public static String[] split(String str, String delimiter) {
        if (isEmpty(str)) {
            return new String[0];
        }
        return str.split(Pattern.quote(delimiter));
    }

    /**
     * 连接字符串数组
     *
     * @param array 字符串数组
     * @param delimiter 分隔符
     * @return 连接后的字符串
     */
    public static String join(String[] array, String delimiter) {
        if (array == null || array.length == 0) {
            return "";
        }
        return String.join(delimiter, array);
    }

    /**
     * 连接集合
     *
     * @param collection 字符串集合
     * @param delimiter 分隔符
     * @return 连接后的字符串
     */
    public static String join(Collection<String> collection, String delimiter) {
        if (collection == null || collection.isEmpty()) {
            return "";
        }
        return String.join(delimiter, collection);
    }

    // ==================== 编码和加密方法 ====================

    /**
     * MD5加密
     *
     * @param str 原字符串
     * @return MD5加密后的字符串
     */
    public static String md5(String str) {
        if (isEmpty(str)) {
            return str;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    /**
     * SHA256加密
     *
     * @param str 原字符串
     * @return SHA256加密后的字符串
     */
    public static String sha256(String str) {
        if (isEmpty(str)) {
            return str;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不可用", e);
        }
    }

    /**
     * Base64编码
     *
     * @param str 原字符串
     * @return Base64编码后的字符串
     */
    public static String base64Encode(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Base64解码
     *
     * @param str Base64编码的字符串
     * @return 解码后的字符串
     */
    public static String base64Decode(String str) {
        if (isEmpty(str)) {
            return str;
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(str);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ==================== 随机字符串生成 ====================

    /**
     * 生成随机字符串（包含字母和数字）
     *
     * @param length 长度
     * @return 随机字符串
     */
    public static String randomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        return randomString(length, chars);
    }

    /**
     * 生成随机字符串
     *
     * @param length 长度
     * @param chars 字符集
     * @return 随机字符串
     */
    public static String randomString(int length, String chars) {
        if (length <= 0 || isEmpty(chars)) {
            return "";
        }
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 生成随机数字字符串
     *
     * @param length 长度
     * @return 随机数字字符串
     */
    public static String randomNumeric(int length) {
        return randomString(length, "0123456789");
    }

    /**
     * 生成随机字母字符串
     *
     * @param length 长度
     * @return 随机字母字符串
     */
    public static String randomAlphabetic(int length) {
        return randomString(length, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
    }
}


