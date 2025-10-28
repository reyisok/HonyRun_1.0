package com.honyrun.constant;

/**
 * 权限码常量统一定义
 * 将项目内使用的权限字符串集中管理，避免散落的硬编码。
 */
public final class PermissionConstants {

    private PermissionConstants() {}

    // 系统管理权限
    public static final String SYSTEM_MANAGEMENT = "SYSTEM_MANAGEMENT";

    // 用户权限
    public static final String USER_MANAGE = "USER_MANAGE";
    public static final String USER_VIEW = "USER_VIEW";
    public static final String USER_CREATE = "USER_CREATE";
    public static final String USER_UPDATE = "USER_UPDATE";
    public static final String USER_DELETE = "USER_DELETE";

    // 系统权限
    public static final String SYSTEM_SETTING = "SYSTEM_SETTING";
    public static final String SYSTEM_MONITOR = "SYSTEM_MONITOR";

    // 日志权限
    public static final String LOG_VIEW = "LOG_VIEW";

    // 业务权限
    public static final String BUSINESS_OPERATE = "BUSINESS_OPERATE";
    public static final String BUSINESS_VIEW = "BUSINESS_VIEW";

    // 文件权限
    public static final String FILE_UPLOAD = "FILE_UPLOAD";
}
