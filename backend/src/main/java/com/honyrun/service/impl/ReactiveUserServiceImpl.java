package com.honyrun.service.impl;

import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.r2dbc.BadSqlGrammarException;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.honyrun.config.MonitoringProperties;
import com.honyrun.constant.CacheConstants;
import com.honyrun.exception.BusinessException;
import com.honyrun.exception.ErrorCode;
import com.honyrun.exception.ValidationException;
import com.honyrun.model.dto.reactive.ReactiveActiveActivity;
import com.honyrun.model.dto.request.UserCreateRequest;
import com.honyrun.model.dto.request.UserUpdateRequest;
import com.honyrun.model.dto.response.UserResponse;
import com.honyrun.model.entity.business.User;
import com.honyrun.model.entity.business.UserPermission;
import com.honyrun.model.enums.UserType;
import com.honyrun.repository.custom.CustomUserRepository;
import com.honyrun.repository.r2dbc.ReactiveUserRepository;
import com.honyrun.service.reactive.ReactivePasswordSecurityService;
import com.honyrun.service.reactive.ReactiveUserService;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.SnowflakeIdGenerator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 响应式用户服务实现类 - 简化权限模型
 * 【权限架构】：采用用户-权限直接映射模型，去除角色中间层
 * 【用户类型标准化】：严格使用SYSTEM_USER、NORMAL_USER、GUEST，禁止role概念
 * 【项目规则56】：本项目任何地方禁止出现role相关的代码、逻辑、目录、包
 * 【项目规则58】：用户类型必须使用"SYSTEM_USER"、"NORMAL_USER"、"GUEST"，不得出现任何其他简化版本
 *
 * 实现ReactiveUserService接口，提供用户管理相关的业务逻辑
 * 包含用户CRUD操作、权限管理、状态控制等功能的具体实现
 * 所有方法返回Mono或Flux类型，支持响应式数据流处理
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 20:00:00
 * @modified 2025-01-15 当前时间
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
@Service
@Transactional
public class ReactiveUserServiceImpl implements ReactiveUserService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveUserServiceImpl.class);

    private final ReactiveUserRepository userRepository;
    private final CustomUserRepository customUserRepository;
    private final DatabaseClient databaseClient;
    private final PasswordEncoder passwordEncoder;
    private final ReactivePasswordSecurityService passwordSecurityService;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final MonitoringProperties monitoringProperties;

    /**
     * 构造函数
     */
    public ReactiveUserServiceImpl(ReactiveUserRepository userRepository,
            CustomUserRepository customUserRepository,
            DatabaseClient databaseClient,
            PasswordEncoder passwordEncoder,
            ReactivePasswordSecurityService passwordSecurityService,
            SnowflakeIdGenerator snowflakeIdGenerator,
            MonitoringProperties monitoringProperties) {
        this.userRepository = userRepository;
        this.customUserRepository = customUserRepository;
        this.databaseClient = databaseClient;
        this.passwordEncoder = passwordEncoder;
        this.passwordSecurityService = passwordSecurityService;
        this.snowflakeIdGenerator = snowflakeIdGenerator;
        this.monitoringProperties = monitoringProperties;
    }

    // ==================== 基础用户操作 ====================

    @Override
    @CacheEvict(value = CacheConstants.CACHE_USER, allEntries = true)
    public Mono<UserResponse> createUser(UserCreateRequest request) {
        LoggingUtil.info(logger, "创建用户，用户名: {}", request.getUsername());

        return validateCreateRequest(request)
                .then(buildUserFromRequest(request))
                .flatMap(userRepository::save)
                .onErrorMap(this::mapUniqueConstraintViolation)
                .map(this::convertToUserResponse)
                .doOnSuccess(user -> LoggingUtil.info(logger, "用户创建成功，ID: {}", user.getId()))
                .doOnError(error -> LoggingUtil.error(logger, "用户创建失败", error));
    }

    @Override
    @Cacheable(value = CacheConstants.CACHE_USER, key = "#id")
    public Mono<UserResponse> getUserById(Long id) {
        LoggingUtil.debug(logger, "根据ID查询用户: {}", id);

        return userRepository.findByIdAndDeletedFalse(id)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.USER_NOT_FOUND)))
                .map(this::convertToUserResponse)
                .doOnSuccess(user -> LoggingUtil.debug(logger, "用户查询成功"))
                .doOnError(error -> LoggingUtil.error(logger, "用户查询失败", error));
    }

    @Override
    @Cacheable(value = CacheConstants.CACHE_USER, key = "#username")
    public Mono<UserResponse> getUserByUsername(String username) {
        LoggingUtil.debug(logger, "根据用户名查询用户: {}", username);

        if (!StringUtils.hasText(username)) {
            return Mono.error(new ValidationException("用户名不能为空"));
        }

        return userRepository.findByUsernameAndDeletedFalse(username)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.USER_NOT_FOUND)))
                .map(this::convertToUserResponse)
                .doOnSuccess(user -> LoggingUtil.debug(logger, "用户查询成功"))
                .doOnError(error -> LoggingUtil.error(logger, "用户查询失败", error));
    }

    @Override
    @CacheEvict(value = CacheConstants.CACHE_USER, key = "#id")
    public Mono<UserResponse> updateUser(Long id, UserUpdateRequest request) {
        LoggingUtil.info(logger, "更新用户，ID: {}", id);

        return validateUpdateRequest(request)
                .then(userRepository.findByIdAndDeletedFalse(id))
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.USER_NOT_FOUND)))
                .flatMap(user -> updateUserFromRequest(user, request))
                .flatMap(userRepository::save)
                .map(this::convertToUserResponse)
                .doOnSuccess(user -> LoggingUtil.info(logger, "用户更新成功"))
                .doOnError(error -> LoggingUtil.error(logger, "用户更新失败", error));
    }

    @Override
    @CacheEvict(value = CacheConstants.CACHE_USER, key = "#id")
    public Mono<Boolean> deleteUser(Long id) {
        LoggingUtil.info(logger, "删除用户，ID: {}", id);

        return userRepository.findByIdAndDeletedFalse(id)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.USER_NOT_FOUND)))
                .flatMap(user -> {
                    user.setDeleted(1); // 使用Integer类型的1表示已删除
                    user.setModifiedDate(LocalDateTime.now());
                    return userRepository.save(user);
                })
                .then(Mono.just(true))
                .doOnSuccess(result -> LoggingUtil.info(logger, "用户删除成功"))
                .doOnError(error -> LoggingUtil.error(logger, "用户删除失败", error));
    }

    @Override
    public Flux<UserResponse> getAllUsers(int page, int size) {
        LoggingUtil.debug(logger, "分页查询所有用户，页码: {}, 大小: {}", page, size);

        return validatePageParams(page, size)
                .thenMany(Flux.defer(() -> userRepository.findByDeletedFalse()
                        .skip((long) page * size)
                        .take(size)
                        .map(this::convertToUserResponse)
                        .doOnError(error -> LoggingUtil.error(logger, "用户数据转换失败", error))
                        .onErrorResume(error -> {
                            LoggingUtil.error(logger, "用户查询过程中发生错误", error);
                            return Flux.empty();
                        })))
                .doOnComplete(() -> LoggingUtil.debug(logger, "用户查询完成"))
                .doOnError(error -> LoggingUtil.error(logger, "用户查询失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "分页查询用户失败", error);
                    return Flux.empty();
                });
    }

    // ==================== 权限管理 ====================

    public Mono<Void> updateUserPermissions(Long userId, List<String> permissionCodes) {
        LoggingUtil.info(logger, "更新用户权限，用户ID: {}, 权限数量: {}", userId, permissionCodes.size());

        return validateUserId(userId)
                .then(validatePermissionCodes(permissionCodes))
                .then(deleteUserPermissions(userId))
                .then(insertUserPermissions(userId, permissionCodes))
                .then()
                .doOnSuccess(v -> LoggingUtil.info(logger, "用户权限更新成功"))
                .doOnError(error -> LoggingUtil.error(logger, "用户权限更新失败", error));
    }

    public Flux<String> getUserPermissionCodes(Long userId) {
        LoggingUtil.debug(logger, "查询用户权限代码，用户ID: {}", userId);

        return validateUserId(userId)
                .thenMany(databaseClient.sql("""
                        SELECT up.permission AS permission_name
                        FROM user_permissions up
                        WHERE up.user_id = :userId
                          AND up.is_active = 1
                        """)
                        .bind("userId", userId)
                        .map(row -> row.get("permission_name", String.class))
                        .all()
                        .doOnError(error -> LoggingUtil.error(logger, "数据库查询权限代码失败", error))
                        .onErrorResume(error -> {
                            LoggingUtil.error(logger, "权限代码查询过程中发生错误", error);
                            return Flux.empty();
                        }))
                .doOnComplete(() -> LoggingUtil.debug(logger, "用户权限代码查询完成"))
                .doOnError(error -> LoggingUtil.error(logger, "用户权限代码查询失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "获取用户权限代码失败", error);
                    return Flux.empty();
                });
    }

    // ==================== 状态管理 ====================

    @Override
    public Mono<Boolean> updateUserStatus(Long userId, String status) {
        LoggingUtil.info(logger, "更新用户状态，用户ID: {}, 状态: {}", userId, status);

        return validateUserId(userId)
                .then(validateUserStatus(status))
                .then(userRepository.findByIdAndDeletedFalse(userId))
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.USER_NOT_FOUND)))
                .flatMap(user -> {
                    user.setEnabled("ACTIVE".equals(status));
                    user.setModifiedDate(LocalDateTime.now());
                    return userRepository.save(user);
                })
                .map(user -> true)
                .doOnSuccess(result -> LoggingUtil.info(logger, "用户状态更新成功"))
                .doOnError(error -> LoggingUtil.error(logger, "用户状态更新失败", error));
    }

    public Mono<Long> getActiveUserCount() {
        LoggingUtil.debug(logger, "查询活跃用户数量");

        return databaseClient.sql("""
                SELECT user_type, COUNT(*) as count
                FROM sys_users
                WHERE deleted = 0
                GROUP BY user_type
                """)
                .map(row -> row.get("count", Long.class))
                .one()
                .defaultIfEmpty(0L)
                .onErrorMap(this::mapDataAccessException)
                .doOnSuccess(count -> LoggingUtil.debug(logger, "活跃用户数量: {}", count))
                .doOnError(error -> LoggingUtil.error(logger, "活跃用户数量查询失败", error));
    }

    // ==================== 活动记录 ====================

    @Override
    public Flux<ReactiveActiveActivity> getUserActivities(Long userId) {
        LoggingUtil.debug(logger, "查询用户活动记录，用户ID: {}", userId);

        return validateUserId(userId)
                .thenMany(databaseClient.sql("""
                        SELECT * FROM user_activity_log
                        WHERE user_id = :userId
                        ORDER BY activity_time DESC
                        LIMIT :limit
                        """)
                        .bind("userId", userId)
                        .bind("limit", monitoringProperties.getConstants().getQueryLimit())
                        .map(row -> ReactiveActiveActivity.builder()
                                .id(row.get("id", Long.class))
                                .userId(row.get("user_id", Long.class))
                                .userType(row.get("activity_type", String.class))
                                .lastActivityTime(row.get("activity_time", LocalDateTime.class))
                                .clientIp(row.get("ip_address", String.class))
                                .userAgent(row.get("user_agent", String.class))
                                .build())
                        .all()
                        .cast(ReactiveActiveActivity.class)
                        .doOnError(error -> LoggingUtil.error(logger, "数据库查询用户活动记录失败", error))
                        .onErrorResume(error -> {
                            LoggingUtil.error(logger, "用户活动记录查询过程中发生错误", error);
                            return Flux.empty();
                        }))
                .doOnComplete(() -> LoggingUtil.debug(logger, "用户活动记录查询完成"))
                .doOnError(error -> LoggingUtil.error(logger, "用户活动记录查询失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "获取用户活动记录失败", error);
                    return Flux.empty();
                });
    }

    // ==================== 统计信息 ====================

    @Override
    public Mono<Map<String, Object>> getUserStatistics() {
        LoggingUtil.debug(logger, "查询用户统计信息");

        return Mono.zip(
                countUsers(),
                getActiveUserCount(),
                getUserTypeDistribution())
                .map(tuple -> Map.of(
                        "totalUsers", tuple.getT1(),
                        "activeUsers", tuple.getT2(),
                        "typeDistribution", tuple.getT3()))
                .switchIfEmpty(Mono.just(Map.of(
                        "totalUsers", 0L,
                        "activeUsers", 0L,
                        "typeDistribution", Collections.emptyMap())))
                .onErrorMap(this::mapDataAccessException)
                .doOnSuccess(stats -> LoggingUtil.debug(logger, "用户统计信息查询完成"))
                .doOnError(error -> LoggingUtil.error(logger, "用户统计信息查询失败", error));
    }

    public Mono<Map<String, Long>> getUserRegistrationStats() {
        LoggingUtil.debug(logger, "查询用户注册统计");

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(monitoringProperties.getConstants().getDefaultDaysRange());

        return databaseClient.sql("""
                SELECT
                    DATE(created_at) as date,
                    COUNT(*) as count
                FROM sys_users
                WHERE deleted = 0 AND created_at BETWEEN :startDate AND :endDate
                GROUP BY DATE(created_at)
                ORDER BY date DESC
                """)
                .bind("startDate", startDate)
                .bind("endDate", endDate)
                .map(row -> new AbstractMap.SimpleEntry<>(
                        row.get("date", String.class),
                        row.get("count", Long.class)))
                .all()
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .switchIfEmpty(Mono.just(Collections.emptyMap()))
                .onErrorMap(this::mapDataAccessException)
                .doOnSuccess(stats -> LoggingUtil.debug(logger, "用户注册统计查询完成"))
                .doOnError(error -> LoggingUtil.error(logger, "用户注册统计查询失败", error));
    }

    @Override
    public Mono<Map<UserType, Long>> getUserTypeDistribution() {
        LoggingUtil.debug(logger, "查询用户类型分布");

        return databaseClient.sql("""
                SELECT user_type, COUNT(*) as count
                FROM sys_users
                WHERE deleted = 0
                GROUP BY user_type
                """)
                .map(row -> new AbstractMap.SimpleEntry<>(
                        UserType.fromCode(row.get("user_type", String.class)),
                        row.get("count", Long.class)))
                .all()
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .switchIfEmpty(Mono.just(Collections.emptyMap()))
                .onErrorMap(this::mapDataAccessException)
                .doOnSuccess(distribution -> LoggingUtil.debug(logger, "用户类型分布查询完成"))
                .doOnError(error -> LoggingUtil.error(logger, "用户类型分布查询失败", error));
    }

    @Override
    public Flux<UserResponse> getRecentlyLoggedInUsers(int limit) {
        LoggingUtil.debug(logger, "查询最近登录用户，限制数量: {}", limit);

        return databaseClient.sql("""
                SELECT DISTINCT u.* FROM sys_users u
                INNER JOIN user_activity_log ual ON u.id = ual.user_id
                WHERE u.deleted = 0 AND ual.activity_type = 'LOGIN'
                ORDER BY ual.activity_time DESC
                LIMIT :limit
                """)
                .bind("limit", limit)
                .map((row, metadata) -> mapRowToUser(row))
                .all()
                .map(this::convertToUserResponse)
                .doOnComplete(() -> LoggingUtil.debug(logger, "最近登录用户查询完成"))
                .doOnError(error -> LoggingUtil.error(logger, "最近登录用户查询失败", error));
    }

    @Override
    public Mono<Long> countUsers() {
        LoggingUtil.debug(logger, "统计用户总数");

        return userRepository.countByDeletedFalse()
                .defaultIfEmpty(0L)
                .onErrorMap(this::mapDataAccessException)
                .doOnSuccess(count -> LoggingUtil.debug(logger, "用户总数: {}", count))
                .doOnError(error -> LoggingUtil.error(logger, "用户总数统计失败", error));
    }

    @Override
    public Mono<Long> countUsersByType(UserType userType) {
        LoggingUtil.debug(logger, "统计指定类型用户数量，类型: {}", userType);

        return userRepository.countByUserTypeAndDeletedFalse(userType)
                .defaultIfEmpty(0L)
                .onErrorMap(this::mapDataAccessException)
                .doOnSuccess(count -> LoggingUtil.debug(logger, "用户数量: {}", count))
                .doOnError(error -> LoggingUtil.error(logger, "用户数量统计失败", error));
    }

    @Override
    public Mono<Boolean> existsByUsername(String username) {
        LoggingUtil.debug(logger, "检查用户名是否存在: {}", username);

        return userRepository.countByUsernameAndDeletedFalse(username)
                .map(count -> count > 0)
                .doOnSuccess(exists -> LoggingUtil.debug(logger, "用户名存在性检查结果: {}", exists))
                .doOnError(error -> LoggingUtil.error(logger, "用户名存在性检查失败", error));
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        LoggingUtil.debug(logger, "检查邮箱是否存在: {}", email);

        return userRepository.countByEmailAndDeletedFalse(email)
                .map(count -> count > 0)
                .doOnSuccess(exists -> LoggingUtil.debug(logger, "邮箱存在性检查结果: {}", exists))
                .doOnError(error -> LoggingUtil.error(logger, "邮箱存在性检查失败", error));
    }

    // ==================== 新增方法实现 ====================

    @Override
    public Mono<Boolean> changePassword(Long userId, String oldPassword, String newPassword) {
        LoggingUtil.info(logger, "用户修改密码，用户ID: {}", userId);

        return validateUserId(userId)
                .then(userRepository.findByIdAndDeletedFalse(userId))
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.USER_NOT_FOUND)))
                .flatMap(user -> {
                    // 验证旧密码
                    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                        return Mono.error(new BusinessException(ErrorCode.INVALID_PASSWORD));
                    }

                    // 检查新密码是否与历史密码重复
                    return passwordSecurityService.isPasswordUsedBefore(userId, newPassword)
                            .flatMap(isDuplicate -> {
                                if (isDuplicate) {
                                    return Mono.error(new BusinessException(ErrorCode.PASSWORD_REUSED));
                                }

                                // 更新密码
                                user.setPassword(passwordEncoder.encode(newPassword));
                                user.setModifiedDate(LocalDateTime.now());
                                return userRepository.save(user)
                                        .then(passwordSecurityService.savePasswordHistory(userId,
                                                passwordEncoder.encode(newPassword), "SYSTEM", "密码重置"))
                                        .thenReturn(true);
                            });
                })
                .doOnSuccess(result -> LoggingUtil.info(logger, "用户密码修改成功"))
                .doOnError(error -> LoggingUtil.error(logger, "用户密码修改失败", error));
    }

    @Override
    public Mono<Boolean> resetPassword(Long userId, String newPassword) {
        LoggingUtil.info(logger, "重置用户密码，用户ID: {}", userId);

        return validateUserId(userId)
                .then(userRepository.findByIdAndDeletedFalse(userId))
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.USER_NOT_FOUND)))
                .flatMap(user -> {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    user.setModifiedDate(LocalDateTime.now());
                    return userRepository.save(user)
                            .then(passwordSecurityService.savePasswordHistory(userId,
                                    passwordEncoder.encode(newPassword), "SYSTEM", "密码重置"))
                            .thenReturn(true);
                })
                .doOnSuccess(result -> LoggingUtil.info(logger, "用户密码重置成功"))
                .doOnError(error -> LoggingUtil.error(logger, "用户密码重置失败", error));
    }

    @Override
    public Mono<Boolean> enableUser(Long userId) {
        LoggingUtil.info(logger, "启用用户，用户ID: {}", userId);

        return updateUserStatus(userId, "ACTIVE").then(Mono.just(true));
    }

    @Override
    public Mono<Boolean> disableUser(Long userId) {
        LoggingUtil.info(logger, "禁用用户，用户ID: {}", userId);

        return updateUserStatus(userId, "INACTIVE").then(Mono.just(true));
    }

    @Override
    public Mono<Boolean> assignPermissions(Long userId, List<String> permissionCodes) {
        LoggingUtil.info(logger, "分配用户权限，用户ID: {}, 权限数量: {}", userId, permissionCodes.size());

        return validateUserId(userId)
                .then(validatePermissionCodes(permissionCodes))
                .then(databaseClient.sql("""
                        SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END as result
                        FROM (
                            INSERT INTO user_permissions (
                                user_id, permission, is_active, granted_by, granted_at,
                                created_at, updated_at
                            )
                            VALUES (:userId, :permission, 1, :grantedBy, :createdDate,
                                   :createdDate, :createdDate)
                            RETURNING 1
                        ) AS inserted_rows
                        """)
                        .bind("userId", userId)
                        .bind("permissionCodes", permissionCodes)
                        .bind("grantedBy", userId)
                        .bind("createdDate", LocalDateTime.now())
                        .bind("createdBy", userId)
                        .bind("lastModifiedBy", userId)
                        .map(row -> row.get("result", Boolean.class))
                        .one())
                .doOnSuccess(result -> LoggingUtil.info(logger, "用户权限分配完成，结果: {}", result))
                .doOnError(error -> LoggingUtil.error(logger, "用户权限分配失败", error));
    }

    @Override
    public Mono<Boolean> revokePermissions(Long userId, List<String> permissionCodes) {
        LoggingUtil.info(logger, "撤销用户权限，用户ID: {}, 权限数量: {}", userId, permissionCodes.size());

        return validateUserId(userId)
                .then(validatePermissionCodes(permissionCodes))
                .then(databaseClient.sql("""
                        SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END as result
                        FROM (
                            UPDATE user_permissions up
                            SET is_active = 0, updated_at = :modifiedDate
                            WHERE up.user_id = :userId
                              AND up.is_active = 1
                              AND up.permission IN (:permissionCodes)
                            RETURNING 1
                        ) AS updated_rows
                        """)
                        .bind("userId", userId)
                        .bind("permissionCodes", permissionCodes)
                        .bind("modifiedDate", LocalDateTime.now())
                        .map(row -> row.get("result", Boolean.class))
                        .one())
                .doOnSuccess(result -> LoggingUtil.info(logger, "用户权限撤销完成，结果: {}", result))
                .doOnError(error -> LoggingUtil.error(logger, "用户权限撤销失败", error));
    }

    @Override
    public Flux<UserPermission> getUserPermissions(Long userId) {
        LoggingUtil.debug(logger, "查询用户权限对象，用户ID: {}", userId);

        return validateUserId(userId)
                .thenMany(databaseClient.sql("""
                        SELECT
                            up.id,
                            up.user_id,
                            up.permission AS permission_code,
                            up.granted_at AS granted_time,
                            up.granted_by,
                            CASE WHEN up.is_active THEN 1 ELSE 0 END AS status,
                            up.created_at,
                            up.updated_at
                        FROM user_permissions up
                        WHERE up.user_id = :userId
                          AND up.is_active = 1
                        """)
                        .bind("userId", userId)
                        .map(row -> {
                            UserPermission permission = new UserPermission();
                            permission.setId(row.get("id", Long.class));
                            permission.setUserId(row.get("user_id", Long.class));
                            permission.setPermission(row.get("permission", String.class));
                            permission.setGrantedAt(row.get("granted_at", LocalDateTime.class));
                            permission.setGrantedBy(row.get("granted_by", Long.class));
                            permission.setIsActive(row.get("is_active", Integer.class) == 1);
                            permission.setCreatedDate(row.get("created_at", LocalDateTime.class));
                            permission.setLastModifiedDate(row.get("updated_at", LocalDateTime.class));
                            return permission;
                        })
                        .all())
                .doOnComplete(() -> LoggingUtil.debug(logger, "用户权限对象查询完成"))
                .doOnError(error -> LoggingUtil.error(logger, "用户权限对象查询失败", error));
    }

    @Override
    public Mono<Boolean> hasPermission(Long userId, String permissionCode) {
        LoggingUtil.debug(logger, "检查用户权限，用户ID: {}, 权限代码: {}", userId, permissionCode);

        // 直接进行参数验证，不使用validateUserId方法
        if (userId == null || userId <= 0) {
            LoggingUtil.warn(logger, "用户ID无效: {}", userId);
            return Mono.just(false);
        }

        return userRepository.findById(userId)
                .flatMap(user -> {
                    // 【项目规则58】使用完整用户类型版本"SYSTEM_USER"而非简化版本"SYSTEM"
                    // SYSTEM_USER类型拥有所有权限，无需检查具体权限记录
                    if (user.getUserType() == UserType.SYSTEM_USER) {
                        LoggingUtil.debug(logger, "SYSTEM_USER类型用户拥有所有权限，用户ID: {}", userId);
                        return Mono.just(true);
                    }

                    // 其他用户类型需要检查数据库权限记录
                    return databaseClient.sql("""
                            SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END as result
                            FROM user_permissions up
                            WHERE up.user_id = :userId
                              AND up.permission = :permissionCode
                              AND up.is_active = 1
                            """)
                            .bind("userId", userId)
                            .bind("permissionCode", permissionCode)
                            .map(row -> row.get("result", Boolean.class))
                            .one();
                })
                .switchIfEmpty(Mono.just(false)) // 用户不存在时返回false
                .doOnSuccess(hasPermission -> LoggingUtil.debug(logger, "用户权限检查结果: {}", hasPermission))
                .doOnError(error -> LoggingUtil.error(logger, "用户权限检查失败", error));
    }

    @Override
    public Mono<Map<String, Boolean>> hasPermissions(Long userId, List<String> permissionCodes) {
        LoggingUtil.debug(logger, "批量检查用户权限，用户ID: {}, 权限数量: {}", userId, permissionCodes.size());

        return validateUserId(userId)
                .then(Flux.fromIterable(permissionCodes)
                        .flatMap(permissionCode -> hasPermission(userId, permissionCode)
                                .map(hasPermission -> Map.entry(permissionCode, hasPermission)))
                        .collectMap(Map.Entry::getKey, Map.Entry::getValue))
                .doOnSuccess(result -> LoggingUtil.debug(logger, "批量权限检查完成，结果数量: {}", result.size()))
                .doOnError(error -> LoggingUtil.error(logger, "批量权限检查失败", error));
    }

    @Override
    public Mono<Boolean> clearUserPermissions(Long userId) {
        LoggingUtil.debug(logger, "清除用户权限，用户ID: {}", userId);

        return validateUserId(userId)
                .then(databaseClient.sql("""
                        SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END as result
                        FROM (
                            UPDATE user_permissions
                            SET is_active = 0, deleted = 1, last_modified_date = :modifiedDate
                            WHERE user_id = :userId
                              AND is_active = 1
                              AND deleted = 0
                            RETURNING 1
                        ) AS updated_rows
                        """)
                        .bind("userId", userId)
                        .bind("modifiedDate", LocalDateTime.now())
                        .map(row -> row.get("result", Boolean.class))
                        .one())
                .doOnSuccess(result -> LoggingUtil.debug(logger, "用户权限清除完成，结果: {}", result))
                .doOnError(error -> LoggingUtil.error(logger, "用户权限清除失败", error));
    }

    @Override
    public Flux<UserResponse> getUsersByType(UserType userType) {
        LoggingUtil.debug(logger, "根据用户类型查询用户，类型: {}", userType);

        return userRepository.findByUserTypeAndDeletedFalse(userType)
                .map(this::convertToUserResponse)
                .doOnComplete(() -> LoggingUtil.debug(logger, "根据用户类型查询完成"))
                .doOnError(error -> LoggingUtil.error(logger, "根据用户类型查询失败", error));
    }

    @Override
    public Flux<UserResponse> getUsersByStatus(Boolean enabled) {
        LoggingUtil.debug(logger, "根据用户状态查询用户，状态: {}", enabled);
        // 使用Repository以提升兼容性，避免不同数据库SQL差异导致异常
        boolean enabledFlag = enabled != null && enabled;
        return userRepository.findByEnabledAndDeletedFalse(enabledFlag)
                .map(this::convertToUserResponse)
                .onErrorResume(ex -> {
                    LoggingUtil.error(logger, "根据用户状态查询失败，返回空集合以保证路由稳定", ex);
                    return Flux.empty();
                })
                .doOnComplete(() -> LoggingUtil.debug(logger, "根据用户状态查询完成"));
    }

    @Override
    public Flux<UserResponse> searchUsers(String keyword) {
        LoggingUtil.debug(logger, "搜索用户，关键词: {}", keyword);

        if (!StringUtils.hasText(keyword)) {
            return Flux.empty();
        }

        return databaseClient.sql("""
                SELECT * FROM sys_users
                WHERE deleted = 0 AND (
                    username LIKE :keyword OR
                    email LIKE :keyword OR
                    phone LIKE :keyword OR
                    full_name LIKE :keyword
                )
                ORDER BY created_date DESC
                """)
                .bind("keyword", "%" + keyword + "%")
                .map((row, metadata) -> mapRowToUser(row))
                .all()
                .map(this::convertToUserResponse)
                .onErrorResume(ex -> {
                    LoggingUtil.error(logger, "用户搜索失败，返回空集合以保证路由稳定", ex);
                    return Flux.empty();
                })
                .doOnComplete(() -> LoggingUtil.debug(logger, "用户搜索完成"));
    }

    @Override
    public Flux<UserResponse> getUserList(int page, int size, String keyword) {
        LoggingUtil.debug(logger, "查询用户列表，页码: {}, 大小: {}, 关键词: {}", page, size, keyword);

        return validatePageParams(page, size)
                .thenMany(StringUtils.hasText(keyword)
                        ? customUserRepository.fullTextSearchUsers(keyword, null, size)
                                .skip((long) page * size)
                                .take(size)
                        : userRepository.findByDeletedFalse()
                                .skip((long) page * size)
                                .take(size))
                .map(this::convertToUserResponse)
                .doOnComplete(() -> LoggingUtil.debug(logger, "用户列表查询完成"))
                .doOnError(error -> LoggingUtil.error(logger, "用户列表查询失败", error));
    }

    @Override
    public Mono<Long> batchDeleteUsers(List<Long> userIds) {
        LoggingUtil.debug(logger, "批量删除用户，用户ID列表: {}", userIds);

        if (userIds == null || userIds.isEmpty()) {
            return Mono.just(0L);
        }

        return databaseClient.sql("""
                UPDATE sys_users SET deleted = 1, last_modified_date = CURRENT_TIMESTAMP
                WHERE id IN (:userIds) AND deleted = 0
                """)
                .bind("userIds", userIds)
                .fetch()
                .rowsUpdated()
                .map(Long::valueOf)
                .doOnSuccess(count -> LoggingUtil.info(logger, "批量删除用户完成，删除数量: {}", count))
                .doOnError(error -> LoggingUtil.error(logger, "批量删除用户失败", error));
    }

    @Override
    public Mono<Map<String, Object>> getUserRegistrationStats(LocalDateTime startDate, LocalDateTime endDate) {
        LoggingUtil.debug(logger, "查询用户注册统计，开始时间: {}, 结束时间: {}", startDate, endDate);

        return databaseClient.sql("""
                SELECT
                    DATE(created_at) as date,
                    COUNT(*) as count
                FROM sys_users
                WHERE deleted = 0 AND created_at BETWEEN :startDate AND :endDate
                GROUP BY DATE(created_at)
                ORDER BY date DESC
                """)
                .bind("startDate", startDate)
                .bind("endDate", endDate)
                .map(row -> new AbstractMap.SimpleEntry<>(
                        row.get("date", String.class),
                        row.get("count", Long.class)))
                .all()
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .map(stats -> Map.of(
                        "period", Map.of("start", startDate, "end", endDate),
                        "dailyStats", stats,
                        "totalRegistrations", stats.values().stream().mapToLong(Long::longValue).sum()))
                .onErrorResume(ex -> {
                    LoggingUtil.error(logger, "用户注册统计查询失败，返回空统计以保证路由稳定", ex);
                    return Mono.just(Map.of(
                            "period", Map.of("start", startDate, "end", endDate),
                            "dailyStats", java.util.Collections.emptyMap(),
                            "totalRegistrations", 0L));
                })
                .doOnSuccess(stats -> LoggingUtil.debug(logger, "用户注册统计查询完成"));
    }

    @Override
    public Mono<Long> getActiveUserCount(int days) {
        LoggingUtil.debug(logger, "查询活跃用户数量，天数: {}", days);

        LocalDateTime sinceDate = LocalDateTime.now().minusDays(days);
        return databaseClient.sql("""
                SELECT COUNT(DISTINCT u.id) as count
                FROM sys_users u
                INNER JOIN user_activity_log ual ON u.id = ual.user_id
                WHERE u.deleted = 0 AND u.status = 'ACTIVE'
                AND ual.activity_time >= :sinceDate
                """)
                .bind("sinceDate", sinceDate)
                .map(row -> row.get("count", Long.class))
                .one()
                .defaultIfEmpty(0L)
                .onErrorResume(ex -> {
                    LoggingUtil.error(logger, "活跃用户数量查询失败，返回0以保证路由稳定", ex);
                    return Mono.just(0L);
                })
                .doOnSuccess(count -> LoggingUtil.debug(logger, "活跃用户数量查询完成: {}", count));
    }

    private Throwable mapDataAccessException(Throwable ex) {
        if (ex instanceof BusinessException) {
            return ex;
        }
        if (ex instanceof DataIntegrityViolationException) {
            return BusinessException.of(ErrorCode.DATA_INTEGRITY_ERROR, ex);
        }
        if (ex instanceof BadSqlGrammarException) {
            return BusinessException.of(ErrorCode.SQL_EXECUTION_ERROR, ex);
        }
        if (ex instanceof DataAccessException) {
            return BusinessException.of(ErrorCode.DATABASE_ERROR, ex);
        }
        return ex;
    }

    // ==================== 私有辅助方法 ====================

    private Mono<Void> validateCreateRequest(UserCreateRequest request) {
        if (request == null) {
            return Mono.error(new ValidationException("创建请求不能为空"));
        }
        if (!StringUtils.hasText(request.getUsername())) {
            return Mono.error(new ValidationException("用户名不能为空"));
        }
        if (!StringUtils.hasText(request.getPassword())) {
            return Mono.error(new ValidationException("密码不能为空"));
        }
        // 邮箱改为可选字段；仅在提供时进行唯一性检查
        Mono<Void> emailCheck = StringUtils.hasText(request.getEmail())
                ? checkEmailExists(request.getEmail())
                : Mono.empty();

        return checkUsernameExists(request.getUsername())
                .then(emailCheck);
    }

    private Mono<Void> validateUpdateRequest(UserUpdateRequest request) {
        if (request == null) {
            return Mono.error(new ValidationException("更新请求不能为空"));
        }
        if (StringUtils.hasText(request.getEmail())) {
            return checkEmailExists(request.getEmail());
        }
        return Mono.empty();
    }

    private Mono<Void> validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return Mono.error(new ValidationException("用户ID无效"));
        }
        return Mono.empty();
    }

    private Mono<Void> validatePermissionCodes(List<String> permissionCodes) {
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return Mono.error(new ValidationException("权限代码列表不能为空"));
        }
        for (String code : permissionCodes) {
            if (!StringUtils.hasText(code)) {
                return Mono.error(new ValidationException("权限代码不能为空"));
            }
        }
        return Mono.empty();
    }

    private Mono<Void> validateUserStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return Mono.error(new ValidationException("用户状态不能为空"));
        }
        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
            return Mono.error(new ValidationException("用户状态值无效"));
        }
        return Mono.empty();
    }

    private Mono<Void> validatePageParams(int page, int size) {
        if (page < 0) {
            return Mono.error(new ValidationException("页码不能小于0"));
        }
        int maxPageSize = monitoringProperties.getConstants().getMaxPageSize();
        if (size <= 0 || size > maxPageSize) {
            return Mono.error(new ValidationException("页面大小必须在1-" + maxPageSize + "之间"));
        }
        return Mono.empty();
    }

    private Mono<Void> checkUsernameExists(String username) {
        return existsByUsername(username)
                .flatMap(exists -> exists ? Mono.error(new BusinessException(ErrorCode.USER_ALREADY_EXISTS))
                        : Mono.empty());
    }

    private Mono<Void> checkEmailExists(String email) {
        return existsByEmail(email)
                .flatMap(exists -> exists ? Mono.error(new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS))
                        : Mono.empty());
    }

    @SuppressWarnings("unused")
    private Mono<Void> checkPhoneExists(String phone) {
        if (!StringUtils.hasText(phone)) {
            return Mono.empty();
        }
        return userRepository.countByPhoneAndDeletedFalse(phone)
                .map(count -> count > 0)
                .flatMap(exists -> exists ? Mono.error(new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS))
                        : Mono.empty());
    }

    private Mono<User> buildUserFromRequest(UserCreateRequest request) {
        User user = new User();
        user.setId(snowflakeIdGenerator.generateUserId()); // 使用雪花ID生成器生成用户ID
        // 显式标记为新实体，确保R2DBC走插入逻辑而非更新
        user.markAsNew();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setUserType(request.getUserType() != null ? request.getUserType() : UserType.NORMAL_USER);
        user.setEnabled(true);
        user.setCreatedDate(LocalDateTime.now());
        user.setModifiedDate(LocalDateTime.now());
        return Mono.just(user);
    }

    private Mono<User> updateUserFromRequest(User user, UserUpdateRequest request) {
        // 确保实体被标记为已存在实体，避免R2DBC执行INSERT操作
        user.markAsExisting();

        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getRealName())) {
            user.setRealName(request.getRealName());
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (request.getUserType() != null) {
            user.setUserType(request.getUserType());
        }
        user.setModifiedDate(LocalDateTime.now());
        return Mono.just(user);
    }

    private Mono<Long> deleteUserPermissions(Long userId) {
        return databaseClient.sql("""
                UPDATE user_permissions
                SET is_active = 0, deleted = 1, last_modified_date = :modifiedDate
                WHERE user_id = :userId AND is_active = 1 AND deleted = 0
                """)
                .bind("userId", userId)
                .bind("modifiedDate", LocalDateTime.now())
                .fetch()
                .rowsUpdated()
                .map(Long::valueOf);
    }

    private Mono<Long> insertUserPermissions(Long userId, List<String> permissionCodes) {
        if (permissionCodes.isEmpty()) {
            return Mono.just(0L);
        }

        // 批量插入用户权限记录
        return Flux.fromIterable(permissionCodes)
                .flatMap(permission -> databaseClient.sql("""
                        INSERT INTO user_permissions (
                            user_id, permission, is_active, granted_by, granted_at,
                            created_at, updated_at
                        )
                        VALUES (:userId, :permission, 1, :grantedBy, :createdDate,
                               :createdDate, :createdDate)
                        """)
                        .bind("userId", userId)
                        .bind("permission", permission)
                        .bind("grantedBy", userId)
                        .bind("createdDate", LocalDateTime.now())
                        .fetch()
                        .rowsUpdated())
                .reduce(0L, Long::sum);
    }

    // ==================== 用户认证操作 ====================

    @Override
    public Mono<User> authenticateUser(String username, String password) {
        LoggingUtil.info(logger, "用户认证，用户名: {}", username);

        return validateUsernameAndPassword(username, password)
                .then(userRepository.findByUsername(username))
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.USER_NOT_FOUND)))
                .flatMap(user -> {
                    // 检查用户状态
                    if (!"ACTIVE".equals(user.getStatus())) {
                        return Mono.error(new BusinessException(ErrorCode.ACCOUNT_DISABLED));
                    }

                    // 验证密码
                    if (!passwordEncoder.matches(password, user.getPassword())) {
                        return Mono.error(new BusinessException(ErrorCode.INVALID_PASSWORD));
                    }

                    // 更新最后登录时间 - lastLoginTime字段已从数据库中移除
                    // user.setLastLoginTime(LocalDateTime.now()); // lastLoginTime字段已移除
                    return userRepository.save(user);
                })
                .doOnSuccess(user -> LoggingUtil.info(logger, "用户认证成功，用户ID: {}", user.getId()))
                .doOnError(error -> LoggingUtil.error(logger, "用户认证失败，用户名: " + username, error));
    }

    /**
     * 验证用户名和密码格式
     *
     * @param username 用户名
     * @param password 密码
     * @return 验证结果
     */
    private Mono<Void> validateUsernameAndPassword(String username, String password) {
        return Mono.fromRunnable(() -> {
            if (username == null || username.trim().isEmpty()) {
                throw new ValidationException("用户名不能为空");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new ValidationException("密码不能为空");
            }
        });
    }

    // ==================== 新增验证方法实现 ====================

    @Override
    public Mono<Boolean> existsByPhone(String phone) {
        LoggingUtil.info(logger, "检查手机号是否存在: {}", phone);

        if (!StringUtils.hasText(phone)) {
            return Mono.just(false);
        }

        return userRepository.countByPhone(phone)
                .map(count -> count > 0)
                .doOnNext(exists -> LoggingUtil.debug(logger, "手机号 {} 存在性检查结果: {}", phone, exists))
                .doOnError(error -> LoggingUtil.error(logger, "检查手机号存在性失败: " + phone, error));
    }

    @Override
    public Mono<Boolean> validatePassword(Long userId, String password) {
        LoggingUtil.info(logger, "验证用户密码，用户ID: {}", userId);

        return validateUserId(userId)
                .then(userRepository.findByIdAndDeletedFalse(userId))
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.USER_NOT_FOUND)))
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .doOnNext(isValid -> LoggingUtil.debug(logger, "用户 {} 密码验证结果: {}", userId, isValid))
                .doOnError(error -> LoggingUtil.error(logger, "验证用户密码失败，用户ID: " + userId, error));
    }

    @Override
    public Mono<Boolean> batchEnableUsers(List<Long> userIds) {
        LoggingUtil.info(logger, "批量启用用户，用户数量: {}", userIds.size());

        if (userIds.isEmpty()) {
            return Mono.just(true);
        }

        return customUserRepository.batchToggleUserStatus(userIds, true)
                .map(updatedCount -> updatedCount > 0)
                .doOnNext(success -> LoggingUtil.info(logger, "批量启用用户完成，成功: {}", success))
                .doOnError(error -> LoggingUtil.error(logger, "批量启用用户失败", error));
    }

    @Override
    public Mono<Boolean> batchDisableUsers(List<Long> userIds) {
        LoggingUtil.info(logger, "批量禁用用户，用户数量: {}", userIds.size());

        if (userIds.isEmpty()) {
            return Mono.just(true);
        }

        return customUserRepository.batchToggleUserStatus(userIds, false)
                .map(updatedCount -> updatedCount > 0)
                .doOnNext(success -> LoggingUtil.info(logger, "批量禁用用户完成，成功: {}", success))
                .doOnError(error -> LoggingUtil.error(logger, "批量禁用用户失败", error));
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRealName(user.getRealName());
        response.setPhone(user.getPhone());
        response.setUserType(user.getUserType());
        response.setCreatedDate(user.getCreatedDate());
        response.setLastModifiedDate(user.getModifiedDate());
        // 契约要求：permissions 字段必须存在；新用户默认空列表
        response.setPermissions(Collections.emptyList());
        // 可选：同步当前账户状态字符串
        response.setStatus(user.getStatus());
        return response;
    }

    private User mapRowToUser(io.r2dbc.spi.Row row, io.r2dbc.spi.RowMetadata metadata) {
        User user = new User();
        user.setId(row.get("id", Long.class));
        user.setUsername(row.get("username", String.class));
        user.setEmail(row.get("email", String.class));
        user.setPassword(row.get("password", String.class));
        user.setRealName(row.get("full_name", String.class));
        user.setPhone(row.get("phone", String.class));
        user.setUserType(UserType.fromCode(row.get("user_type", String.class)));
        String status = row.get("status", String.class);
        user.setEnabled(status != null && "ACTIVE".equalsIgnoreCase(status));
        user.setDeleted(row.get("deleted", Integer.class));
        user.setCreatedDate(row.get("created_date", LocalDateTime.class));
        user.setModifiedDate(row.get("last_modified_date", LocalDateTime.class));
        return user;
    }

    private User mapRowToUser(io.r2dbc.spi.Row row) {
        return mapRowToUser(row, null);
    }

    // ==================== 错误映射辅助方法 ====================

    /**
     * 将数据唯一约束冲突映射为明确的业务异常，返回409状态。
     * 优先识别 username/email 字段的重复冲突，分别映射到 USER_ALREADY_EXISTS / EMAIL_ALREADY_EXISTS。
     * 其余数据完整性问题统一映射为 DATA_INTEGRITY_ERROR。
     */
    private Throwable mapUniqueConstraintViolation(Throwable ex) {
        if (ex instanceof BusinessException) {
            return ex;
        }

        if (ex instanceof DataIntegrityViolationException) {
            String message = getRootMessage(ex).toLowerCase();

            // 常见冲突关键字与约束命名判断
            boolean isUniqueConflict = message.contains("unique")
                    || message.contains("duplicate")
                    || message.contains("constraint")
                    || message.contains("uk_")
                    || message.contains("users_email")
                    || message.contains("users_username");

            if (isUniqueConflict) {
                if (message.contains("email")) {
                    return new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
                }
                if (message.contains("username") || message.contains("user_name")) {
                    return new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
                }
                return BusinessException.resourceConflict("唯一约束冲突");
            }

            return BusinessException.of(ErrorCode.DATA_INTEGRITY_ERROR, ex);
        }

        if (ex instanceof org.springframework.dao.DataAccessException) {
            return BusinessException.of(ErrorCode.DATABASE_ERROR, ex);
        }

        return ex;
    }

    /**
     * 获取异常根因的消息文本
     */
    private String getRootMessage(Throwable ex) {
        Throwable t = ex;
        String msg = t.getMessage();
        while (t.getCause() != null) {
            t = t.getCause();
            if (t.getMessage() != null) {
                msg = t.getMessage();
            }
        }
        return msg != null ? msg : "";
    }
}
