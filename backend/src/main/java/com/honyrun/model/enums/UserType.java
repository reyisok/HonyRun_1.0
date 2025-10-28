package com.honyrun.model.enums;

/**
 * 用户类型枚举
 *
 * 定义系统中支持的用户类型，用于用户分类和权限控制
 * 支持R2DBC枚举映射和响应式数据访问
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  16:25:00
 * @modified 2025-07-01 16:25:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public enum UserType {

    /**
     * 系统用户
     * 具有系统管理权限的用户，如honyrun-sys等
     * 拥有最高级别的系统访问权限
     */
    SYSTEM_USER("SYSTEM_USER", "系统用户", "具有系统管理权限的用户"),

    /**
     * 普通用户
     * 一般业务用户，如user1、user2等
     * 拥有基本的业务功能访问权限
     */
    NORMAL_USER("NORMAL_USER", "普通用户", "一般业务用户"),

    /**
     * 访客用户
     * 临时访问用户或受限用户
     * 拥有有限的只读访问权限
     */
    GUEST("GUEST", "访客用户", "临时访问用户或受限用户");

    /**
     * 用户类型代码
     * 用于数据库存储和API传输
     */
    private final String code;

    /**
     * 用户类型名称
     * 用于界面显示
     */
    private final String name;

    /**
     * 用户类型描述
     * 详细说明该用户类型的特点和权限
     */
    private final String description;

    /**
     * 构造函数
     *
     * @param code 用户类型代码
     * @param name 用户类型名称
     * @param description 用户类型描述
     */
    UserType(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    // ==================== Getter方法 ====================

    /**
     * 获取用户类型代码
     *
     * @return 用户类型代码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取用户类型名称
     *
     * @return 用户类型名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取用户类型描述
     *
     * @return 用户类型描述
     */
    public String getDescription() {
        return description;
    }

    // ==================== 静态方法 ====================

    /**
     * 根据代码获取用户类型
     *
     * @param code 用户类型代码
     * @return 用户类型枚举，如果未找到返回null
     */
    public static UserType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        for (UserType userType : UserType.values()) {
            if (userType.code.equalsIgnoreCase(code.trim())) {
                return userType;
            }
        }
        return null;
    }

    /**
     * 根据名称获取用户类型
     *
     * @param name 用户类型名称
     * @return 用户类型枚举，如果未找到返回null
     */
    public static UserType fromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        for (UserType userType : UserType.values()) {
            if (userType.name.equalsIgnoreCase(name.trim())) {
                return userType;
            }
        }
        return null;
    }

    /**
     * 判断是否为系统用户
     *
     * @param userType 用户类型
     * @return true-系统用户，false-非系统用户
     */
    public static boolean isSystemUser(UserType userType) {
        return SYSTEM_USER.equals(userType);
    }

    /**
     * 判断是否为普通用户
     *
     * @param userType 用户类型
     * @return true-普通用户，false-非普通用户
     */
    public static boolean isNormalUser(UserType userType) {
        return NORMAL_USER.equals(userType);
    }

    /**
     * 判断是否为访客用户
     *
     * @param userType 用户类型
     * @return true-访客用户，false-非访客用户
     */
    public static boolean isGuest(UserType userType) {
        return GUEST.equals(userType);
    }

    /**
     * 获取所有用户类型代码数组
     *
     * @return 用户类型代码数组
     */
    public static String[] getAllCodes() {
        UserType[] values = UserType.values();
        String[] codes = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            codes[i] = values[i].code;
        }
        return codes;
    }

    /**
     * 获取所有用户类型名称数组
     *
     * @return 用户类型名称数组
     */
    public static String[] getAllNames() {
        UserType[] values = UserType.values();
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].name;
        }
        return names;
    }

    // ==================== 权限相关方法 ====================

    /**
     * 判断当前用户类型是否有管理权限
     *
     * @return true-有管理权限，false-无管理权限
     */
    public boolean hasAdminPermission() {
        return this == SYSTEM_USER;
    }

    /**
     * 判断当前用户类型是否有业务操作权限
     *
     * @return true-有业务操作权限，false-无业务操作权限
     */
    public boolean hasBusinessPermission() {
        return this == SYSTEM_USER || this == NORMAL_USER;
    }

    /**
     * 判断当前用户类型是否只有只读权限
     *
     * @return true-只读权限，false-有写权限
     */
    public boolean isReadOnly() {
        return this == GUEST;
    }

    /**
     * 获取用户类型的权限级别
     * 数值越大权限越高
     *
     * @return 权限级别
     */
    public int getPermissionLevel() {
        switch (this) {
            case SYSTEM_USER:
                return 100;
            case NORMAL_USER:
                return 50;
            case GUEST:
                return 10;
            default:
                return 0;
        }
    }

    // ==================== Object方法重写 ====================

    /**
     * 重写toString方法
     * 返回用户类型的代码
     *
     * @return 用户类型代码
     */
    @Override
    public String toString() {
        return this.code;
    }
}


