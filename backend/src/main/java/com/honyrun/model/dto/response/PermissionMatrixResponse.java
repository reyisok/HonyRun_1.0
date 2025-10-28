package com.honyrun.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 权限矩阵响应DTO
 *
 * 用于返回权限矩阵数据的响应数据传输对象，包含用户列表、权限列表、
 * 权限矩阵数据等信息。
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 01:30:00
 * @modified 2025-07-01 01:30:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class PermissionMatrixResponse {

    /**
     * 用户列表
     */
    private List<UserInfo> users;

    /**
     * 权限列表
     */
    private List<PermissionInfo> permissions;

    /**
     * 权限矩阵数据
     * key: userId-permissionCode, value: 是否拥有该权限
     */
    private Map<String, Boolean> matrix;

    /**
     * 权限统计信息
     */
    private StatisticsInfo statistics;

    /**
     * 数据生成时间
     */
    private LocalDateTime generatedTime;

    /**
     * 默认构造函数
     */
    public PermissionMatrixResponse() {
        this.generatedTime = LocalDateTime.now();
    }

    /**
     * 带参数的构造函数
     *
     * @param users 用户列表
     * @param permissions 权限列表
     * @param matrix 权限矩阵数据
     */
    public PermissionMatrixResponse(List<UserInfo> users, 
                                   List<PermissionInfo> permissions, 
                                   Map<String, Boolean> matrix) {
        this();
        this.users = users;
        this.permissions = permissions;
        this.matrix = matrix;
    }

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取用户列表
     *
     * @return 用户列表
     */
    public List<UserInfo> getUsers() {
        return users;
    }

    /**
     * 设置用户列表
     *
     * @param users 用户列表
     */
    public void setUsers(List<UserInfo> users) {
        this.users = users;
    }

    /**
     * 获取权限列表
     *
     * @return 权限列表
     */
    public List<PermissionInfo> getPermissions() {
        return permissions;
    }

    /**
     * 设置权限列表
     *
     * @param permissions 权限列表
     */
    public void setPermissions(List<PermissionInfo> permissions) {
        this.permissions = permissions;
    }

    /**
     * 获取权限矩阵数据
     *
     * @return 权限矩阵数据
     */
    public Map<String, Boolean> getMatrix() {
        return matrix;
    }

    /**
     * 设置权限矩阵数据
     *
     * @param matrix 权限矩阵数据
     */
    public void setMatrix(Map<String, Boolean> matrix) {
        this.matrix = matrix;
    }

    /**
     * 获取权限统计信息
     *
     * @return 权限统计信息
     */
    public StatisticsInfo getStatistics() {
        return statistics;
    }

    /**
     * 设置权限统计信息
     *
     * @param statistics 权限统计信息
     */
    public void setStatistics(StatisticsInfo statistics) {
        this.statistics = statistics;
    }

    /**
     * 获取数据生成时间
     *
     * @return 数据生成时间
     */
    public LocalDateTime getGeneratedTime() {
        return generatedTime;
    }

    /**
     * 设置数据生成时间
     *
     * @param generatedTime 数据生成时间
     */
    public void setGeneratedTime(LocalDateTime generatedTime) {
        this.generatedTime = generatedTime;
    }

    // ==================== 内部类 ====================

    /**
     * 用户信息内部类
     */
    public static class UserInfo {
        
        /**
         * 用户ID
         */
        private Long id;
        
        /**
         * 用户名
         */
        private String username;
        
        /**
         * 真实姓名
         */
        private String realName;
        
        /**
         * 用户类型
         */
        private String userType;
        
        /**
         * 用户状态
         */
        private String status;
        
        /**
         * 权限数量
         */
        private Integer permissionCount;

        /**
         * 默认构造函数
         */
        public UserInfo() {
        }

        /**
         * 带参数的构造函数
         *
         * @param id 用户ID
         * @param username 用户名
         * @param realName 真实姓名
         * @param userType 用户类型
         */
        public UserInfo(Long id, String username, String realName, String userType) {
            this.id = id;
            this.username = username;
            this.realName = realName;
            this.userType = userType;
        }

        // Getter和Setter方法
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }
        
        public String getUserType() { return userType; }
        public void setUserType(String userType) { this.userType = userType; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Integer getPermissionCount() { return permissionCount; }
        public void setPermissionCount(Integer permissionCount) { this.permissionCount = permissionCount; }
    }

    /**
     * 权限信息内部类
     */
    public static class PermissionInfo {
        
        /**
         * 权限代码
         */
        private String code;
        
        /**
         * 权限名称
         */
        private String name;
        
        /**
         * 权限描述
         */
        private String description;
        
        /**
         * 权限分类
         */
        private String category;
        
        /**
         * 权限级别
         */
        private Integer level;
        
        /**
         * 拥有该权限的用户数量
         */
        private Integer userCount;

        /**
         * 默认构造函数
         */
        public PermissionInfo() {
        }

        /**
         * 带参数的构造函数
         *
         * @param code 权限代码
         * @param name 权限名称
         * @param description 权限描述
         * @param category 权限分类
         */
        public PermissionInfo(String code, String name, String description, String category) {
            this.code = code;
            this.name = name;
            this.description = description;
            this.category = category;
        }

        // Getter和Setter方法
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public Integer getLevel() { return level; }
        public void setLevel(Integer level) { this.level = level; }
        
        public Integer getUserCount() { return userCount; }
        public void setUserCount(Integer userCount) { this.userCount = userCount; }
    }

    /**
     * 统计信息内部类
     */
    public static class StatisticsInfo {
        
        /**
         * 总用户数
         */
        private Integer totalUsers;
        
        /**
         * 总权限数
         */
        private Integer totalPermissions;
        
        /**
         * 总权限分配数
         */
        private Integer totalAssignments;
        
        /**
         * 平均每用户权限数
         */
        private Double averagePermissionsPerUser;
        
        /**
         * 平均每权限用户数
         */
        private Double averageUsersPerPermission;
        
        /**
         * 权限覆盖率
         */
        private Double permissionCoverage;

        /**
         * 默认构造函数
         */
        public StatisticsInfo() {
        }

        // Getter和Setter方法
        public Integer getTotalUsers() { return totalUsers; }
        public void setTotalUsers(Integer totalUsers) { this.totalUsers = totalUsers; }
        
        public Integer getTotalPermissions() { return totalPermissions; }
        public void setTotalPermissions(Integer totalPermissions) { this.totalPermissions = totalPermissions; }
        
        public Integer getTotalAssignments() { return totalAssignments; }
        public void setTotalAssignments(Integer totalAssignments) { this.totalAssignments = totalAssignments; }
        
        public Double getAveragePermissionsPerUser() { return averagePermissionsPerUser; }
        public void setAveragePermissionsPerUser(Double averagePermissionsPerUser) { this.averagePermissionsPerUser = averagePermissionsPerUser; }
        
        public Double getAverageUsersPerPermission() { return averageUsersPerPermission; }
        public void setAverageUsersPerPermission(Double averageUsersPerPermission) { this.averageUsersPerPermission = averageUsersPerPermission; }
        
        public Double getPermissionCoverage() { return permissionCoverage; }
        public void setPermissionCoverage(Double permissionCoverage) { this.permissionCoverage = permissionCoverage; }
    }

    // ==================== 业务方法 ====================

    /**
     * 获取用户数量
     *
     * @return 用户数量
     */
    public int getUserCount() {
        return users != null ? users.size() : 0;
    }

    /**
     * 获取权限数量
     *
     * @return 权限数量
     */
    public int getPermissionCount() {
        return permissions != null ? permissions.size() : 0;
    }

    /**
     * 获取矩阵大小
     *
     * @return 矩阵大小
     */
    public int getMatrixSize() {
        return matrix != null ? matrix.size() : 0;
    }

    /**
     * 检查指定用户是否拥有指定权限
     *
     * @param userId 用户ID
     * @param permissionCode 权限代码
     * @return 是否拥有权限
     */
    public boolean hasPermission(Long userId, String permissionCode) {
        if (matrix == null) {
            return false;
        }
        String key = userId + "-" + permissionCode;
        return matrix.getOrDefault(key, false);
    }

    // ==================== Object方法重写 ====================

    /**
     * 重写toString方法
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "PermissionMatrixResponse{" +
                "userCount=" + getUserCount() +
                ", permissionCount=" + getPermissionCount() +
                ", matrixSize=" + getMatrixSize() +
                ", generatedTime=" + generatedTime +
                ", statistics=" + statistics +
                '}';
    }
}

