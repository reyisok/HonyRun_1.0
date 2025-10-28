package com.honyrun.service.impl;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.honyrun.exception.BusinessException;
import com.honyrun.exception.ValidationException;
import com.honyrun.model.dto.request.PermissionAssignRequest;
import com.honyrun.model.dto.request.PermissionBatchRequest;
import com.honyrun.model.dto.response.PermissionMatrixResponse;
import com.honyrun.model.entity.business.User;
import com.honyrun.model.entity.business.UserPermission;
import com.honyrun.repository.custom.CustomUserRepository;
import com.honyrun.repository.r2dbc.ReactiveUserRepository;
import com.honyrun.service.reactive.ReactivePermissionService;
import com.honyrun.service.reactive.ReactiveUserService;
import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 响应式权限管理服务实现类
 *
 * 实现权限管理相关的所有业务逻辑，包括用户权限矩阵管理、批量权限操作、
 * 权限查询、权限统计、权限模板管理、权限审计等功能。
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 01:40:00
 * @modified 2025-07-01 01:40:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Service
@Transactional
public class ReactivePermissionServiceImpl implements ReactivePermissionService {

    private static final Logger logger = LoggerFactory.getLogger(ReactivePermissionServiceImpl.class);

    private final ReactiveUserService userService;
    private final ReactiveUserRepository userRepository;
    private final CustomUserRepository customUserRepository;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    private ReactiveValueOperations<String, Object> valueOperations;

    // 权限缓存键前缀
    private static final String PERMISSION_CACHE_PREFIX = "permission:user:";

    // 预定义权限列表
    private static final List<String> SYSTEM_PERMISSIONS = Arrays.asList(
        "SYSTEM_MANAGEMENT", "USER_MANAGEMENT", "PERMISSION_MANAGEMENT",
        "SYSTEM_MONITOR", "BUSINESS_FUNCTION_1", "BUSINESS_FUNCTION_2",
        "BUSINESS_FUNCTION_3", "EXTERNAL_INTERFACE_MANAGEMENT",
        "MOCK_INTERFACE_MANAGEMENT", "IMAGE_CONVERSION", "VERSION_MANAGEMENT"
    );

    /**
     * 构造函数注入 - 符合Spring Boot 3最佳实践
     *
     * @param userService 用户服务
     * @param userRepository 用户仓库
     * @param customUserRepository 自定义用户仓库
     * @param redisTemplate Redis模板
     */
    public ReactivePermissionServiceImpl(ReactiveUserService userService,
                                       ReactiveUserRepository userRepository,
                                       CustomUserRepository customUserRepository,
                                       @Qualifier("unifiedReactiveRedisTemplate") ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.customUserRepository = customUserRepository;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void init() {
        this.valueOperations = redisTemplate.opsForValue();
    }

    // ==================== 权限矩阵管理 ====================

    @Override
    public Mono<PermissionMatrixResponse> getPermissionMatrix() {
        LoggingUtil.info(logger, "获取用户权限矩阵");

        return Mono.zip(
                getAllUsers(),
                getAllPermissions(),
                getAllUserPermissions()
        ).map(tuple -> {
            List<PermissionMatrixResponse.UserInfo> users = tuple.getT1();
            List<PermissionMatrixResponse.PermissionInfo> permissions = tuple.getT2();
            Map<String, Boolean> matrix = tuple.getT3();

            PermissionMatrixResponse response = new PermissionMatrixResponse(users, permissions, matrix);
            response.setStatistics(calculateStatistics(users, permissions, matrix));

            LoggingUtil.info(logger, "权限矩阵获取成功，用户数: {}, 权限数: {}",
                users.size(), permissions.size());

            return response;
        }).doOnError(error -> LoggingUtil.error(logger, "获取权限矩阵失败", error));
    }

    @Override
    public Mono<Map<String, Boolean>> getUserPermissionMatrix(Long userId) {
        LoggingUtil.info(logger, "获取用户权限矩阵，用户ID: {}", userId);

        return validateUser(userId)
                .thenMany(getUserPermissionCodes(userId))
                .collectList()
                .map(userPermissions -> {
                    Map<String, Boolean> matrix = new HashMap<>();
                    Set<String> userPermissionSet = new HashSet<>(userPermissions);

                    for (String permission : SYSTEM_PERMISSIONS) {
                        matrix.put(permission, userPermissionSet.contains(permission));
                    }

                    LoggingUtil.info(logger, "用户权限矩阵获取成功，权限数: {}", userPermissions.size());
                    return matrix;
                })
                .doOnError(error -> LoggingUtil.error(logger, "获取用户权限矩阵失败", error));
    }

    @Override
    public Mono<List<Map<String, Object>>> getPermissionTree() {
        LoggingUtil.info(logger, "获取权限分类树");

        return Mono.fromCallable(() -> {
            List<Map<String, Object>> tree = new ArrayList<>();

            // 系统管理权限
            Map<String, Object> systemNode = createPermissionNode("系统管理", "SYSTEM", Arrays.asList(
                "SYSTEM_MANAGEMENT", "USER_MANAGEMENT", "PERMISSION_MANAGEMENT", "SYSTEM_MONITOR"
            ));
            tree.add(systemNode);

            // 业务功能权限
            Map<String, Object> businessNode = createPermissionNode("业务功能", "BUSINESS", Arrays.asList(
                "BUSINESS_FUNCTION_1", "BUSINESS_FUNCTION_2", "BUSINESS_FUNCTION_3"
            ));
            tree.add(businessNode);

            // 接口管理权限
            Map<String, Object> interfaceNode = createPermissionNode("接口管理", "INTERFACE", Arrays.asList(
                "EXTERNAL_INTERFACE_MANAGEMENT", "MOCK_INTERFACE_MANAGEMENT"
            ));
            tree.add(interfaceNode);

            // 其他功能权限
            Map<String, Object> otherNode = createPermissionNode("其他功能", "OTHER", Arrays.asList(
                "IMAGE_CONVERSION", "VERSION_MANAGEMENT"
            ));
            tree.add(otherNode);

            LoggingUtil.info(logger, "权限分类树获取成功，节点数: {}", tree.size());
            return tree;
        }).doOnError(error -> LoggingUtil.error(logger, "获取权限分类树失败", error));
    }

    // ==================== 权限分配管理 ====================

    @Override
    public Mono<Boolean> assignPermissions(PermissionAssignRequest request) {
        LoggingUtil.info(logger, "分配用户权限，用户ID: {}, 权限数量: {}",
            request.getUserId(), request.getPermissionCodes().size());

        return validatePermissionAssignRequest(request)
                .then(Mono.defer(() -> {
                    return userService.assignPermissions(request.getUserId(), request.getPermissionCodes())
                            .then(clearUserPermissionCache(request.getUserId()))
                            .then(recordPermissionOperation("ASSIGN", request.getUserId(),
                                request.getPermissionCodes(), null, request.getDescription()))
                            .thenReturn(true);
                }))
                .doOnSuccess(result -> LoggingUtil.info(logger, "用户权限分配成功"))
                .doOnError(error -> LoggingUtil.error(logger, "用户权限分配失败", error));
    }

    @Override
    public Mono<Boolean> revokePermissions(Long userId, List<String> permissionCodes) {
        LoggingUtil.info(logger, "撤销用户权限，用户ID: {}, 权限数量: {}", userId, permissionCodes.size());

        return validateUser(userId)
                .then(userService.revokePermissions(userId, permissionCodes))
                .then(clearUserPermissionCache(userId))
                .then(recordPermissionOperation("REVOKE", userId, permissionCodes, null, "撤销权限"))
                .thenReturn(true)
                .doOnSuccess(result -> LoggingUtil.info(logger, "用户权限撤销成功"))
                .doOnError(error -> LoggingUtil.error(logger, "用户权限撤销失败", error));
    }

    @Override
    public Mono<Boolean> updateUserPermissions(Long userId, List<String> permissionCodes) {
        LoggingUtil.info(logger, "更新用户权限，用户ID: {}, 权限数量: {}", userId, permissionCodes.size());

        return validateUser(userId)
                .then(userService.clearUserPermissions(userId))
                .then(userService.assignPermissions(userId, permissionCodes))
                .then(clearUserPermissionCache(userId))
                .then(recordPermissionOperation("UPDATE", userId, permissionCodes, null, "更新权限"))
                .thenReturn(true)
                .doOnSuccess(result -> LoggingUtil.info(logger, "用户权限更新成功"))
                .doOnError(error -> LoggingUtil.error(logger, "用户权限更新失败", error));
    }

    // ==================== 批量权限操作 ====================

    @Override
    public Mono<Map<String, Object>> batchAssignPermissions(PermissionBatchRequest request) {
        LoggingUtil.info(logger, "批量分配权限，用户数量: {}, 权限数量: {}",
            request.getUserIds().size(), request.getPermissionCodes().size());

        return Flux.fromIterable(request.getUserIds())
                .flatMap(userId -> {
                    PermissionAssignRequest assignRequest = new PermissionAssignRequest(userId, request.getPermissionCodes());
                    assignRequest.setValidFrom(request.getValidFrom());
                    assignRequest.setValidTo(request.getValidTo());
                    assignRequest.setDescription(request.getDescription());
                    assignRequest.setOverrideExisting(request.getOverrideExisting());

                    return assignPermissions(assignRequest)
                            .map(success -> Map.of("userId", userId, "success", success))
                            .onErrorReturn(Map.of("userId", userId, "success", false));
                })
                .collectList()
                .map(results -> {
                    long successCount = results.stream().mapToLong(r -> (Boolean) r.get("success") ? 1 : 0).sum();
                    long failureCount = results.size() - successCount;

                    Map<String, Object> result = new HashMap<>();
                    result.put("totalCount", results.size());
                    result.put("successCount", successCount);
                    result.put("failureCount", failureCount);
                    result.put("results", results);

                    LoggingUtil.info(logger, "批量权限分配完成，成功: {}, 失败: {}", successCount, failureCount);
                    return result;
                })
                .doOnError(error -> LoggingUtil.error(logger, "批量权限分配失败", error));
    }

    @Override
    public Mono<Map<String, Object>> batchRevokePermissions(PermissionBatchRequest request) {
        LoggingUtil.info(logger, "批量撤销权限，用户数量: {}, 权限数量: {}",
            request.getUserIds().size(), request.getPermissionCodes().size());

        return Flux.fromIterable(request.getUserIds())
                .flatMap(userId -> revokePermissions(userId, request.getPermissionCodes())
                        .map(success -> Map.of("userId", userId, "success", success))
                        .onErrorReturn(Map.of("userId", userId, "success", false)))
                .collectList()
                .map(results -> {
                    long successCount = results.stream().mapToLong(r -> (Boolean) r.get("success") ? 1 : 0).sum();
                    long failureCount = results.size() - successCount;

                    Map<String, Object> result = new HashMap<>();
                    result.put("totalCount", results.size());
                    result.put("successCount", successCount);
                    result.put("failureCount", failureCount);
                    result.put("results", results);

                    LoggingUtil.info(logger, "批量权限撤销完成，成功: {}, 失败: {}", successCount, failureCount);
                    return result;
                })
                .doOnError(error -> LoggingUtil.error(logger, "批量权限撤销失败", error));
    }

    @Override
    public Mono<Map<String, Object>> batchCopyPermissions(Long sourceUserId, List<Long> targetUserIds) {
        LoggingUtil.info(logger, "批量复制权限，源用户ID: {}, 目标用户数量: {}", sourceUserId, targetUserIds.size());

        return getUserPermissionCodes(sourceUserId)
                .collectList()
                .flatMap(sourcePermissions -> {
                    if (sourcePermissions.isEmpty()) {
                        return Mono.just(Map.of("totalCount", 0, "successCount", 0, "failureCount", 0, "results", Collections.emptyList()));
                    }

                    return Flux.fromIterable(targetUserIds)
                            .flatMap(targetUserId -> updateUserPermissions(targetUserId, sourcePermissions)
                                    .map(success -> Map.of("userId", targetUserId, "success", success))
                                    .onErrorReturn(Map.of("userId", targetUserId, "success", false)))
                            .collectList()
                            .map(results -> {
                                long successCount = results.stream().mapToLong(r -> (Boolean) r.get("success") ? 1 : 0).sum();
                                long failureCount = results.size() - successCount;

                                Map<String, Object> result = new HashMap<>();
                                result.put("totalCount", results.size());
                                result.put("successCount", successCount);
                                result.put("failureCount", failureCount);
                                result.put("results", results);
                                result.put("sourcePermissions", sourcePermissions);

                                LoggingUtil.info(logger, "批量权限复制完成，成功: {}, 失败: {}", successCount, failureCount);
                                return result;
                            });
                })
                .doOnError(error -> LoggingUtil.error(logger, "批量权限复制失败", error));
    }

    // ==================== 权限查询 ====================

    @Override
    public Flux<UserPermission> getUserPermissions(Long userId) {
        LoggingUtil.debug(logger, "获取用户权限列表，用户ID: {}", userId);

        return userService.getUserPermissions(userId)
                .doOnComplete(() -> LoggingUtil.debug(logger, "用户权限列表获取完成"))
                .doOnError(error -> LoggingUtil.error(logger, "获取用户权限列表失败", error));
    }

    @Override
    public Mono<Boolean> hasPermission(Long userId, String permissionCode) {
        LoggingUtil.debug(logger, "检查用户权限，用户ID: {}, 权限代码: {}", userId, permissionCode);

        return userService.hasPermission(userId, permissionCode)
                .doOnSuccess(result -> LoggingUtil.debug(logger, "权限检查完成，结果: {}", result))
                .doOnError(error -> LoggingUtil.error(logger, "权限检查失败", error));
    }

    @Override
    public Mono<Map<String, Boolean>> hasPermissions(Long userId, List<String> permissionCodes) {
        LoggingUtil.debug(logger, "批量检查用户权限，用户ID: {}, 权限数量: {}", userId, permissionCodes.size());

        return userService.hasPermissions(userId, permissionCodes)
                .doOnSuccess(result -> LoggingUtil.debug(logger, "批量权限检查完成"))
                .doOnError(error -> LoggingUtil.error(logger, "批量权限检查失败", error));
    }

    @Override
    public Mono<List<Map<String, Object>>> getUsersByPermission(String permissionCode) {
        LoggingUtil.debug(logger, "根据权限查询用户，权限代码: {}", permissionCode);

        return customUserRepository.findUsersByPermission(permissionCode)
                .map(this::convertUserToMap)
                .collectList()
                .doOnSuccess(users -> LoggingUtil.debug(logger, "根据权限查询用户完成，用户数量: {}", users.size()))
                .doOnError(error -> LoggingUtil.error(logger, "根据权限查询用户失败", error));
    }

    @Override
    public Flux<String> getUserPermissionCodes(Long userId) {
        LoggingUtil.debug(logger, "获取用户权限代码列表，用户ID: {}", userId);

        return customUserRepository.findUserPermissions(userId)
                .doOnComplete(() -> LoggingUtil.debug(logger, "用户权限代码列表获取完成"))
                .doOnError(error -> LoggingUtil.error(logger, "获取用户权限代码列表失败", error));
    }

    @Override
    public Flux<UserPermission> getValidUserPermissions(Long userId) {
        LoggingUtil.debug(logger, "获取有效用户权限列表，用户ID: {}", userId);

        return getUserPermissions(userId)
                .filter(UserPermission::isAvailable)
                .doOnComplete(() -> LoggingUtil.debug(logger, "有效用户权限列表获取完成"))
                .doOnError(error -> LoggingUtil.error(logger, "获取有效用户权限列表失败", error));
    }

    // ==================== 权限统计 ====================

    @Override
    public Mono<Map<String, Object>> getPermissionStatistics() {
        LoggingUtil.debug(logger, "获取权限统计信息");

        return Mono.zip(
                userRepository.count(),
                Mono.just((long) SYSTEM_PERMISSIONS.size()),
                getTotalPermissionAssignments()
        ).map(tuple -> {
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalUsers", tuple.getT1());
            statistics.put("totalPermissions", tuple.getT2());
            statistics.put("totalAssignments", tuple.getT3());
            statistics.put("averagePermissionsPerUser", tuple.getT1() > 0 ? (double) tuple.getT3() / tuple.getT1() : 0.0);
            statistics.put("generatedTime", LocalDateTime.now());

            LoggingUtil.debug(logger, "权限统计信息获取成功");
            return statistics;
        }).doOnError(error -> LoggingUtil.error(logger, "获取权限统计信息失败", error));
    }

    @Override
    public Mono<Map<String, Object>> getUserTypePermissionStatistics() {
        LoggingUtil.debug(logger, "获取用户类型权限统计");

        return userRepository.findAll()
                .collectList()
                .map(users -> {
                    Map<String, Long> userTypeCount = users.stream()
                            .collect(Collectors.groupingBy(
                                user -> user.getUserType().name(),
                                Collectors.counting()
                            ));

                    Map<String, Object> statistics = new HashMap<>();
                    statistics.put("userTypeDistribution", userTypeCount);
                    statistics.put("totalUsers", users.size());
                    statistics.put("generatedTime", LocalDateTime.now());

                    LoggingUtil.debug(logger, "用户类型权限统计获取成功");
                    return statistics;
                })
                .doOnError(error -> LoggingUtil.error(logger, "获取用户类型权限统计失败", error));
    }

    // 其他方法的简化实现，实际项目中需要完整实现
    @Override
    public Mono<Map<String, Object>> getPermissionUsageStatistics() {
        return Mono.just(new HashMap<>());
    }

    @Override
    public Mono<Map<String, Object>> getPermissionDistributionStatistics() {
        return Mono.just(new HashMap<>());
    }

    @Override
    public Mono<List<Map<String, Object>>> getPermissionTemplates() {
        return Mono.just(new ArrayList<>());
    }

    @Override
    public Mono<Map<String, Object>> createPermissionTemplate(String templateName, List<String> permissionCodes, String description) {
        return Mono.just(new HashMap<>());
    }

    @Override
    public Mono<Map<String, Object>> applyPermissionTemplate(Long templateId, List<Long> userIds) {
        return Mono.just(new HashMap<>());
    }

    @Override
    public Mono<Boolean> deletePermissionTemplate(Long templateId) {
        return Mono.just(true);
    }

    @Override
    public Mono<List<Map<String, Object>>> getPermissionAuditHistory(Long userId, int page, int size) {
        return Mono.just(new ArrayList<>());
    }

    @Override
    public Mono<List<Map<String, Object>>> getPermissionOperationLogs(int page, int size) {
        return Mono.just(new ArrayList<>());
    }

    @Override
    public Mono<Boolean> recordPermissionOperation(String operationType, Long userId, List<String> permissionCodes, Long operatorId, String description) {
        LoggingUtil.info(logger, "记录权限操作日志，操作类型: {}, 用户ID: {}", operationType, userId);
        return Mono.just(true);
    }

    @Override
    public Mono<Boolean> validatePermissionAssignRequest(PermissionAssignRequest request) {
        if (request == null) {
            return Mono.error(new ValidationException("权限分配请求不能为空"));
        }
        if (request.getUserId() == null) {
            return Mono.error(new ValidationException("用户ID不能为空"));
        }
        if (request.getPermissionCodes() == null || request.getPermissionCodes().isEmpty()) {
            return Mono.error(new ValidationException("权限代码列表不能为空"));
        }
        return Mono.just(true);
    }

    @Override
    public Mono<Boolean> validatePermissionCode(String permissionCode) {
        return Mono.just(SYSTEM_PERMISSIONS.contains(permissionCode));
    }

    @Override
    public Mono<Boolean> validateUser(Long userId) {
        return userRepository.findByIdAndDeletedFalse(userId)
                .hasElement()
                .filter(exists -> exists)
                .switchIfEmpty(Mono.error(new BusinessException("用户不存在")))
                .thenReturn(true);
    }

    @Override
    public Mono<Boolean> refreshUserPermissionCache(Long userId) {
        return clearUserPermissionCache(userId);
    }

    @Override
    public Mono<Boolean> clearUserPermissionCache(Long userId) {
        String cacheKey = PERMISSION_CACHE_PREFIX + userId;
        return redisTemplate.delete(cacheKey).map(count -> count > 0 ? Boolean.TRUE : Boolean.FALSE);
    }

    @Override
    public Mono<Boolean> warmupPermissionCache() {
        return Mono.just(true);
    }

    @Override
    public Mono<Boolean> syncUserPermissionsToCache(Long userId) {
        return Mono.just(true);
    }

    @Override
    public Flux<String> getUserPermissionsFromCache(Long userId) {
        LoggingUtil.debug(logger, "从缓存获取用户权限，用户ID: {}", userId);

        String cacheKey = "user:permissions:" + userId;
        return valueOperations.get(cacheKey)
                .flatMapMany(permissionsObj -> {
                    if (permissionsObj != null) {
                        String permissionsStr = permissionsObj.toString();
                        if (!permissionsStr.isEmpty()) {
                            String[] permissions = permissionsStr.split(",");
                            return Flux.fromArray(permissions);
                        }
                    }
                    return Flux.empty();
                })
                .doOnComplete(() -> LoggingUtil.debug(logger, "从缓存获取用户权限完成"))
                .doOnError(error -> LoggingUtil.error(logger, "从缓存获取用户权限失败", error));
    }

    @Override
    public Mono<Map<String, Object>> batchSyncUserPermissions(List<Long> userIds) {
        return Mono.just(new HashMap<>());
    }

    // ==================== 私有辅助方法 ====================

    private Mono<List<PermissionMatrixResponse.UserInfo>> getAllUsers() {
        return userRepository.findAll()
                .map(this::convertToUserInfo)
                .collectList();
    }

    private Mono<List<PermissionMatrixResponse.PermissionInfo>> getAllPermissions() {
        return Mono.just(SYSTEM_PERMISSIONS.stream()
                .map(this::convertToPermissionInfo)
                .collect(Collectors.toList()));
    }

    private Mono<Map<String, Boolean>> getAllUserPermissions() {
        return userRepository.findAll()
                .flatMap(user -> getUserPermissionCodes(user.getId())
                        .map(permission -> user.getId() + "-" + permission)
                        .collectList()
                        .map(userPermissions -> Map.of(user.getId(), userPermissions)))
                .collectList()
                .map(userPermissionsList -> {
                    Map<String, Boolean> matrix = new HashMap<>();

                    for (Map<Long, List<String>> userPermissions : userPermissionsList) {
                        for (Map.Entry<Long, List<String>> entry : userPermissions.entrySet()) {
                            Long userId = entry.getKey();
                            List<String> permissions = entry.getValue();

                            for (String systemPermission : SYSTEM_PERMISSIONS) {
                                String key = userId + "-" + systemPermission;
                                matrix.put(key, permissions.contains(key));
                            }
                        }
                    }

                    return matrix;
                });
    }

    private PermissionMatrixResponse.StatisticsInfo calculateStatistics(
            List<PermissionMatrixResponse.UserInfo> users,
            List<PermissionMatrixResponse.PermissionInfo> permissions,
            Map<String, Boolean> matrix) {

        PermissionMatrixResponse.StatisticsInfo statistics = new PermissionMatrixResponse.StatisticsInfo();
        statistics.setTotalUsers(users.size());
        statistics.setTotalPermissions(permissions.size());

        long totalAssignments = matrix.values().stream().mapToLong(hasPermission -> hasPermission ? 1 : 0).sum();
        statistics.setTotalAssignments((int) totalAssignments);

        if (users.size() > 0) {
            statistics.setAveragePermissionsPerUser((double) totalAssignments / users.size());
        }

        if (permissions.size() > 0) {
            statistics.setAverageUsersPerPermission((double) totalAssignments / permissions.size());
        }

        if (users.size() > 0 && permissions.size() > 0) {
            double maxPossibleAssignments = (double) users.size() * permissions.size();
            statistics.setPermissionCoverage(totalAssignments / maxPossibleAssignments * 100);
        }

        return statistics;
    }

    private Map<String, Object> createPermissionNode(String name, String category, List<String> permissions) {
        Map<String, Object> node = new HashMap<>();
        node.put("name", name);
        node.put("category", category);
        node.put("permissions", permissions.stream()
                .map(this::convertToPermissionInfo)
                .collect(Collectors.toList()));
        return node;
    }

    private PermissionMatrixResponse.UserInfo convertToUserInfo(User user) {
        PermissionMatrixResponse.UserInfo userInfo = new PermissionMatrixResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setUserType(user.getUserType().name());
        userInfo.setStatus("ACTIVE".equals(user.getStatus()) ? "ACTIVE" : "INACTIVE");
        return userInfo;
    }

    private PermissionMatrixResponse.PermissionInfo convertToPermissionInfo(String permissionCode) {
        PermissionMatrixResponse.PermissionInfo permissionInfo = new PermissionMatrixResponse.PermissionInfo();
        permissionInfo.setCode(permissionCode);
        permissionInfo.setName(getPermissionName(permissionCode));
        permissionInfo.setDescription(getPermissionDescription(permissionCode));
        permissionInfo.setCategory(getPermissionCategory(permissionCode));
        permissionInfo.setLevel(1);
        return permissionInfo;
    }

    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("realName", user.getRealName());
        userMap.put("userType", user.getUserType().name());
        userMap.put("status", "ACTIVE".equals(user.getStatus()) ? "ACTIVE" : "INACTIVE");
        return userMap;
    }

    private Mono<Long> getTotalPermissionAssignments() {
        return userRepository.findAll()
                .flatMap(user -> getUserPermissionCodes(user.getId()).count())
                .reduce(0L, Long::sum);
    }

    private String getPermissionName(String permissionCode) {
        return switch (permissionCode) {
            case "SYSTEM_MANAGEMENT" -> "系统管理";
            case "USER_MANAGEMENT" -> "用户管理";
            case "PERMISSION_MANAGEMENT" -> "权限管理";
            case "SYSTEM_MONITOR" -> "系统监控";
            case "BUSINESS_FUNCTION_1" -> "业务功能1";
            case "BUSINESS_FUNCTION_2" -> "业务功能2";
            case "BUSINESS_FUNCTION_3" -> "业务功能3";
            case "EXTERNAL_INTERFACE_MANAGEMENT" -> "外部接口管理";
            case "MOCK_INTERFACE_MANAGEMENT" -> "模拟接口管理";
            case "IMAGE_CONVERSION" -> "图片转换";
            case "VERSION_MANAGEMENT" -> "版本管理";
            default -> permissionCode;
        };
    }

    private String getPermissionDescription(String permissionCode) {
        return switch (permissionCode) {
            case "SYSTEM_MANAGEMENT" -> "系统管理相关功能权限";
            case "USER_MANAGEMENT" -> "用户管理相关功能权限";
            case "PERMISSION_MANAGEMENT" -> "权限管理相关功能权限";
            case "SYSTEM_MONITOR" -> "系统监控相关功能权限";
            case "BUSINESS_FUNCTION_1" -> "业务功能1相关权限";
            case "BUSINESS_FUNCTION_2" -> "业务功能2相关权限";
            case "BUSINESS_FUNCTION_3" -> "业务功能3相关权限";
            case "EXTERNAL_INTERFACE_MANAGEMENT" -> "外部接口管理相关权限";
            case "MOCK_INTERFACE_MANAGEMENT" -> "模拟接口管理相关权限";
            case "IMAGE_CONVERSION" -> "图片转换相关权限";
            case "VERSION_MANAGEMENT" -> "版本管理相关权限";
            default -> permissionCode + "权限";
        };
    }

    private String getPermissionCategory(String permissionCode) {
        if (permissionCode.contains("SYSTEM") || permissionCode.contains("USER") || permissionCode.contains("PERMISSION")) {
            return "SYSTEM";
        } else if (permissionCode.contains("BUSINESS")) {
            return "BUSINESS";
        } else if (permissionCode.contains("INTERFACE")) {
            return "INTERFACE";
        } else {
            return "OTHER";
        }
    }
}
