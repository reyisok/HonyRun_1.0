package com.honyrun.model.dto.request;

import com.honyrun.model.dto.reactive.ReactiveRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 认证请求DTO
 *
 * 用于用户登录认证的请求数据传输对象，支持响应式处理。
 * 该DTO包含用户名、密码等认证信息，支持参数验证。
 *
 * 特性：
 * - 继承响应式请求基类
 * - 参数验证支持
 * - 安全信息处理
 * - 登录类型支持
 *
 * @author Mr.Rey
 * @since 2.0.0
 * @created 2025-07-01  16:55:00
 * @modified 2025-07-01 16:55:00
 * @version 2.0.0
 */
public class AuthRequest extends ReactiveRequest {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     * 用户登录的用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    /**
     * 密码
     * 用户登录的密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 3, max = 100, message = "密码长度必须在3-100个字符之间")
    private String password;

    /**
     * 登录类型
     * NORMAL-普通登录, SYSTEM_USER-系统用户登录, SYSTEM-系统登录
     */
    private LoginType loginType = LoginType.NORMAL;

    /**
     * 验证码
     * 登录验证码（可选）
     */
    private String captcha;

    /**
     * 验证码键
     * 验证码的唯一标识键
     */
    private String captchaKey;

    /**
     * 记住我
     * 是否记住登录状态
     */
    private Boolean rememberMe = false;

    /**
     * 设备信息
     * 登录设备的信息
     */
    private String deviceInfo;

    /**
     * 设备ID
     * 登录设备的唯一标识
     */
    private String deviceId;

    /**
     * 登录来源
     * WEB-网页, MOBILE-移动端, API-API调用
     */
    private LoginSource loginSource = LoginSource.WEB;

    /**
     * 默认构造函数
     */
    public AuthRequest() {
        super();
    }

    /**
     * 带用户名密码的构造函数
     *
     * @param username 用户名
     * @param password 密码
     */
    public AuthRequest(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }

    /**
     * 带完整参数的构造函数
     *
     * @param username 用户名
     * @param password 密码
     * @param loginType 登录类型
     * @param loginSource 登录来源
     */
    public AuthRequest(String username, String password, LoginType loginType, LoginSource loginSource) {
        super();
        this.username = username;
        this.password = password;
        this.loginType = loginType;
        this.loginSource = loginSource;
    }

    /**
     * 获取用户名
     *
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名
     *
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取密码
     *
     * @return 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码
     *
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取登录类型
     *
     * @return 登录类型
     */
    public LoginType getLoginType() {
        return loginType;
    }

    /**
     * 设置登录类型
     *
     * @param loginType 登录类型
     */
    public void setLoginType(LoginType loginType) {
        this.loginType = loginType;
    }

    /**
     * 获取验证码
     *
     * @return 验证码
     */
    public String getCaptcha() {
        return captcha;
    }

    /**
     * 设置验证码
     *
     * @param captcha 验证码
     */
    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    /**
     * 获取验证码键
     *
     * @return 验证码键
     */
    public String getCaptchaKey() {
        return captchaKey;
    }

    /**
     * 设置验证码键
     *
     * @param captchaKey 验证码键
     */
    public void setCaptchaKey(String captchaKey) {
        this.captchaKey = captchaKey;
    }

    /**
     * 获取记住我标识
     *
     * @return 记住我标识
     */
    public Boolean getRememberMe() {
        return rememberMe;
    }

    /**
     * 设置记住我标识
     *
     * @param rememberMe 记住我标识
     */
    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    /**
     * 获取设备信息
     *
     * @return 设备信息
     */
    public String getDeviceInfo() {
        return deviceInfo;
    }

    /**
     * 设置设备信息
     *
     * @param deviceInfo 设备信息
     */
    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    /**
     * 获取设备ID
     *
     * @return 设备ID
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * 设置设备ID
     *
     * @param deviceId 设备ID
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * 获取登录来源
     *
     * @return 登录来源
     */
    public LoginSource getLoginSource() {
        return loginSource;
    }

    /**
     * 设置登录来源
     *
     * @param loginSource 登录来源
     */
    public void setLoginSource(LoginSource loginSource) {
        this.loginSource = loginSource;
    }

    /**
     * 判断是否需要验证码
     *
     * @return 如果需要验证码返回true，否则返回false
     */
    public boolean needsCaptcha() {
        return this.captcha != null && !this.captcha.trim().isEmpty() &&
               this.captchaKey != null && !this.captchaKey.trim().isEmpty();
    }

    /**
     * 判断是否为系统用户登录
     *
     * @return 如果是系统用户登录返回true，否则返回false
     */
    public boolean isAdminLogin() {
        return this.loginType == LoginType.SYSTEM_USER;
    }

    /**
     * 判断是否为系统登录
     *
     * @return 如果是系统登录返回true，否则返回false
     */
    public boolean isSystemLogin() {
        return this.loginType == LoginType.SYSTEM;
    }

    /**
     * 判断是否记住登录状态
     *
     * @return 如果记住登录状态返回true，否则返回false
     */
    public boolean isRememberMe() {
        return Boolean.TRUE.equals(this.rememberMe);
    }

    /**
     * 清除敏感信息
     * 用于日志记录时清除密码等敏感信息
     */
    public void clearSensitiveInfo() {
        this.password = "***";
        this.captcha = "***";
    }

    /**
     * 获取安全的字符串表示
     * 不包含敏感信息的字符串表示
     *
     * @return 安全的字符串表示
     */
    public String toSafeString() {
        return String.format("AuthRequest{username='%s', loginType=%s, loginSource=%s, rememberMe=%s, needsCaptcha=%s}",
                username, loginType, loginSource, rememberMe, needsCaptcha());
    }

    @Override
    public boolean validate() {
        if (!super.validate()) {
            return false;
        }

        // 验证用户名和密码
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            return false;
        }

        // 验证用户名长度
        if (username.length() < 3 || username.length() > 50) {
            return false;
        }

        // 验证密码长度
        if (password.length() < 3 || password.length() > 100) {
            return false;
        }

        // 如果有验证码，验证验证码键
        if (captcha != null && !captcha.trim().isEmpty()) {
            if (captchaKey == null || captchaKey.trim().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * 登录类型枚举
     */
    public enum LoginType {
        /** 普通登录 */
        NORMAL("普通登录"),
        /** 系统用户登录 */
        SYSTEM_USER("系统用户登录"),
        /** 系统登录 */
        SYSTEM("系统登录");

        private final String description;

        LoginType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 登录来源枚举
     */
    public enum LoginSource {
        /** 网页 */
        WEB("网页"),
        /** 移动端 */
        MOBILE("移动端"),
        /** API调用 */
        API("API调用");

        private final String description;

        LoginSource(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 创建Builder实例
     *
     * @return Builder实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder模式构建器
     */
    public static class Builder {
        private String username;
        private String password;
        private LoginType loginType = LoginType.NORMAL;
        private String captcha;
        private String captchaKey;
        private Boolean rememberMe = false;
        private String deviceInfo;
        private String deviceId;
        private LoginSource loginSource = LoginSource.WEB;

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder loginType(LoginType loginType) {
            this.loginType = loginType;
            return this;
        }

        public Builder captcha(String captcha) {
            this.captcha = captcha;
            return this;
        }

        public Builder captchaKey(String captchaKey) {
            this.captchaKey = captchaKey;
            return this;
        }

        public Builder rememberMe(Boolean rememberMe) {
            this.rememberMe = rememberMe;
            return this;
        }

        public Builder deviceInfo(String deviceInfo) {
            this.deviceInfo = deviceInfo;
            return this;
        }

        public Builder deviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Builder loginSource(LoginSource loginSource) {
            this.loginSource = loginSource;
            return this;
        }

        public AuthRequest build() {
            AuthRequest request = new AuthRequest();
            request.username = this.username;
            request.password = this.password;
            request.loginType = this.loginType;
            request.captcha = this.captcha;
            request.captchaKey = this.captchaKey;
            request.rememberMe = this.rememberMe;
            request.deviceInfo = this.deviceInfo;
            request.deviceId = this.deviceId;
            request.loginSource = this.loginSource;
            return request;
        }
    }

    @Override
    public String toString() {
        return String.format("AuthRequest{username='%s', loginType=%s, loginSource=%s, rememberMe=%s, requestId='%s'}",
                username, loginType, loginSource, rememberMe, getRequestId());
    }
}


