package com.honyrun.repository.custom;

import com.honyrun.model.entity.business.User;
import com.honyrun.model.enums.UserType;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 自定义用户仓库实现类
 *
 * 实现CustomUserRepository接口，使用DatabaseClient进行响应式数据库操作
 * 提供复杂的用户查询功能，支持动态条件查询、统计分析、批量操作等
 * 所有方法返回Mono或Flux类型，支持响应式数据流处理
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  19:15:00
 * @modified 2025-07-01 19:15:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Repository
public class CustomUserRepositoryImpl implements CustomUserRepository {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserRepositoryImpl.class);
    private final DatabaseClient databaseClient;

    public CustomUserRepositoryImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    // ==================== 复杂条件查询方法 ====================

    @Override
    public Flux<User> findByDynamicConditions(Map<String, Object> conditions,
                                             Long offset,
                                             Integer limit,
                                             String orderBy,
                                             String orderDirection) {
        LoggingUtil.info(logger, "执行动态条件查询用户，条件: {}", conditions);

        StringBuilder sql = new StringBuilder("SELECT * FROM sys_users WHERE 1=1");
        Map<String, Object> parameters = new HashMap<>();

        // 构建动态WHERE条件
        buildDynamicWhereClause(sql, conditions, parameters);

        // 添加排序
        if (orderBy != null && !orderBy.trim().isEmpty()) {
            sql.append(" ORDER BY ").append(orderBy);
            if ("DESC".equalsIgnoreCase(orderDirection)) {
                sql.append(" DESC");
            } else {
                sql.append(" ASC");
            }
        } else {
            sql.append(" ORDER BY created_at DESC");
        }

        // 添加分页
        if (offset != null && limit != null) {
            sql.append(" LIMIT ").append(limit).append(" OFFSET ").append(offset);
        }

        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(sql.toString());

        // 绑定参数
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            executeSpec = executeSpec.bind(entry.getKey(), entry.getValue());
        }

        return executeSpec
                .map((row, metadata) -> mapRowToUser(row))
                .all()
                .doOnError(error -> LoggingUtil.error(logger, "动态条件查询用户失败", error))
                .doOnComplete(() -> LoggingUtil.debug(logger, "动态条件查询用户完成"));
    }

    @Override
    public Mono<Long> countByDynamicConditions(Map<String, Object> conditions) {
        LoggingUtil.info(logger, "统计动态条件查询用户数量，条件: {}", conditions);

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM sys_users WHERE 1=1");
        Map<String, Object> parameters = new HashMap<>();

        // 构建动态WHERE条件
        buildDynamicWhereClause(sql, conditions, parameters);

        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(sql.toString());

        // 绑定参数
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            executeSpec = executeSpec.bind(entry.getKey(), entry.getValue());
        }

        return executeSpec
                .map((row, metadata) -> row.get(0, Long.class))
                .one()
                .doOnError(error -> LoggingUtil.error(logger, "统计动态条件查询用户数量失败", error));
    }

    // ==================== 统计分析方法 ====================

    @Override
    public Mono<Map<UserType, Long>> getUserTypeStatistics() {
        LoggingUtil.info(logger, "获取用户类型统计信息");

        String sql = "SELECT user_type, COUNT(*) as count FROM sys_users GROUP BY user_type";

        return databaseClient.sql(sql)
                .fetch()
                .all()
                .collectMap(
                    row -> UserType.valueOf((String) row.get("user_type")),
                    row -> ((Number) row.get("count")).longValue()
                )
                .doOnSuccess(result -> LoggingUtil.info(logger, "查询用户类型统计完成，结果数量: {}", result.size()))
                .doOnError(error -> LoggingUtil.error(logger, "获取用户类型统计信息失败", error));
    }

    @Override
    public Mono<Map<String, Long>> getUserStatusStatistics() {
        LoggingUtil.info(logger, "获取用户状态统计信息");

        String sql = """
            SELECT
                CASE
                    WHEN status = 1 AND locked_time IS NULL AND (valid_to IS NULL OR valid_to > CURRENT_TIMESTAMP) THEN 'ACTIVE'
                    WHEN status = 0 THEN 'DISABLED'
                    WHEN locked_time IS NOT NULL THEN 'LOCKED'
                    WHEN valid_to IS NOT NULL AND valid_to <= CURRENT_TIMESTAMP THEN 'EXPIRED'
                    ELSE 'UNKNOWN'
                END as status_desc,
                COUNT(*) as count
            FROM sys_users
            GROUP BY status_desc
            """;

        return databaseClient.sql(sql)
                .map((row, metadata) -> {
                    String statusDesc = row.get("status_desc", String.class);
                    Long count = row.get("count", Long.class);
                    return Map.entry(statusDesc, count);
                })
                .all()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue
                ))
                .doOnError(error -> LoggingUtil.error(logger, "获取用户状态统计信息失败", error));
    }

    @Override
    public Flux<Map<String, Object>> getUserRegistrationTrend(LocalDateTime startDate,
                                                             LocalDateTime endDate,
                                                             String groupBy) {
        LoggingUtil.info(logger, "获取用户注册趋势统计，开始时间: {}, 结束时间: {}, 分组: {}", startDate, endDate, groupBy);

        String dateFormat = switch (groupBy.toUpperCase()) {
            case "DAY" -> "CAST(created_at AS DATE)";
            case "WEEK" -> "FORMATDATETIME(created_at, 'yyyy-w')";
            case "MONTH" -> "FORMATDATETIME(created_at, 'yyyy-MM')";
            default -> "CAST(created_at AS DATE)";
        };

        String sql = String.format("""
            SELECT
                %s as date_group,
                COUNT(*) as count,
                COUNT(CASE WHEN user_type = 'SYSTEM_USER' THEN 1 END) as system_users,
                COUNT(CASE WHEN user_type = 'NORMAL_USER' THEN 1 END) as normal_users,
                COUNT(CASE WHEN user_type = 'GUEST' THEN 1 END) as guest_users
            FROM sys_users
            WHERE created_at >= :startDate
                AND created_at <= :endDate
            GROUP BY date_group
            ORDER BY date_group
            """, dateFormat);

        return databaseClient.sql(sql)
                .bind("startDate", startDate)
                .bind("endDate", endDate)
                .map((row, metadata) -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("date", row.get("date_group", String.class));
                    result.put("total", row.get("count", Long.class));
                    result.put("systemUsers", row.get("system_users", Long.class));
                    result.put("normalUsers", row.get("normal_users", Long.class));
                    result.put("guestUsers", row.get("guest_users", Long.class));
                    return result;
                })
                .all()
                .doOnError(error -> LoggingUtil.error(logger, "获取用户注册趋势统计失败", error));
    }

    @Override
    public Mono<Map<String, Object>> getUserActivityStatistics(Integer days) {
        LoggingUtil.info(logger, "获取用户活跃度统计，统计天数: {}", days);

        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(days);

        String sql = """
            SELECT
                COUNT(*) as total_users,
                COUNT(CASE WHEN last_login_time >= :cutoffTime THEN 1 END) as active_users,
                COUNT(CASE WHEN last_login_time IS NULL THEN 1 END) as never_logged_in,
                COUNT(CASE WHEN status = 1 THEN 1 END) as enabled_users,
                COUNT(CASE WHEN locked_time IS NOT NULL THEN 1 END) as locked_users,
                AVG(login_failure_count) as avg_failure_count
            FROM sys_users
            """;

        return databaseClient.sql(sql)
                .bind("cutoffTime", cutoffTime)
                .map((row, metadata) -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("totalUsers", row.get("total_users", Long.class));
                    result.put("activeUsers", row.get("active_users", Long.class));
                    result.put("neverLoggedIn", row.get("never_logged_in", Long.class));
                    result.put("enabledUsers", row.get("enabled_users", Long.class));
                    result.put("lockedUsers", row.get("locked_users", Long.class));
                    result.put("avgFailureCount", row.get("avg_failure_count", Double.class));
                    result.put("statisticsDays", days);
                    result.put("cutoffTime", cutoffTime);
                    return result;
                })
                .one()
                .doOnError(error -> LoggingUtil.error(logger, "获取用户活跃度统计失败", error));
    }

    // ==================== 安全相关查询方法 ====================

    @Override
    public Flux<User> findSuspiciousUsers(Integer failureThreshold, Integer timeWindow) {
        LoggingUtil.info(logger, "查找可疑登录用户，失败阈值: {}, 时间窗口: {}小时", failureThreshold, timeWindow);

        LocalDateTime timeThreshold = LocalDateTime.now().minusHours(timeWindow);

        String sql = """
            SELECT * FROM sys_users
            WHERE (
                login_failure_count >= :failureThreshold
                OR (locked_time IS NOT NULL AND locked_time >= :timeThreshold)
            )
            ORDER BY login_failure_count DESC, locked_time DESC
            """;

        return databaseClient.sql(sql)
                .bind("failureThreshold", failureThreshold)
                .bind("timeThreshold", timeThreshold)
                .map((row, metadata) -> mapRowToUser(row))
                .all()
                .doOnError(error -> LoggingUtil.error(logger, "查找可疑登录用户失败", error));
    }

    @Override
    public Flux<User> findUsersNeedingPasswordReset(Integer passwordMaxAge, Integer forceResetFailureCount) {
        LoggingUtil.info(logger, "查找需要密码重置的用户，密码最大使用天数: {}, 强制重置失败次数: {}", passwordMaxAge, forceResetFailureCount);

        LocalDateTime passwordAgeThreshold = LocalDateTime.now().minusDays(passwordMaxAge);

        String sql = """
            SELECT * FROM sys_users
            WHERE status = 1
                AND (
                    last_modified_date < :passwordAgeThreshold
                    OR login_failure_count >= :forceResetFailureCount
                )
            ORDER BY last_modified_date ASC, login_failure_count DESC
            """;

        return databaseClient.sql(sql)
                .bind("passwordAgeThreshold", passwordAgeThreshold)
                .bind("forceResetFailureCount", forceResetFailureCount)
                .map((row, metadata) -> mapRowToUser(row))
                .all()
                .doOnError(error -> LoggingUtil.error(logger, "查找需要密码重置的用户失败", error));
    }

    @Override
    public Flux<User> findInactiveAccounts(Integer inactiveDays) {
        LoggingUtil.info(logger, "查找长期未使用的账户，未活跃天数: {}", inactiveDays);

        LocalDateTime inactiveThreshold = LocalDateTime.now().minusDays(inactiveDays);

        String sql = """
            SELECT * FROM sys_users
            WHERE status = 1
                AND (
                    last_login_time IS NULL
                    OR last_login_time < :inactiveThreshold
                )
            ORDER BY last_login_time ASC NULLS FIRST
            """;

        return databaseClient.sql(sql)
                .bind("inactiveThreshold", inactiveThreshold)
                .map((row, metadata) -> mapRowToUser(row))
                .all()
                .doOnError(error -> LoggingUtil.error(logger, "查找长期未使用的账户失败", error));
    }

    // ==================== 权限相关查询方法 ====================

    @Override
    public Flux<User> findUsersByPermission(String permissionCode) {
        LoggingUtil.info(logger, "查找具有特定权限的用户，权限代码: {}", permissionCode);

        String sql = """
            SELECT DISTINCT u.* FROM sys_users u
            INNER JOIN user_permissions up ON u.id = up.user_id
            WHERE up.permission = :permissionCode
              AND up.is_active = 1
            ORDER BY u.username
            """;

        return databaseClient.sql(sql)
                .bind("permissionCode", permissionCode)
                .map((row, metadata) -> mapRowToUser(row))
                .all()
                .doOnError(error -> LoggingUtil.error(logger, "查找具有特定权限的用户失败", error));
    }

    @Override
    public Flux<String> findUserPermissions(Long userId) {
        LoggingUtil.info(logger, "查找用户的所有权限，用户ID: {}", userId);

        String sql = """
            SELECT up.permission AS permission_code
            FROM user_permissions up
            WHERE up.user_id = :userId
              AND up.is_active = 1
            ORDER BY permission_code
            """;

        return databaseClient.sql(sql)
                .bind("userId", userId)
                .map((row, metadata) -> row.get("permission_code", String.class))
                .all()
                .doOnError(error -> LoggingUtil.error(logger, "查找用户权限失败", error));
    }

    @Override
    public Mono<Boolean> hasPermission(Long userId, String permissionCode) {
        LoggingUtil.debug(logger, "检查用户是否具有特定权限，用户ID: {}, 权限代码: {}", userId, permissionCode);

        String sql = """
            SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM user_permissions up
            WHERE up.user_id = :userId
                AND up.permission = :permissionCode
                AND up.is_active = 1
            """;

        return databaseClient.sql(sql)
                .bind("userId", userId)
                .bind("permissionCode", permissionCode)
                .map((row, metadata) -> row.get(0, Boolean.class))
                .one()
                .doOnError(error -> LoggingUtil.error(logger, "检查用户权限失败", error));
    }

    // ==================== 批量操作方法 ====================

    @Override
    public Mono<Integer> batchUpdateUsers(Iterable<Long> userIds, Map<String, Object> updateFields) {
        LoggingUtil.info(logger, "批量更新用户信息，用户ID数量: {}, 更新字段: {}",
                        userIds instanceof Collection ? ((Collection<?>) userIds).size() : "未知", updateFields.keySet());

        if (updateFields.isEmpty()) {
            return Mono.just(0);
        }

        StringBuilder sql = new StringBuilder("UPDATE sys_users SET ");
        Map<String, Object> parameters = new HashMap<>();

        // 构建SET子句
        List<String> setClauses = new ArrayList<>();
        for (Map.Entry<String, Object> entry : updateFields.entrySet()) {
            String field = entry.getKey();
            String paramName = "field_" + field;
            setClauses.add(field + " = :" + paramName);
            parameters.put(paramName, entry.getValue());
        }

        sql.append(String.join(", ", setClauses));
        sql.append(", last_modified_date = CURRENT_TIMESTAMP");
        sql.append(" WHERE id IN (:userIds)");

        parameters.put("userIds", userIds);

        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(sql.toString());

        // 绑定参数
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            executeSpec = executeSpec.bind(entry.getKey(), entry.getValue());
        }

        return executeSpec
                .fetch()
                .rowsUpdated()
                .map(Long::intValue)
                .doOnError(error -> LoggingUtil.error(logger, "批量更新用户信息失败", error));
    }

    @Override
    public Mono<Integer> batchToggleUserStatus(Iterable<Long> userIds, Boolean enabled) {
        LoggingUtil.info(logger, "批量切换用户状态，用户ID数量: {}, 启用状态: {}",
                        userIds instanceof Collection ? ((Collection<?>) userIds).size() : "未知", enabled);

        String sql = """
            UPDATE sys_users
            SET status = :status, last_modified_date = CURRENT_TIMESTAMP
            WHERE id IN (:userIds)
            """;

        return databaseClient.sql(sql)
                .bind("status", enabled ? "ACTIVE" : "DISABLED")
                .bind("userIds", userIds)
                .fetch()
                .rowsUpdated()
                .map(Long::intValue)
                .doOnError(error -> LoggingUtil.error(logger, "批量启用/禁用用户失败", error));
    }

    @Override
    public Mono<Integer> batchResetPasswords(Iterable<Long> userIds, String newPassword) {
        LoggingUtil.info(logger, "批量重置用户密码，用户ID数量: {}",
                        userIds instanceof Collection ? ((Collection<?>) userIds).size() : "未知");

        String sql = """
            UPDATE sys_users
            SET password = :newPassword,
                login_failure_count = 0,
                locked_time = NULL,
                last_modified_date = CURRENT_TIMESTAMP
            WHERE id IN (:userIds)
            """;

        return databaseClient.sql(sql)
                .bind("newPassword", newPassword)
                .bind("userIds", userIds)
                .fetch()
                .rowsUpdated()
                .map(Long::intValue)
                .doOnError(error -> LoggingUtil.error(logger, "批量重置用户密码失败", error));
    }

    // ==================== 高级查询方法 ====================

    @Override
    public Flux<User> fullTextSearchUsers(String keyword, Iterable<UserType> userTypes, Integer limit) {
        LoggingUtil.info(logger, "全文搜索用户，关键词: {}, 用户类型: {}, 限制数量: {}", keyword, userTypes, limit);

        StringBuilder sql = new StringBuilder("""
            SELECT * FROM sys_users
            WHERE (
                username LIKE CONCAT('%', :keyword, '%')
                OR full_name LIKE CONCAT('%', :keyword, '%')
                OR email LIKE CONCAT('%', :keyword, '%')
            )
            """);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("keyword", keyword);

        // 添加用户类型过滤
        if (userTypes != null) {
            List<String> typeList = new ArrayList<>();
            for (UserType userType : userTypes) {
                typeList.add(userType.getCode());
            }
            if (!typeList.isEmpty()) {
                sql.append(" AND user_type IN (:userTypes)");
                parameters.put("userTypes", typeList);
            }
        }

        sql.append(" ORDER BY username");

        if (limit != null && limit > 0) {
            sql.append(" LIMIT ").append(limit);
        }

        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(sql.toString());

        // 绑定参数
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            executeSpec = executeSpec.bind(entry.getKey(), entry.getValue());
        }

        return executeSpec
                .map((row, metadata) -> mapRowToUser(row))
                .all()
                .doOnError(error -> LoggingUtil.error(logger, "全文搜索用户失败", error));
    }

    @Override
    public Flux<User> findSimilarUsers(Long userId, Double similarityThreshold, Integer limit) {
        LoggingUtil.info(logger, "查找相似用户，参考用户ID: {}, 相似度阈值: {}, 限制数量: {}", userId, similarityThreshold, limit);

        // 这里实现一个简化的相似度算法，实际项目中可能需要更复杂的算法
        String sql = """
            SELECT u2.*,
                (
                    CASE WHEN u1.user_type = u2.user_type THEN 0.3 ELSE 0 END +
                    CASE WHEN SUBSTRING(u1.email, LOCATE('@', u1.email)) = SUBSTRING(u2.email, LOCATE('@', u2.email)) THEN 0.2 ELSE 0 END +
                    CASE WHEN ABS(DATEDIFF(u1.created_at, u2.created_at)) <= 7 THEN 0.2 ELSE 0 END +
                    CASE WHEN u1.full_name IS NOT NULL AND u2.full_name IS NOT NULL AND
                     SOUNDEX(u1.full_name) = SOUNDEX(u2.full_name) THEN 0.3 ELSE 0 END
                ) as similarity_score
            FROM sys_users u1
            CROSS JOIN sys_users u2
            WHERE u1.id = :userId
                AND u2.id != :userId
            HAVING similarity_score >= :similarityThreshold
            ORDER BY similarity_score DESC
            """;

        if (limit != null && limit > 0) {
            sql += " LIMIT " + limit;
        }

        return databaseClient.sql(sql)
                .bind("userId", userId)
                .bind("similarityThreshold", similarityThreshold)
                .map((row, metadata) -> mapRowToUser(row))
                .all()
                .doOnError(error -> LoggingUtil.error(logger, "查找相似用户失败", error));
    }

    @Override
    public Mono<Map<String, Object>> getUserDetailedInfo(Long userId) {
        LoggingUtil.info(logger, "获取用户详细信息，用户ID: {}", userId);

        String sql = """
            SELECT
                u.*,
                COUNT(up.id) as permission_count,
                GROUP_CONCAT(up.permission) as permissions
            FROM sys_users u
            LEFT JOIN user_permissions up ON u.id = up.user_id
            WHERE u.id = :userId
            GROUP BY u.id
            """;

        return databaseClient.sql(sql)
                .bind("userId", userId)
                .map((row, metadata) -> {
                    Map<String, Object> result = new HashMap<>();

                    // 基本用户信息
                    result.put("user", mapRowToUser(row));

                    // 权限信息
                    result.put("permissionCount", row.get("permission_count", Long.class));
                    String permissionsStr = row.get("permissions", String.class);
                    if (permissionsStr != null) {
                        result.put("permissions", Arrays.asList(permissionsStr.split(",")));
                    } else {
                        result.put("permissions", Collections.emptyList());
                    }

                    return result;
                })
                .one()
                .doOnError(error -> LoggingUtil.error(logger, "获取用户详细信息失败", error));
    }

    // ==================== 数据导出方法 ====================

    @Override
    public Flux<Map<String, Object>> exportUserData(Map<String, Object> conditions, Iterable<String> fields) {
        LoggingUtil.info(logger, "导出用户数据，条件: {}, 字段: {}", conditions, fields);

        List<String> fieldList = new ArrayList<>();
        for (String field : fields) {
            fieldList.add(field);
        }

        String selectFields = fieldList.isEmpty() ? "*" : String.join(", ", fieldList);

        StringBuilder sql = new StringBuilder("SELECT " + selectFields + " FROM sys_users WHERE 1=1");
        Map<String, Object> parameters = new HashMap<>();

        // 构建动态WHERE条件
        buildDynamicWhereClause(sql, conditions, parameters);

        sql.append(" ORDER BY created_at DESC");

        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(sql.toString());

        // 绑定参数
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            executeSpec = executeSpec.bind(entry.getKey(), entry.getValue());
        }

        return executeSpec
                .map((row, metadata) -> {
                    Map<String, Object> result = new HashMap<>();
                    for (String field : fieldList.isEmpty() ?
                            Arrays.asList("id", "username", "full_name", "email", "phone", "user_type", "status") :
                            fieldList) {
                        result.put(field, row.get(field));
                    }
                    return result;
                })
                .all()
                .doOnError(error -> LoggingUtil.error(logger, "导出用户数据失败", error));
    }

    @Override
    public Mono<Map<String, Object>> getUserDataSummary(Long userId) {
        LoggingUtil.info(logger, "获取用户数据摘要，用户ID: {}", userId);

        return getUserDetailedInfo(userId)
                .map(detailedInfo -> {
                    Map<String, Object> summary = new HashMap<>();
                    User user = (User) detailedInfo.get("user");

                    summary.put("userId", user.getId());
                    summary.put("username", user.getUsername());
                    summary.put("userType", user.getUserType());
                    summary.put("status", user.getStatus());
                    summary.put("isActive", user.isAccountNonExpired());
                    summary.put("permissionCount", detailedInfo.get("permissionCount"));
                    summary.put("createdDate", user.getCreatedDate());
                    // 数据库表中没有最后登录时间字段，设置为null
                    summary.put("lastLoginTime", null);
                    // 数据库表中没有登录失败次数字段，设置为null
                    summary.put("loginFailureCount", null);

                    return summary;
                })
                .doOnError(error -> LoggingUtil.error(logger, "获取用户数据摘要失败", error));
    }

    // ==================== 缓存相关方法 ====================

    @Override
    public Mono<Boolean> refreshUserCache(Long userId) {
        LoggingUtil.info(logger, "刷新用户缓存，用户ID: {}", userId);
        // 这里应该实现具体的缓存刷新逻辑
        // 由于当前阶段还没有实现缓存，这里返回成功
        return Mono.just(true);
    }

    @Override
    public Mono<Boolean> clearUserCache(Long userId) {
        LoggingUtil.info(logger, "清除用户相关缓存，用户ID: {}", userId);
        // 这里应该实现具体的缓存清除逻辑
        // 由于当前阶段还没有实现缓存，这里返回成功
        return Mono.just(true);
    }

    // ==================== 审计相关方法 ====================

    @Override
    public Mono<Boolean> auditUserOperation(Long userId, String operation, String details, String ipAddress) {
        LoggingUtil.info(logger, "记录用户操作审计日志，用户ID: {}, 操作: {}, IP: {}", userId, operation, ipAddress);

        String sql = """
            INSERT INTO sys_system_logs (log_type, log_level, operation_type, module, user_id,
                                       description, request_uri, request_method, user_agent,
                                       status, created_at)
            VALUES ('OPERATION', 'INFO', :operation, 'USER_MANAGEMENT', :userId,
                    :details, '', '', :ipAddress, 1, CURRENT_TIMESTAMP)
            """;

        return databaseClient.sql(sql)
                .bind("operation", operation)
                .bind("userId", userId)
                .bind("details", details)
                .bind("ipAddress", ipAddress)
                .fetch()
                .rowsUpdated()
                .map(rows -> rows > 0)
                .doOnError(error -> LoggingUtil.error(logger, "记录用户操作审计日志失败", error));
    }

    @Override
    public Flux<Map<String, Object>> getUserOperationHistory(Long userId,
                                                            LocalDateTime startTime,
                                                            LocalDateTime endTime,
                                                            Integer limit) {
        LoggingUtil.info(logger, "查询用户操作历史，用户ID: {}, 开始时间: {}, 结束时间: {}, 限制数量: {}",
                        userId, startTime, endTime, limit);

        StringBuilder sql = new StringBuilder("""
            SELECT log_type, operation_type, description, created_at, user_agent
            FROM sys_system_logs
            WHERE user_id = :userId
                AND created_at >= :startTime
                AND created_at <= :endTime
            ORDER BY created_at DESC
            """);

        if (limit != null && limit > 0) {
            sql.append(" LIMIT ").append(limit);
        }

        return databaseClient.sql(sql.toString())
                .bind("userId", userId)
                .bind("startTime", startTime)
                .bind("endTime", endTime)
                .map((row, metadata) -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("logType", row.get("log_type", String.class));
                    result.put("operationType", row.get("operation_type", String.class));
                    result.put("description", row.get("description", String.class));
                    result.put("createdDate", row.get("created_at", LocalDateTime.class));
                    result.put("userAgent", row.get("user_agent", String.class));
                    return result;
                })
                .all()
                .doOnError(error -> LoggingUtil.error(logger, "查询用户操作历史失败", error));
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建动态WHERE条件子句
     *
     * @param sql SQL构建器
     * @param conditions 查询条件
     * @param parameters 参数Map
     */
    private void buildDynamicWhereClause(StringBuilder sql, Map<String, Object> conditions, Map<String, Object> parameters) {
        if (conditions == null || conditions.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                continue;
            }

            switch (key) {
                case "username":
                    sql.append(" AND username LIKE CONCAT('%', :username, '%')");
                    parameters.put("username", value);
                    break;
                case "realName":
                    sql.append(" AND full_name LIKE CONCAT('%', :realName, '%')");
                    parameters.put("realName", value);
                    break;
                case "email":
                    sql.append(" AND email = :email");
                    parameters.put("email", value);
                    break;
                case "phone":
                    sql.append(" AND phone = :phone");
                    parameters.put("phone", value);
                    break;
                case "userType":
                    sql.append(" AND user_type = :userType");
                    parameters.put("userType", value);
                    break;
                case "status":
                    sql.append(" AND status = :status");
                    parameters.put("status", value);
                    break;
                case "enabled":
                    sql.append(" AND status = :enabled");
                    parameters.put("enabled", (Boolean) value ? "ACTIVE" : "DISABLED");
                    break;
                case "locked":
                    if ((Boolean) value) {
                        sql.append(" AND locked_time IS NOT NULL");
                    } else {
                        sql.append(" AND locked_time IS NULL");
                    }
                    break;
                case "validFrom":
                    sql.append(" AND valid_from >= :validFrom");
                    parameters.put("validFrom", value);
                    break;
                case "validTo":
                    sql.append(" AND valid_to <= :validTo");
                    parameters.put("validTo", value);
                    break;
                case "createdAfter":
                    sql.append(" AND created_at >= :createdAfter");
                    parameters.put("createdAfter", value);
                    break;
                case "createdBefore":
                    sql.append(" AND created_at <= :createdBefore");
                    parameters.put("createdBefore", value);
                    break;
                case "lastLoginAfter":
                    sql.append(" AND last_login_time >= :lastLoginAfter");
                    parameters.put("lastLoginAfter", value);
                    break;
                case "lastLoginBefore":
                    sql.append(" AND last_login_time <= :lastLoginBefore");
                    parameters.put("lastLoginBefore", value);
                    break;
            }
        }
    }

    /**
     * 将数据库行映射为User对象
     *
     * @param row 数据库行
     * @return User对象
     */
    private User mapRowToUser(io.r2dbc.spi.Row row) {
        User user = new User();

        user.setId(row.get("id", Long.class));
        user.setUsername(row.get("username", String.class));
        user.setPassword(row.get("password", String.class));

        String userTypeStr = row.get("user_type", String.class);
        if (userTypeStr != null) {
            user.setUserType(UserType.fromCode(userTypeStr));
        }

        user.setRealName(row.get("full_name", String.class));
        user.setEmail(row.get("email", String.class));
        user.setPhone(row.get("phone", String.class));
        user.setStatus(row.get("status", String.class));

        // 修复enabled字段映射 - 对应数据库BOOLEAN字段
        Boolean enabled = row.get("enabled", Boolean.class);
        if (enabled != null) {
            user.setEnabled(enabled);
        }

        // 修复字段映射 - 这些字段已从数据库中移除，不再设置
        // user.setValidTo(row.get("expiry_date", java.time.LocalDate.class)); // validTo字段已移除
        // user.setLastLoginTime(row.get("last_login_time", LocalDateTime.class)); // lastLoginTime字段已移除
        // user.setLastLoginIp(row.get("last_login_ip", String.class)); // lastLoginIp字段已移除
        // user.setLoginFailureCount(row.get("failed_login_attempts", Integer.class)); // loginFailureCount字段已移除
        // user.setLockedTime(row.get("locked_until", LocalDateTime.class)); // lockedTime字段已移除

        // 设置审计字段
        user.setCreatedDate(row.get("created_at", LocalDateTime.class));
        user.setLastModifiedDate(row.get("last_modified_date", LocalDateTime.class));

        return user;
    }
}


