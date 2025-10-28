package com.honyrun.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * 验证异常类
 * 实现参数验证错误和字段级错误信息
 *
 * @author Mr.Rey
 * @since 2025-07-01
 * @version 2.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class ValidationException extends RuntimeException {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final ErrorCode errorCode;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 字段错误映射
     */
    private transient Map<String, String> fieldErrors;

    /**
     * 验证规则名称
     */
    private String ruleName;

    /**
     * 验证对象类型
     */
    private String objectType;

    /**
     * 构造函数
     *
     * @param errorCode 错误码枚举
     */
    public ValidationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.fieldErrors = new HashMap<>();
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码枚举
     * @param message 自定义错误消息
     */
    public ValidationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.fieldErrors = new HashMap<>();
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码枚举
     * @param fieldErrors 字段错误信息
     */
    public ValidationException(ErrorCode errorCode, Map<String, String> fieldErrors) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.fieldErrors = fieldErrors != null ? new HashMap<>(fieldErrors) : new HashMap<>();
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     */
    public ValidationException(String message) {
        super(message);
        this.errorCode = ErrorCode.VALIDATION_ERROR;
        this.fieldErrors = new HashMap<>();
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     * @param fieldErrors 字段错误信息
     */
    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.errorCode = ErrorCode.VALIDATION_ERROR;
        this.fieldErrors = fieldErrors != null ? new HashMap<>(fieldErrors) : new HashMap<>();
    }

    /**
     * 创建验证异常
     *
     * @param errorCode 错误码枚举
     * @return 验证异常实例
     */
    public static ValidationException of(ErrorCode errorCode) {
        return new ValidationException(errorCode);
    }

    /**
     * 创建验证异常
     *
     * @param errorCode 错误码枚举
     * @param message 自定义错误消息
     * @return 验证异常实例
     */
    public static ValidationException of(ErrorCode errorCode, String message) {
        return new ValidationException(errorCode, message);
    }

    /**
     * 创建验证异常
     *
     * @param fieldName 字段名称
     * @param errorMessage 错误消息
     * @return 验证异常实例
     */
    public static ValidationException fieldError(String fieldName, String errorMessage) {
        ValidationException exception = new ValidationException(ErrorCode.VALIDATION_ERROR);
        exception.addFieldError(fieldName, errorMessage);
        return exception;
    }

    /**
     * 创建必填字段验证异常
     *
     * @param fieldName 字段名称
     * @return 验证异常实例
     */
    public static ValidationException requiredField(String fieldName) {
        ValidationException exception = new ValidationException(ErrorCode.MISSING_PARAMETER,
                String.format("字段 '%s' 不能为空", fieldName));
        exception.addFieldError(fieldName, "不能为空");
        exception.setRuleName("required");
        return exception;
    }

    /**
     * 创建字段格式验证异常
     *
     * @param fieldName 字段名称
     * @param expectedFormat 期望格式
     * @return 验证异常实例
     */
    public static ValidationException invalidFormat(String fieldName, String expectedFormat) {
        ValidationException exception = new ValidationException(ErrorCode.INVALID_PARAMETER,
                String.format("字段 '%s' 格式不正确，期望格式: %s", fieldName, expectedFormat));
        exception.addFieldError(fieldName, String.format("格式不正确，期望: %s", expectedFormat));
        exception.setRuleName("format");
        return exception;
    }

    /**
     * 创建字段长度验证异常
     *
     * @param fieldName 字段名称
     * @param minLength 最小长度
     * @param maxLength 最大长度
     * @return 验证异常实例
     */
    public static ValidationException invalidLength(String fieldName, int minLength, int maxLength) {
        ValidationException exception = new ValidationException(ErrorCode.INVALID_PARAMETER,
                String.format("字段 '%s' 长度必须在 %d 到 %d 之间", fieldName, minLength, maxLength));
        exception.addFieldError(fieldName, String.format("长度必须在 %d 到 %d 之间", minLength, maxLength));
        exception.setRuleName("length");
        return exception;
    }

    /**
     * 创建字段范围验证异常
     *
     * @param fieldName 字段名称
     * @param minValue 最小值
     * @param maxValue 最大值
     * @return 验证异常实例
     */
    public static ValidationException invalidRange(String fieldName, Object minValue, Object maxValue) {
        ValidationException exception = new ValidationException(ErrorCode.INVALID_PARAMETER,
                String.format("字段 '%s' 值必须在 %s 到 %s 之间", fieldName, minValue, maxValue));
        exception.addFieldError(fieldName, String.format("值必须在 %s 到 %s 之间", minValue, maxValue));
        exception.setRuleName("range");
        return exception;
    }

    /**
     * 创建邮箱格式验证异常
     *
     * @param fieldName 字段名称
     * @return 验证异常实例
     */
    public static ValidationException invalidEmail(String fieldName) {
        ValidationException exception = new ValidationException(ErrorCode.INVALID_PARAMETER,
                String.format("字段 '%s' 不是有效的邮箱地址", fieldName));
        exception.addFieldError(fieldName, "不是有效的邮箱地址");
        exception.setRuleName("email");
        return exception;
    }

    /**
     * 创建手机号格式验证异常
     *
     * @param fieldName 字段名称
     * @return 验证异常实例
     */
    public static ValidationException invalidPhone(String fieldName) {
        ValidationException exception = new ValidationException(ErrorCode.INVALID_PARAMETER,
                String.format("字段 '%s' 不是有效的手机号码", fieldName));
        exception.addFieldError(fieldName, "不是有效的手机号码");
        exception.setRuleName("phone");
        return exception;
    }

    /**
     * 创建密码强度验证异常
     *
     * @param fieldName 字段名称
     * @return 验证异常实例
     */
    public static ValidationException weakPassword(String fieldName) {
        ValidationException exception = new ValidationException(ErrorCode.PASSWORD_TOO_WEAK,
                "密码强度不足，必须包含大小写字母、数字和特殊字符");
        exception.addFieldError(fieldName, "密码强度不足，必须包含大小写字母、数字和特殊字符");
        exception.setRuleName("password_strength");
        return exception;
    }

    /**
     * 添加字段错误
     *
     * @param fieldName 字段名称
     * @param errorMessage 错误消息
     * @return 当前异常实例（支持链式调用）
     */
    public ValidationException addFieldError(String fieldName, String errorMessage) {
        this.fieldErrors.put(fieldName, errorMessage);
        return this;
    }

    /**
     * 添加多个字段错误
     *
     * @param errors 字段错误映射
     * @return 当前异常实例（支持链式调用）
     */
    public ValidationException addFieldErrors(Map<String, String> errors) {
        if (errors != null) {
            this.fieldErrors.putAll(errors);
        }
        return this;
    }

    /**
     * 设置请求路径
     *
     * @param path 请求路径
     * @return 当前异常实例（支持链式调用）
     */
    public ValidationException withPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * 设置验证规则名称
     *
     * @param ruleName 验证规则名称
     * @return 当前异常实例（支持链式调用）
     */
    public ValidationException withRuleName(String ruleName) {
        this.ruleName = ruleName;
        return this;
    }

    /**
     * 设置验证对象类型
     *
     * @param objectType 验证对象类型
     * @return 当前异常实例（支持链式调用）
     */
    public ValidationException withObjectType(String objectType) {
        this.objectType = objectType;
        return this;
    }

    /**
     * 获取错误码
     *
     * @return 错误码枚举
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * 获取请求路径
     *
     * @return 请求路径
     */
    public String getPath() {
        return path;
    }

    /**
     * 设置请求路径
     *
     * @param path 请求路径
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取字段错误信息
     *
     * @return 字段错误映射
     */
    public Map<String, String> getFieldErrors() {
        return new HashMap<>(fieldErrors);
    }

    /**
     * 设置字段错误信息
     *
     * @param fieldErrors 字段错误映射
     */
    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors != null ? new HashMap<>(fieldErrors) : new HashMap<>();
    }

    /**
     * 获取验证规则名称
     *
     * @return 验证规则名称
     */
    public String getRuleName() {
        return ruleName;
    }

    /**
     * 设置验证规则名称
     *
     * @param ruleName 验证规则名称
     */
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    /**
     * 获取验证对象类型
     *
     * @return 验证对象类型
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * 设置验证对象类型
     *
     * @param objectType 验证对象类型
     */
    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    /**
     * 判断是否有字段错误
     *
     * @return 如果有字段错误返回true，否则返回false
     */
    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }

    /**
     * 获取字段错误数量
     *
     * @return 字段错误数量
     */
    public int getFieldErrorCount() {
        return fieldErrors.size();
    }

    /**
     * 判断是否为特定错误码
     *
     * @param errorCode 要比较的错误码
     * @return 如果匹配返回true，否则返回false
     */
    public boolean isErrorCode(ErrorCode errorCode) {
        return this.errorCode == errorCode;
    }

    @Override
    public String toString() {
        return String.format("ValidationException{errorCode=%s, message='%s', fieldErrors=%s, ruleName='%s'}",
                errorCode, getMessage(), fieldErrors, ruleName);
    }
}

