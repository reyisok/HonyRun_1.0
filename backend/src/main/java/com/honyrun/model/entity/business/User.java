package com.honyrun.model.entity.business;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.honyrun.model.entity.base.AuditableEntity;
import com.honyrun.model.enums.UserType;

/**
 * 用户实体类
 *
 * 用户管理相关的实体类，支持三种用户类型（系统用户、普通用户、访客用户）
 * 继承AuditableEntity，支持R2DBC响应式数据访问和审计功能
 *
 * 重要说明：
 * 1. 用户ID使用雪花算法生成64位长整型唯一标识
 * 2. 确保分布式环境下用户ID的全局唯一性
 * 3. 所有用户相关操作必须使用Long类型的用户ID
 * 4. 测试环境和生产环境保持ID类型一致性
 *
 * 雪花算法特性：
 * - 高性能：单机每秒可生成400万个用户ID
 * - 唯一性：分布式环境下保证全局唯一
 * - 有序性：生成的用户ID按时间递增
 * - 可读性：ID包含时间信息，便于调试和追踪
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 17:00:00
 * @modified 2025-07-01 16:00:00
 * @see com.honyrun.util.SnowflakeIdGenerator
 *      Copyright © 2025 HonyRun. All rights reserved.
 */
@Table("sys_users")
public class User extends AuditableEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     * 用于登录的唯一标识，不可重复
     */
    @Column("username")
    private String username;

    /**
     * 密码
     * 存储加密后的密码，不能明文存储
     */
    @Column("password_hash")
    private String password;

    /**
     * 用户类型
     * 支持三种类型：SYSTEM_USER、NORMAL_USER、GUEST
     */
    @Column("user_type")
    private UserType userType;

    /**
     * 真实姓名
     * 用户的真实姓名，用于显示和识别
     */
    @Column("real_name")
    private String realName;

    /**
     * 邮箱地址
     * 用户的邮箱地址，可用于找回密码等功能
     */
    @Column("email")
    private String email;

    /**
     * 手机号码
     * 用户的手机号码，可用于短信通知等功能
     */
    @Column("phone")
    private String phone;

    /**
     * 用户状态 - 使用String类型以保持数据库VARCHAR字段一致
     * ACTIVE: 激活, INACTIVE: 未激活, DISABLED: 禁用, LOCKED: 锁定
     */
    @Column("status")
    private String status = "ACTIVE";

    /**
     * 账户启用状态 - 对应数据库enabled字段
     * true: 启用, false: 禁用
     * 注意：此字段与status字段保持同步
     */
    @Column("enabled")
    private Boolean enabled = true;

    /**
     * 账户有效期开始时间
     * 账户生效的开始时间
     */
    @Column("valid_from")
    private LocalDateTime validFrom;

    // 移除数据库表中不存在的字段：
    // - validTo (expiry_date)
    // - lastLoginTime (last_login_time)
    // - lastLoginIp (last_login_ip)
    // - loginFailureCount (failed_login_attempts)
    // - lockedTime (locked_until)

    // 从父类BaseEntity继承的deleted和remark字段已在BaseEntity中标记为@Transient
    // 无需在此处重复定义，避免字段重复声明

    /**
     * 默认构造函数
     */
    public User() {
        super();
        this.validFrom = LocalDateTime.now();
        this.enabled = true; // 默认启用状态
    }

    /**
     * 带用户名的构造函数
     *
     * @param username 用户名
     */
    public User(String username) {
        this();
        this.username = username;
    }

    /**
     * 完整构造函数
     *
     * @param username 用户名
     * @param password 密码
     * @param userType 用户类型
     * @param realName 真实姓名
     */
    public User(String username, String password, UserType userType, String realName) {
        this(username);
        this.password = password;
        this.userType = userType;
        this.realName = realName;
    }

    // ==================== Getter和Setter方法 ====================

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
     * 获取用户类型
     *
     * @return 用户类型
     */
    public UserType getUserType() {
        return userType;
    }

    /**
     * 设置用户类型
     *
     * @param userType 用户类型
     */
    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    /**
     * 获取真实姓名
     *
     * @return 真实姓名
     */
    public String getRealName() {
        return realName;
    }

    /**
     * 设置真实姓名
     *
     * @param realName 真实姓名
     */
    public void setRealName(String realName) {
        this.realName = realName;
    }

    /**
     * 获取全名（别名方法，与setFullName对应）
     *
     * @return 全名
     */
    public String getFullName() {
        return realName;
    }

    /**
     * 设置全名（别名方法，映射到realName字段）
     *
     * @param fullName 全名
     */
    public void setFullName(String fullName) {
        this.realName = fullName;
    }

    /**
     * 获取邮箱地址
     *
     * @return 邮箱地址
     */
    public String getEmail() {
        return email;
    }

    /**
     * 设置邮箱地址
     *
     * @param email 邮箱地址
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 获取手机号码
     *
     * @return 手机号码
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 设置手机号码
     *
     * @param phone 手机号码
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * 获取账户状态
     *
     * @return 账户状态，ACTIVE-激活，INACTIVE-未激活，DISABLED-禁用，LOCKED-锁定
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置账户状态
     *
     * @param status 账户状态，ACTIVE-激活，INACTIVE-未激活，DISABLED-禁用，LOCKED-锁定
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取账户有效期开始时间
     *
     * @return 账户有效期开始时间
     */
    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    /**
     * 设置账户有效期开始时间
     *
     * @param validFrom 账户有效期开始时间
     */
    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    /**
     * 删除标记字段
     * 0-未删除，1-已删除
     * 用于逻辑删除，避免物理删除数据
     */
    @Column("deleted")
    private Integer deleted = 0;

    /**
     * 获取删除标记
     *
     * @return 删除标记，0-未删除，1-已删除
     */
    public Integer getDeleted() {
        return deleted;
    }

    /**
     * 设置删除标记
     *
     * @param deleted 删除标记，0-未删除，1-已删除
     */
    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    /**
     * 覆盖父类的getRemark方法
     *
     * @return 备注信息，对于User实体始终返回null
     */
    @Override
    public String getRemark() {
        return null;
    }

    /**
     * 判断实体是否已删除
     *
     * @return true-已删除，false-未删除
     */
    public boolean isDeleted() {
        return this.deleted != null && this.deleted == 1;
    }

    /**
     * 标记实体为已删除
     */
    public void markDeleted() {
        this.deleted = 1;
    }

    /**
     * 标记实体为未删除
     */
    public void markUndeleted() {
        this.deleted = 0;
    }

    // ==================== 业务方法 ====================

    /**
     * 判断账户是否启用
     * 优先使用enabled字段，如果为null则根据status字段判断
     *
     * @return true-启用，false-禁用
     */
    public boolean isEnabled() {
        if (this.enabled != null) {
            return this.enabled;
        }
        return this.status != null && "ACTIVE".equals(this.status);
    }

    /**
     * 获取账户启用状态（别名方法，与setEnabled对应）
     *
     * @return true-启用，false-禁用
     */
    public boolean getEnabled() {
        return isEnabled();
    }

    /**
     * 设置账户启用状态
     * 同时更新enabled字段和status字段以保持一致性
     *
     * @param enabled true-启用，false-禁用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.status = enabled ? "ACTIVE" : "DISABLED";
    }

    /**
     * 获取修改时间（别名方法，与setModifiedDate对应）
     *
     * @return 修改时间
     */
    public LocalDateTime getModifiedDate() {
        return getLastModifiedDate();
    }

    /**
     * 设置修改时间（别名方法，映射到lastModifiedDate字段）
     *
     * @param modifiedDate 修改时间
     */
    public void setModifiedDate(LocalDateTime modifiedDate) {
        setLastModifiedDate(modifiedDate);
    }

    /**
     * 判断账户是否在有效期内
     *
     * @return true-有效，false-无效
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();

        // 检查开始时间
        if (this.validFrom != null && now.isBefore(this.validFrom)) {
            return false;
        }

        // 注意：validTo字段已从数据库表中移除，不再检查结束时间
        // 如需要有效期结束时间功能，需要在数据库表中添加相应字段

        return true;
    }

    /**
     * 判断账户是否被锁定
     * 基于状态字段判断是否锁定
     *
     * @return true-锁定，false-未锁定
     */
    public boolean isLocked() {
        return "LOCKED".equals(this.status);
    }

    /**
     * 判断账户是否可用（启用且有效且未锁定且未删除）
     *
     * @return true-可用，false-不可用
     */
    public boolean isAccountNonExpired() {
        return isEnabled() && isValid() && !isLocked() && !isDeleted();
    }

    /**
     * 判断是否为系统用户
     *
     * @return true-系统用户，false-非系统用户
     */
    public boolean isSystemUser() {
        return UserType.isSystemUser(this.userType);
    }

    /**
     * 判断是否为普通用户
     *
     * @return true-普通用户，false-非普通用户
     */
    public boolean isNormalUser() {
        return UserType.isNormalUser(this.userType);
    }

    /**
     * 判断是否为访客用户
     *
     * @return true-访客用户，false-非访客用户
     */
    public boolean isGuest() {
        return UserType.isGuest(this.userType);
    }

    /**
     * 启用账户
     */
    public void enable() {
        this.status = "ACTIVE";
    }

    /**
     * 禁用账户
     */
    public void disable() {
        this.status = "DISABLED";
    }

    /**
     * 锁定账户
     */
    public void lock() {
        // 数据库表中没有锁定时间字段，仅更新状态
        this.status = "LOCKED";
    }

    /**
     * 解锁账户
     */
    public void unlock() {
        // 数据库表中没有锁定时间和登录失败次数字段，仅更新状态
        this.status = "ACTIVE";
    }

    /**
     * 增加登录失败次数
     */
    public void incrementLoginFailureCount() {
        // 数据库表中没有登录失败次数字段，此方法暂时保留但不执行操作
        // 可以通过日志记录或其他方式处理登录失败
    }

    /**
     * 重置登录失败次数
     */
    public void resetLoginFailureCount() {
        // 数据库表中没有登录失败次数字段，此方法暂时保留但不执行操作
    }

    /**
     * 更新最后登录信息
     *
     * @param loginIp 登录IP地址
     */
    public void updateLastLoginInfo(String loginIp) {
        // 数据库表中没有最后登录时间和IP字段，此方法暂时保留但不执行操作
        // 可以通过日志记录或其他方式处理登录信息
    }

    // ==================== Object方法重写 ====================

    /**
     * 重写equals方法
     * 基于用户名进行比较
     *
     * @param obj 比较对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        User user = (User) obj;

        // 如果ID都不为空，则基于ID比较
        if (this.getId() != null && user.getId() != null) {
            return Objects.equals(this.getId(), user.getId());
        }

        // 如果用户名都不为空，则基于用户名比较
        if (this.username != null && user.username != null) {
            return Objects.equals(this.username, user.username);
        }

        return super.equals(obj);
    }

    /**
     * 重写hashCode方法
     * 基于用户名生成哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        if (this.getId() != null) {
            return Objects.hash(this.getId());
        }
        if (this.username != null) {
            return Objects.hash(this.username);
        }
        return super.hashCode();
    }

    /**
     * 重写toString方法
     * 提供用户实体的字符串表示（不包含敏感信息）
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", username='" + username + '\'' +
                ", userType=" + userType +
                ", realName='" + realName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", status='" + status + '\'' +
                ", enabled=" + enabled +
                ", validFrom=" + validFrom +
                ", deleted=" + getDeleted() +
                '}';
    }
}
