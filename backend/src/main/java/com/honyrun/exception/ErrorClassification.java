package com.honyrun.exception;

/**
 * 错误分类枚举
 * 
 * 用于对异常进行分类，支持不同的重试和熔断策略。
 * 根据错误类型决定是否需要重试、熔断或降级处理。
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-02 20:35:00
 * @modified 2025-07-02 20:35:00
 * @version 1.0.0
 */
public enum ErrorClassification {

    /**
     * 业务错误 - 不需要重试
     */
    BUSINESS_ERROR("BUSINESS", "业务错误", false, false),

    /**
     * 参数验证错误 - 不需要重试
     */
    VALIDATION_ERROR("VALIDATION", "参数验证错误", false, false),

    /**
     * 认证授权错误 - 不需要重试
     */
    AUTH_ERROR("AUTH", "认证授权错误", false, false),

    /**
     * 数据访问错误 - 可以重试
     */
    DATA_ACCESS_ERROR("DATA_ACCESS", "数据访问错误", true, true),

    /**
     * 外部服务错误 - 可以重试和熔断
     */
    EXTERNAL_SERVICE_ERROR("EXTERNAL_SERVICE", "外部服务错误", true, true),

    /**
     * 网络错误 - 可以重试和熔断
     */
    NETWORK_ERROR("NETWORK", "网络错误", true, true),

    /**
     * 超时错误 - 可以重试和熔断
     */
    TIMEOUT_ERROR("TIMEOUT", "超时错误", true, true),

    /**
     * 系统错误 - 可以重试
     */
    SYSTEM_ERROR("SYSTEM", "系统错误", true, false),

    /**
     * 未知错误 - 不重试
     */
    UNKNOWN_ERROR("UNKNOWN", "未知错误", false, false);

    private final String code;
    private final String description;
    private final boolean retryable;
    private final boolean circuitBreakerEnabled;

    ErrorClassification(String code, String description, boolean retryable, boolean circuitBreakerEnabled) {
        this.code = code;
        this.description = description;
        this.retryable = retryable;
        this.circuitBreakerEnabled = circuitBreakerEnabled;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public boolean isCircuitBreakerEnabled() {
        return circuitBreakerEnabled;
    }

    /**
     * 根据异常类型获取错误分类
     * 
     * @param throwable 异常对象
     * @return 错误分类
     */
    public static ErrorClassification classify(Throwable throwable) {
        if (throwable instanceof BusinessException) {
            return BUSINESS_ERROR;
        } else if (throwable instanceof jakarta.validation.ConstraintViolationException ||
                   throwable instanceof org.springframework.validation.BindException ||
                   throwable instanceof org.springframework.web.bind.MethodArgumentNotValidException) {
            return VALIDATION_ERROR;
        } else if (throwable instanceof AuthenticationException ||
                   throwable instanceof org.springframework.security.access.AccessDeniedException) {
            return AUTH_ERROR;
        } else if (throwable instanceof DataAccessException ||
                   throwable instanceof org.springframework.dao.DataAccessException) {
            return DATA_ACCESS_ERROR;
        } else if (throwable instanceof java.net.ConnectException ||
                   throwable instanceof java.net.SocketTimeoutException ||
                   throwable instanceof java.net.UnknownHostException) {
            return NETWORK_ERROR;
        } else if (throwable instanceof java.util.concurrent.TimeoutException ||
                   throwable.getMessage() != null && throwable.getMessage().toLowerCase().contains("timeout")) {
            return TIMEOUT_ERROR;
        } else if (throwable instanceof SystemException) {
            return SYSTEM_ERROR;
        } else {
            return UNKNOWN_ERROR;
        }
    }
}

