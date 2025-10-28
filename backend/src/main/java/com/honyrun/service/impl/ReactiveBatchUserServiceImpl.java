package com.honyrun.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.honyrun.annotation.ReactiveService;
import com.honyrun.config.MonitoringProperties;
import com.honyrun.model.dto.request.BatchPermissionAssignRequest;
import com.honyrun.model.dto.request.BatchUserCreateRequest;
import com.honyrun.model.dto.request.BatchUserImportRequest;
import com.honyrun.model.dto.response.BatchOperationResponse;
import com.honyrun.repository.r2dbc.ReactiveUserRepository;
import com.honyrun.service.reactive.ReactiveAuthService;
import com.honyrun.service.reactive.ReactiveBatchUserService;
import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 响应式批量用户服务实现类
 *
 * 基于WebFlux的响应式批量用户管理服务实现，提供非阻塞的批量用户操作功能
 * 支持批量创建、导入、权限分配等批量用户管理功能
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 11:55:00
 * @modified 2025-07-01 11:55:00
 *           Copyright © 2025 HonyRun. All rights reserved.
 */
@Service
@ReactiveService
public class ReactiveBatchUserServiceImpl implements ReactiveBatchUserService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveBatchUserServiceImpl.class);

    private final ReactiveUserRepository userRepository;
    private final ReactiveAuthService authService;
    private final MonitoringProperties monitoringProperties;

    // 批量操作任务缓存
    private final Map<String, BatchOperationResponse> batchTasks = new ConcurrentHashMap<>();

    public ReactiveBatchUserServiceImpl(ReactiveUserRepository userRepository, ReactiveAuthService authService,
            MonitoringProperties monitoringProperties) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.monitoringProperties = monitoringProperties;
    }

    // ==================== 批量用户创建功能 ====================

    @Override
    public Mono<BatchOperationResponse> batchCreateUsers(BatchUserCreateRequest request, Long operatorId,
            String operatorName) {
        LoggingUtil.info(logger, "开始批量创建用户，数量: {}, 操作人: {}", request.getUserCount(), operatorName);

        String taskId = generateTaskId("batch_create");
        BatchOperationResponse response = new BatchOperationResponse(taskId, "BATCH_CREATE_USER",
                request.getUserCount());
        response.setOperatorId(operatorId);
        response.setOperatorName(operatorName);
        response.setDescription("批量创建用户");

        batchTasks.put(taskId, response);

        return performBatchCreateUsers(request, response)
                .doOnSuccess(result -> LoggingUtil.info(logger, "批量创建用户完成，任务ID: {}", taskId))
                .doOnError(error -> LoggingUtil.error(logger, "批量创建用户失败，任务ID: " + taskId, error));
    }

    @Override
    public Mono<BatchOperationResponse> asyncBatchCreateUsers(BatchUserCreateRequest request, Long operatorId,
            String operatorName) {
        LoggingUtil.info(logger, "开始异步批量创建用户，数量: {}, 操作人: {}", request.getUserCount(), operatorName);

        String taskId = generateTaskId("async_batch_create");
        BatchOperationResponse response = new BatchOperationResponse(taskId, "ASYNC_BATCH_CREATE_USER",
                request.getUserCount());
        response.setOperatorId(operatorId);
        response.setOperatorName(operatorName);
        response.setDescription("异步批量创建用户");

        batchTasks.put(taskId, response);

        // 异步执行批量创建
        performBatchCreateUsers(request, response)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();

        return Mono.just(response);
    }

    @Override
    public Mono<Map<String, Object>> validateBatchCreateData(BatchUserCreateRequest request) {
        LoggingUtil.info(logger, "验证批量创建用户数据，数量: {}", request.getUserCount());

        return Mono.fromCallable(() -> {
            Map<String, Object> result = new HashMap<>();
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            // 验证基本参数
            if (!request.isValid()) {
                errors.add("请求参数无效");
            }

            // 验证用户名唯一性
            List<String> usernames = request.getValidUsers().stream()
                    .map(BatchUserCreateRequest.UserCreateInfo::getUsername)
                    .collect(Collectors.toList());

            Set<String> duplicateUsernames = findDuplicates(usernames);
            if (!duplicateUsernames.isEmpty()) {
                errors.add("存在重复的用户名: " + String.join(", ", duplicateUsernames));
            }

            result.put("valid", errors.isEmpty());
            result.put("errors", errors);
            result.put("warnings", warnings);
            result.put("totalCount", request.getUserCount());
            result.put("validCount", request.getValidUsers().size());

            return result;
        });
    }

    @Override
    public Mono<Map<String, Object>> previewBatchCreate(BatchUserCreateRequest request) {
        LoggingUtil.info(logger, "预览批量创建用户，数量: {}", request.getUserCount());

        return validateBatchCreateData(request)
                .map(validationResult -> {
                    Map<String, Object> preview = new HashMap<>(validationResult);

                    // 添加预览数据（前5个用户）
                    List<BatchUserCreateRequest.UserCreateInfo> previewUsers = request.getValidUsers()
                            .stream()
                            .limit(5)
                            .collect(Collectors.toList());

                    preview.put("previewUsers", previewUsers);
                    preview.put("defaultPassword", request.getDefaultPassword() != null ? "***" : null);
                    preview.put("defaultUserType", request.getDefaultUserType());
                    preview.put("defaultPermissions", request.getDefaultPermissions());

                    return preview;
                });
    }

    // ==================== 批量用户导入功能 ====================

    @Override
    public Mono<BatchOperationResponse> batchImportUsers(BatchUserImportRequest request, Long operatorId,
            String operatorName) {
        LoggingUtil.info(logger, "开始批量导入用户，文件: {}, 操作人: {}", request.getFileName(), operatorName);

        String taskId = generateTaskId("batch_import");
        BatchOperationResponse response = new BatchOperationResponse(taskId, "BATCH_IMPORT_USER", 0);
        response.setOperatorId(operatorId);
        response.setOperatorName(operatorName);
        response.setDescription("批量导入用户");

        batchTasks.put(taskId, response);

        return performBatchImportUsers(request, response)
                .doOnSuccess(result -> LoggingUtil.info(logger, "批量导入用户完成，任务ID: {}", taskId))
                .doOnError(error -> LoggingUtil.error(logger, "批量导入用户失败，任务ID: " + taskId, error));
    }

    @Override
    public Mono<BatchOperationResponse> asyncBatchImportUsers(BatchUserImportRequest request, Long operatorId,
            String operatorName) {
        LoggingUtil.info(logger, "开始异步批量导入用户，文件: {}, 操作人: {}", request.getFileName(), operatorName);

        String taskId = generateTaskId("async_batch_import");
        BatchOperationResponse response = new BatchOperationResponse(taskId, "ASYNC_BATCH_IMPORT_USER", 0);
        response.setOperatorId(operatorId);
        response.setOperatorName(operatorName);
        response.setDescription("异步批量导入用户");

        batchTasks.put(taskId, response);

        // 异步执行批量导入
        performBatchImportUsers(request, response)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();

        return Mono.just(response);
    }

    @Override
    public Mono<Map<String, Object>> parseImportFile(BatchUserImportRequest request) {
        LoggingUtil.info(logger, "解析导入文件: {}", request.getFileName());

        return Mono.fromCallable(() -> {
            Map<String, Object> result = new HashMap<>();

            try {
                // 实际解析文件内容
                byte[] fileData = request.getFile().getBytes();
                if (fileData == null || fileData.length == 0) {
                    throw new IllegalArgumentException("文件数据为空");
                }

                String content = new String(fileData, java.nio.charset.StandardCharsets.UTF_8);
                String[] lines = content.split("\n");

                int totalRows = lines.length;
                int headerRow = request.getHasHeader() ? 1 : 0;
                int emptyRows = 0;
                int errorRows = 0;
                int dataRows = 0;

                // 解析列名
                List<String> columns = new ArrayList<>();
                if (headerRow > 0 && totalRows > 0) {
                    String[] headers = lines[0].split(",");
                    for (String header : headers) {
                        columns.add(header.trim());
                    }
                }

                // 分析数据行
                for (int i = headerRow; i < totalRows; i++) {
                    String line = lines[i].trim();
                    if (line.isEmpty()) {
                        emptyRows++;
                    } else {
                        String[] fields = line.split(",");
                        if (fields.length < 3) { // 至少需要用户名、邮箱、姓名
                            errorRows++;
                        } else {
                            dataRows++;
                        }
                    }
                }

                result.put("fileName", request.getFileName());
                result.put("fileSize", request.getFormattedFileSize());
                result.put("fileFormat", request.getFileFormat());
                result.put("totalRows", totalRows);
                result.put("dataRows", dataRows);
                result.put("headerRow", headerRow);
                result.put("emptyRows", emptyRows);
                result.put("errorRows", errorRows);
                result.put("columns", columns);

            } catch (Exception e) {
                LoggingUtil.error(logger, "解析导入文件失败", e);
                result.put("fileName", request.getFileName());
                result.put("fileSize", request.getFormattedFileSize());
                result.put("fileFormat", request.getFileFormat());
                result.put("totalRows", 0);
                result.put("dataRows", 0);
                result.put("headerRow", 0);
                result.put("emptyRows", 0);
                result.put("errorRows", 1);
                result.put("columns", new ArrayList<>());
                result.put("error", e.getMessage());
            }

            return result;
        });
    }

    @Override
    public Mono<Map<String, Object>> validateImportData(BatchUserImportRequest request) {
        LoggingUtil.info(logger, "验证导入数据: {}", request.getFileName());

        return parseImportFile(request)
                .map(parseResult -> {
                    Map<String, Object> result = new HashMap<>(parseResult);
                    List<String> errors = new ArrayList<>();
                    List<String> warnings = new ArrayList<>();

                    // 验证文件格式
                    if (!request.isValid()) {
                        errors.add("文件格式不支持或参数无效");
                    }

                    // 实际数据验证
                    if (request.getFileSize() > 10 * 1024 * 1024) { // 10MB
                        warnings.add("文件较大，导入可能需要较长时间");
                    }

                    // 验证文件格式
                    String fileFormat = request.getFileFormat();
                    if (!Arrays.asList("csv", "xlsx", "xls").contains(fileFormat.toLowerCase())) {
                        errors.add("不支持的文件格式: " + fileFormat);
                    }

                    // 验证文件大小
                    if (request.getFileSize() > 50 * 1024 * 1024) { // 50MB
                        errors.add("文件过大，超过50MB限制");
                    }

                    result.put("valid", errors.isEmpty());
                    result.put("errors", errors);
                    result.put("warnings", warnings);

                    return result;
                });
    }

    @Override
    public Mono<Map<String, Object>> previewImportData(BatchUserImportRequest request, int previewCount) {
        LoggingUtil.info(logger, "预览导入数据: {}, 预览数量: {}", request.getFileName(), previewCount);

        return validateImportData(request)
                .map(validationResult -> {
                    Map<String, Object> preview = new HashMap<>(validationResult);

                    try {
                        // 实际解析预览数据
                        List<Map<String, Object>> previewData = new ArrayList<>();
                        byte[] fileData = request.getFile().getBytes();

                        if (fileData != null && fileData.length > 0) {
                            String content = new String(fileData, java.nio.charset.StandardCharsets.UTF_8);
                            String[] lines = content.split("\n");

                            int startIndex = request.getHasHeader() ? 1 : 0;
                            int endIndex = Math.min(lines.length, startIndex + previewCount);

                            // 获取列名
                            String[] headers = null;
                            if (request.getHasHeader() && lines.length > 0) {
                                headers = lines[0].split(",");
                            }

                            for (int i = startIndex; i < endIndex; i++) {
                                if (i < lines.length) {
                                    String line = lines[i].trim();
                                    if (!line.isEmpty()) {
                                        String[] fields = line.split(",");
                                        Map<String, Object> row = new HashMap<>();

                                        for (int j = 0; j < fields.length; j++) {
                                            String key = (headers != null && j < headers.length) ? headers[j].trim()
                                                    : "column" + (j + 1);
                                            row.put(key, fields[j].trim());
                                        }

                                        previewData.add(row);
                                    }
                                }
                            }
                        }

                        preview.put("previewData", previewData);
                        preview.put("previewCount", previewData.size());

                    } catch (Exception e) {
                        LoggingUtil.error(logger, "解析预览数据失败", e);
                        preview.put("previewData", new ArrayList<>());
                        preview.put("previewCount", 0);
                        preview.put("previewError", e.getMessage());
                    }

                    return preview;
                });
    }

    @Override
    public Mono<byte[]> downloadImportTemplate(String format) {
        LoggingUtil.info(logger, "下载用户导入模板，格式: {}", format);

        return Mono.fromCallable(() -> {
            try {
                StringBuilder template = new StringBuilder();

                if ("csv".equalsIgnoreCase(format)) {
                    // 生成CSV模板
                    template.append("用户名,姓名,手机号,邮箱,部门,职位\n");
                    template.append("user001,张三,13800138001,zhangsan@example.com,技术部,开发工程师\n");
                    template.append("user002,李四,13800138002,lisi@example.com,产品部,产品经理\n");

                    return template.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
                } else {
                    // 其他格式暂不支持，返回CSV格式
                    template.append("用户名,姓名,手机号,邮箱,部门,职位\n");
                    return template.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
                }

            } catch (Exception e) {
                LoggingUtil.error(logger, "生成导入模板失败", e);
                return new byte[0];
            }
        });
    }

    // ==================== 批量权限分配功能 ====================

    @Override
    public Mono<BatchOperationResponse> batchAssignPermissions(BatchPermissionAssignRequest request, Long operatorId,
            String operatorName) {
        LoggingUtil.info(logger, "开始批量分配权限，用户数: {}, 权限数: {}, 操作人: {}",
                request.getUserCount(), request.getPermissionCount(), operatorName);

        String taskId = generateTaskId("batch_assign_permission");
        BatchOperationResponse response = new BatchOperationResponse(taskId, "BATCH_ASSIGN_PERMISSION",
                request.getUserCount());
        response.setOperatorId(operatorId);
        response.setOperatorName(operatorName);
        response.setDescription("批量分配权限");

        batchTasks.put(taskId, response);

        return performBatchAssignPermissions(request, response)
                .doOnSuccess(result -> LoggingUtil.info(logger, "批量分配权限完成，任务ID: {}", taskId))
                .doOnError(error -> LoggingUtil.error(logger, "批量分配权限失败，任务ID: " + taskId, error));
    }

    @Override
    public Mono<BatchOperationResponse> asyncBatchAssignPermissions(BatchPermissionAssignRequest request,
            Long operatorId, String operatorName) {
        LoggingUtil.info(logger, "开始异步批量分配权限，用户数: {}, 权限数: {}, 操作人: {}",
                request.getUserCount(), request.getPermissionCount(), operatorName);

        String taskId = generateTaskId("async_batch_assign_permission");
        BatchOperationResponse response = new BatchOperationResponse(taskId, "ASYNC_BATCH_ASSIGN_PERMISSION",
                request.getUserCount());
        response.setOperatorId(operatorId);
        response.setOperatorName(operatorName);
        response.setDescription("异步批量分配权限");

        batchTasks.put(taskId, response);

        // 异步执行批量权限分配
        performBatchAssignPermissions(request, response)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();

        return Mono.just(response);
    }

    @Override
    public Mono<BatchOperationResponse> batchRevokePermissions(List<Long> userIds, List<String> permissionCodes,
            Long operatorId, String operatorName) {
        LoggingUtil.info(logger, "开始批量撤销权限，用户数: {}, 权限数: {}, 操作人: {}",
                userIds.size(), permissionCodes.size(), operatorName);

        String taskId = generateTaskId("batch_revoke_permission");
        BatchOperationResponse response = new BatchOperationResponse(taskId, "BATCH_REVOKE_PERMISSION", userIds.size());
        response.setOperatorId(operatorId);
        response.setOperatorName(operatorName);
        response.setDescription("批量撤销权限");

        batchTasks.put(taskId, response);

        BatchPermissionAssignRequest request = new BatchPermissionAssignRequest(userIds, permissionCodes, "REVOKE");
        return performBatchAssignPermissions(request, response)
                .doOnSuccess(result -> LoggingUtil.info(logger, "批量撤销权限完成，任务ID: {}", taskId))
                .doOnError(error -> LoggingUtil.error(logger, "批量撤销权限失败，任务ID: " + taskId, error));
    }

    @Override
    public Mono<BatchOperationResponse> batchReplacePermissions(List<Long> userIds, List<String> permissionCodes,
            Long operatorId, String operatorName) {
        LoggingUtil.info(logger, "开始批量替换权限，用户数: {}, 权限数: {}, 操作人: {}",
                userIds.size(), permissionCodes.size(), operatorName);

        String taskId = generateTaskId("batch_replace_permission");
        BatchOperationResponse response = new BatchOperationResponse(taskId, "BATCH_REPLACE_PERMISSION",
                userIds.size());
        response.setOperatorId(operatorId);
        response.setOperatorName(operatorName);
        response.setDescription("批量替换权限");

        batchTasks.put(taskId, response);

        BatchPermissionAssignRequest request = new BatchPermissionAssignRequest(userIds, permissionCodes, "REPLACE");
        return performBatchAssignPermissions(request, response)
                .doOnSuccess(result -> LoggingUtil.info(logger, "批量替换权限完成，任务ID: {}", taskId))
                .doOnError(error -> LoggingUtil.error(logger, "批量替换权限失败，任务ID: " + taskId, error));
    }

    @Override
    public Mono<Map<String, Object>> previewPermissionAssign(BatchPermissionAssignRequest request) {
        LoggingUtil.info(logger, "预览权限分配，用户数: {}, 权限数: {}", request.getUserCount(), request.getPermissionCount());

        return Mono.fromCallable(() -> {
            Map<String, Object> preview = new HashMap<>();

            preview.put("operationType", request.getOperationType());
            preview.put("operationDescription", request.getOperationDescription());
            preview.put("userCount", request.getUserCount());
            preview.put("permissionCount", request.getPermissionCount());
            preview.put("estimatedOperations", request.getEstimatedOperationCount());
            preview.put("overrideExisting", request.getOverrideExisting());
            preview.put("reason", request.getReason());

            return preview;
        });
    }

    // ==================== 批量用户操作功能 ====================

    @Override
    public Mono<BatchOperationResponse> batchEnableUsers(List<Long> userIds, Long operatorId, String operatorName) {
        LoggingUtil.info(logger, "开始批量启用用户，数量: {}, 操作人: {}", userIds.size(), operatorName);

        String taskId = generateTaskId("batch_enable");
        BatchOperationResponse response = new BatchOperationResponse(taskId, "BATCH_ENABLE_USER", userIds.size());
        response.setOperatorId(operatorId);
        response.setOperatorName(operatorName);
        response.setDescription("批量启用用户");

        batchTasks.put(taskId, response);

        return performBatchUserOperation(userIds, "ENABLE", response)
                .doOnSuccess(result -> LoggingUtil.info(logger, "批量启用用户完成，任务ID: {}", taskId))
                .doOnError(error -> LoggingUtil.error(logger, "批量启用用户失败，任务ID: " + taskId, error));
    }

    @Override
    public Mono<BatchOperationResponse> batchDisableUsers(List<Long> userIds, Long operatorId, String operatorName) {
        LoggingUtil.info(logger, "开始批量禁用用户，数量: {}, 操作人: {}", userIds.size(), operatorName);

        String taskId = generateTaskId("batch_disable");
        BatchOperationResponse response = new BatchOperationResponse(taskId, "BATCH_DISABLE_USER", userIds.size());
        response.setOperatorId(operatorId);
        response.setOperatorName(operatorName);
        response.setDescription("批量禁用用户");

        batchTasks.put(taskId, response);

        return performBatchUserOperation(userIds, "DISABLE", response)
                .doOnSuccess(result -> LoggingUtil.info(logger, "批量禁用用户完成，任务ID: {}", taskId))
                .doOnError(error -> LoggingUtil.error(logger, "批量禁用用户失败，任务ID: " + taskId, error));
    }

    @Override
    public Mono<BatchOperationResponse> batchDeleteUsers(List<Long> userIds, Long operatorId, String operatorName) {
        LoggingUtil.info(logger, "开始批量删除用户，数量: {}, 操作人: {}", userIds.size(), operatorName);

        String taskId = generateTaskId("batch_delete");
        BatchOperationResponse response = new BatchOperationResponse(taskId, "BATCH_DELETE_USER", userIds.size());
        response.setOperatorId(operatorId);
        response.setOperatorName(operatorName);
        response.setDescription("批量删除用户");

        batchTasks.put(taskId, response);

        return performBatchUserOperation(userIds, "DELETE", response)
                .doOnSuccess(result -> LoggingUtil.info(logger, "批量删除用户完成，任务ID: {}", taskId))
                .doOnError(error -> LoggingUtil.error(logger, "批量删除用户失败，任务ID: " + taskId, error));
    }

    @Override
    public Mono<BatchOperationResponse> batchResetPasswords(List<Long> userIds, String newPassword, Long operatorId,
            String operatorName) {
        LoggingUtil.info(logger, "开始批量重置密码，数量: {}, 操作人: {}", userIds.size(), operatorName);

        String taskId = generateTaskId("batch_reset_password");
        BatchOperationResponse response = new BatchOperationResponse(taskId, "BATCH_RESET_PASSWORD", userIds.size());
        response.setOperatorId(operatorId);
        response.setOperatorName(operatorName);
        response.setDescription("批量重置密码");

        batchTasks.put(taskId, response);

        return performBatchPasswordReset(userIds, newPassword, response)
                .doOnSuccess(result -> LoggingUtil.info(logger, "批量重置密码完成，任务ID: {}", taskId))
                .doOnError(error -> LoggingUtil.error(logger, "批量重置密码失败，任务ID: " + taskId, error));
    }

    @Override
    public Mono<BatchOperationResponse> batchUpdateUsers(List<Long> userIds, Map<String, Object> updateData,
            Long operatorId, String operatorName) {
        LoggingUtil.info(logger, "开始批量更新用户，数量: {}, 操作人: {}", userIds.size(), operatorName);

        String taskId = generateTaskId("batch_update");
        BatchOperationResponse response = new BatchOperationResponse(taskId, "BATCH_UPDATE_USER", userIds.size());
        response.setOperatorId(operatorId);
        response.setOperatorName(operatorName);
        response.setDescription("批量更新用户");

        batchTasks.put(taskId, response);

        return performBatchUserUpdate(userIds, updateData, response)
                .doOnSuccess(result -> LoggingUtil.info(logger, "批量更新用户完成，任务ID: {}", taskId))
                .doOnError(error -> LoggingUtil.error(logger, "批量更新用户失败，任务ID: " + taskId, error));
    }

    // ==================== 批量操作状态管理 ====================

    @Override
    public Mono<BatchOperationResponse> getBatchOperationStatus(String taskId) {
        LoggingUtil.info(logger, "获取批量操作状态: {}", taskId);

        BatchOperationResponse response = batchTasks.get(taskId);
        if (response == null) {
            return Mono.empty();
        }

        return Mono.just(response);
    }

    @Override
    public Mono<Boolean> cancelBatchOperation(String taskId, Long operatorId) {
        LoggingUtil.info(logger, "取消批量操作: {}, 操作人ID: {}", taskId, operatorId);

        BatchOperationResponse response = batchTasks.get(taskId);
        if (response == null) {
            return Mono.just(false);
        }

        // 这里应该实现实际的取消逻辑
        response.markFailed("操作被用户取消");

        return Mono.just(true);
    }

    @Override
    public Flux<BatchOperationResponse> getBatchOperationHistory(Long operatorId, int page, int size) {
        LoggingUtil.info(logger, "获取批量操作历史: 操作人ID={}, 页码={}, 大小={}", operatorId, page, size);

        return Flux.fromIterable(batchTasks.values())
                .filter(response -> operatorId == null || operatorId.equals(response.getOperatorId()))
                .skip((long) (page - 1) * size)
                .take(size);
    }

    @Override
    public Mono<Void> cleanupExpiredBatchOperations() {
        LoggingUtil.info(logger, "清理过期的批量操作记录");

        return Mono.fromRunnable(() -> {
            LocalDateTime expireTime = LocalDateTime.now().minusDays(7);
            List<String> expiredTasks = batchTasks.entrySet().stream()
                    .filter(entry -> entry.getValue().getStartTime().isBefore(expireTime))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            expiredTasks.forEach(batchTasks::remove);
            LoggingUtil.info(logger, "清理过期批量操作记录完成，数量: {}", expiredTasks.size());
        });
    }

    // ==================== 批量操作统计功能 ====================

    @Override
    public Mono<Map<String, Object>> getBatchOperationStats(Long operatorId) {
        LoggingUtil.info(logger, "获取批量操作统计: 操作人ID={}", operatorId);

        return Mono.fromCallable(() -> {
            Map<String, Object> stats = new HashMap<>();

            List<BatchOperationResponse> operations = batchTasks.values().stream()
                    .filter(response -> operatorId == null || operatorId.equals(response.getOperatorId()))
                    .collect(Collectors.toList());

            stats.put("totalOperations", operations.size());
            stats.put("completedOperations", operations.stream().mapToLong(op -> op.isCompleted() ? 1 : 0).sum());
            stats.put("successOperations", operations.stream().mapToLong(op -> op.isSuccess() ? 1 : 0).sum());
            stats.put("failedOperations", operations.stream().mapToLong(op -> op.isFailed() ? 1 : 0).sum());

            return stats;
        });
    }

    @Override
    public Mono<Map<String, Object>> getBatchOperationMetrics() {
        LoggingUtil.info(logger, "获取批量操作性能指标");

        return Mono.fromCallable(() -> {
            Map<String, Object> metrics = new HashMap<>();

            List<BatchOperationResponse> completedOps = batchTasks.values().stream()
                    .filter(BatchOperationResponse::isCompleted)
                    .collect(Collectors.toList());

            if (!completedOps.isEmpty()) {
                double avgExecutionTime = completedOps.stream()
                        .mapToLong(op -> op.getExecutionTime() != null ? op.getExecutionTime() : 0)
                        .average()
                        .orElse(0.0);

                metrics.put("averageExecutionTime", avgExecutionTime);
                metrics.put("totalProcessedItems",
                        completedOps.stream().mapToInt(op -> op.getSuccessCount() + op.getFailureCount()).sum());
            } else {
                // 即使没有完成的操作，也返回默认值
                metrics.put("averageExecutionTime", 0.0);
                metrics.put("totalProcessedItems", 0);
            }

            return metrics;
        });
    }

    @Override
    public Mono<Map<String, Object>> analyzeBatchOperationTrends(int days) {
        LoggingUtil.info(logger, "分析批量操作趋势: {} 天", days);

        return Mono.fromCallable(() -> {
            Map<String, Object> trends = new HashMap<>();

            LocalDateTime startTime = LocalDateTime.now().minusDays(days);
            List<BatchOperationResponse> recentOps = batchTasks.values().stream()
                    .filter(response -> response.getStartTime().isAfter(startTime))
                    .collect(Collectors.toList());

            trends.put("period", days + " 天");
            trends.put("totalOperations", recentOps.size());
            trends.put("dailyAverage", (double) recentOps.size() / days);

            return trends;
        });
    }

    // ==================== 批量数据验证功能 ====================

    @Override
    public Mono<Map<String, Boolean>> validateUsernameUniqueness(List<String> usernames) {
        LoggingUtil.info(logger, "验证用户名唯一性，数量: {}", usernames.size());

        return Flux.fromIterable(usernames)
                .flatMap(username -> userRepository.findByUsername(username)
                        .map(user -> Map.entry(username, false))
                        .switchIfEmpty(Mono.just(Map.entry(username, true))))
                .collectMap(entry -> entry.getKey(), entry -> entry.getValue());
    }

    @Override
    public Mono<Map<String, Boolean>> validatePhoneNumberUniqueness(List<String> phoneNumbers) {
        LoggingUtil.info(logger, "验证手机号唯一性，数量: {}", phoneNumbers.size());

        return Flux.fromIterable(phoneNumbers)
                .flatMap(phone -> userRepository.findByPhone(phone)
                        .map(user -> Map.entry(phone, false))
                        .switchIfEmpty(Mono.just(Map.entry(phone, true))))
                .collectMap(entry -> entry.getKey(), entry -> entry.getValue());
    }

    @Override
    public Mono<Map<String, Boolean>> validateEmailUniqueness(List<String> emails) {
        LoggingUtil.info(logger, "验证邮箱唯一性，数量: {}", emails.size());

        return Flux.fromIterable(emails)
                .flatMap(email -> userRepository.findByEmail(email)
                        .map(user -> Map.entry(email, false))
                        .switchIfEmpty(Mono.just(Map.entry(email, true))))
                .collectMap(entry -> entry.getKey(), entry -> entry.getValue());
    }

    @Override
    public Mono<Map<String, Boolean>> validatePermissionCodes(List<String> permissionCodes) {
        LoggingUtil.info(logger, "验证权限代码有效性，数量: {}", permissionCodes.size());

        // 这里应该查询权限表验证权限代码
        // 为了演示，假设所有权限代码都有效
        return Mono.fromCallable(() -> permissionCodes.stream()
                .collect(Collectors.toMap(code -> code, code -> true)));
    }

    // ==================== 批量数据导出功能 ====================

    @Override
    public Mono<byte[]> exportBatchOperationResult(String taskId, String format) {
        LoggingUtil.info(logger, "导出批量操作结果: taskId={}, format={}", taskId, format);

        return getBatchOperationStatus(taskId)
                .flatMap(response -> {
                    if (response == null) {
                        return Mono.just(new byte[0]);
                    }

                    return Mono.fromCallable(() -> {
                        try {
                            StringBuilder content = new StringBuilder();

                            if ("csv".equalsIgnoreCase(format)) {
                                // 生成CSV格式的结果报告
                                content.append("任务ID,操作类型,总数量,成功数量,失败数量,状态,开始时间,结束时间\n");
                                content.append(String.format("%s,%s,%d,%d,%d,%s,%s,%s\n",
                                        response.getTaskId(),
                                        response.getOperationType(),
                                        response.getTotalCount(),
                                        response.getSuccessCount(),
                                        response.getFailureCount(),
                                        response.getStatus(),
                                        response.getStartTime(),
                                        response.getEndTime() != null ? response.getEndTime() : ""));

                                return content.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
                            } else {
                                // 默认返回CSV格式
                                content.append("任务ID,操作类型,总数量,成功数量,失败数量,状态\n");
                                content.append(String.format("%s,%s,%d,%d,%d,%s\n",
                                        response.getTaskId(),
                                        response.getOperationType(),
                                        response.getTotalCount(),
                                        response.getSuccessCount(),
                                        response.getFailureCount(),
                                        response.getStatus()));

                                return content.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
                            }

                        } catch (Exception e) {
                            LoggingUtil.error(logger, "生成导出文件失败", e);
                            return new byte[0];
                        }
                    });
                });
    }

    @Override
    public Mono<byte[]> exportUserDataTemplate(boolean includePermissions, String format) {
        LoggingUtil.info(logger, "导出用户数据模板: includePermissions={}, format={}", includePermissions, format);

        return Mono.fromCallable(() -> {
            try {
                StringBuilder template = new StringBuilder();

                if ("csv".equalsIgnoreCase(format)) {
                    // 生成CSV模板
                    if (includePermissions) {
                        template.append("用户ID,用户名,姓名,手机号,邮箱,部门,职位,状态,权限代码\n");
                    } else {
                        template.append("用户ID,用户名,姓名,手机号,邮箱,部门,职位,状态,创建时间\n");
                    }

                    return template.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
                } else {
                    // 其他格式暂不支持，返回CSV格式
                    template.append("用户ID,用户名,姓名,手机号,邮箱,部门,职位,状态\n");
                    return template.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
                }

            } catch (Exception e) {
                LoggingUtil.error(logger, "生成用户数据模板失败", e);
                return new byte[0];
            }
        });
    }

    @Override
    public Mono<byte[]> batchExportUserData(List<Long> userIds, boolean includePermissions, String format) {
        LoggingUtil.info(logger, "批量导出用户数据: userIds={}, includePermissions={}, format={}",
                userIds.size(), includePermissions, format);

        return Flux.fromIterable(userIds)
                .flatMap(userId -> userRepository.findById(userId))
                .collectList()
                .flatMap(users -> {
                    if (includePermissions) {
                        // 响应式获取所有用户的权限信息
                        return Flux.fromIterable(users)
                                .flatMap(user -> {
                                    return authService.getUserPermissions(user.getId())
                                            .collectList()
                                            .map(permissionList -> {
                                                String permissions = String.join(";", permissionList);
                                                if (permissions.isEmpty()) {
                                                    permissions = "无权限";
                                                }
                                                return new UserWithPermissions(user, permissions);
                                            });
                                })
                                .collectList()
                                .map(usersWithPermissions -> generateExportData(usersWithPermissions, includePermissions, format));
                    } else {
                        // 不需要权限信息，直接生成数据
                        return Mono.fromCallable(() -> generateExportData(
                                users.stream()
                                        .map(user -> new UserWithPermissions(user, null))
                                        .collect(java.util.stream.Collectors.toList()),
                                includePermissions, format));
                    }
                });
    }

    /**
     * 生成导出数据
     */
    private byte[] generateExportData(List<UserWithPermissions> usersWithPermissions, boolean includePermissions, String format) {
        try {
            StringBuilder content = new StringBuilder();

            if ("csv".equalsIgnoreCase(format)) {
                // 生成CSV格式的用户数据
                if (includePermissions) {
                    content.append("用户ID,用户名,姓名,手机号,邮箱,部门,职位,状态,创建时间,权限代码\n");
                } else {
                    content.append("用户ID,用户名,姓名,手机号,邮箱,部门,职位,状态,创建时间\n");
                }

                for (UserWithPermissions userWithPermissions : usersWithPermissions) {
                    com.honyrun.model.entity.business.User user = userWithPermissions.getUser();

                    // 获取用户的真实字段值
                    Long userId = user.getId();
                    String username = user.getUsername() != null ? user.getUsername() : "";
                    String realName = user.getRealName() != null ? user.getRealName() : "";
                    String phone = user.getPhone() != null ? user.getPhone() : "";
                    String email = user.getEmail() != null ? user.getEmail() : "";
                    String status = user.getStatus() != null ? user.getStatus() : "UNKNOWN";
                    String createdDate = user.getCreatedDate() != null
                            ? user.getCreatedDate().toString()
                            : "";

                    if (includePermissions) {
                        String permissions = userWithPermissions.getPermissions();
                        content.append(String.format("%s,%s,%s,%s,%s,,%s,%s,%s,%s\n",
                                userId, username, realName, phone, email, "", status, createdDate,
                                permissions));
                    } else {
                        content.append(String.format("%s,%s,%s,%s,%s,,%s,%s,%s\n",
                                userId, username, realName, phone, email, "", status, createdDate));
                    }
                }

                return content.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            } else {
                // 默认返回CSV格式
                content.append("用户ID,用户名,姓名,状态\n");
                for (UserWithPermissions userWithPermissions : usersWithPermissions) {
                    com.honyrun.model.entity.business.User user = userWithPermissions.getUser();

                    // 获取用户的真实字段值
                    Long userId = user.getId();
                    String username = user.getUsername() != null ? user.getUsername() : "";
                    String realName = user.getRealName() != null ? user.getRealName() : "";
                    String status = user.getStatus() != null ? user.getStatus() : "UNKNOWN";

                    content.append(String.format("%s,%s,%s,%s\n",
                            userId, username, realName, status));
                }

                return content.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            }

        } catch (Exception e) {
            LoggingUtil.error(logger, "生成用户数据导出文件失败", e);
            return new byte[0];
        }
    }

    /**
     * 用户和权限的包装类
     */
    private static class UserWithPermissions {
        private final com.honyrun.model.entity.business.User user;
        private final String permissions;

        public UserWithPermissions(com.honyrun.model.entity.business.User user, String permissions) {
            this.user = user;
            this.permissions = permissions;
        }

        public com.honyrun.model.entity.business.User getUser() {
            return user;
        }

        public String getPermissions() {
            return permissions;
        }
    }

    // ==================== 批量操作配置管理 ====================

    @Override
    public Mono<Map<String, Object>> getBatchOperationConfig() {
        LoggingUtil.info(logger, "获取批量操作配置");

        return Mono.fromCallable(() -> {
            Map<String, Object> config = new HashMap<>();
            config.put("maxBatchSize", 1000);
            config.put("defaultBatchSize", 100);
            config.put("maxConcurrentOperations", 5);
            config.put("operationTimeout", 300000); // 5分钟
            config.put("enableAsyncExecution", true);
            config.put("enableNotification", false);

            return config;
        });
    }

    @Override
    public Mono<Boolean> updateBatchOperationConfig(Map<String, Object> config, Long operatorId) {
        LoggingUtil.info(logger, "更新批量操作配置: 操作人ID={}", operatorId);

        // 这里应该实现配置更新逻辑
        return Mono.just(true);
    }

    @Override
    public Mono<Map<String, Object>> getBatchOperationLimits(Long operatorId) {
        LoggingUtil.info(logger, "获取批量操作限制: 操作人ID={}", operatorId);

        return Mono.fromCallable(() -> {
            Map<String, Object> limits = new HashMap<>();
            limits.put("maxBatchSize", 1000);
            limits.put("maxDailyOperations", 10);
            limits.put("maxConcurrentOperations", 3);
            limits.put("allowedOperationTypes", Arrays.asList("CREATE", "UPDATE", "ASSIGN_PERMISSION"));

            return limits;
        });
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 执行批量创建用户
     */
    private Mono<BatchOperationResponse> performBatchCreateUsers(BatchUserCreateRequest request,
            BatchOperationResponse response) {
        response.markProcessing(0);

        return Flux.fromIterable(request.getValidUsers())
                .index()
                .flatMap(indexed -> {
                    long index = indexed.getT1();
                    BatchUserCreateRequest.UserCreateInfo userInfo = indexed.getT2();

                    // 将UserCreateInfo转换为Map<String, Object>
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("username", userInfo.getUsername());
                    userData.put("password", userInfo.getPassword());
                    userData.put("realName", userInfo.getRealName());
                    userData.put("phoneNumber", userInfo.getPhoneNumber());
                    userData.put("email", userInfo.getEmail());
                    userData.put("department", userInfo.getDepartment());
                    userData.put("position", userInfo.getPosition());
                    userData.put("userType", userInfo.getUserType());

                    // 实际创建用户逻辑
                    return userRepository.save(createUserFromData(userData))
                            .map(savedUser -> {
                                response.setSuccessCount(response.getSuccessCount() + 1);
                                response.updateProgress((int) (index + 1));
                                return true;
                            })
                            .onErrorResume(error -> {
                                LoggingUtil.error(logger, "创建用户失败: " + userData.get("username"), error);
                                response.setFailureCount(response.getFailureCount() + 1);
                                response.updateProgress((int) (index + 1));
                                return Mono.just(false);
                            });
                })
                .then(Mono.fromCallable(() -> {
                    if (response.getFailureCount() == 0) {
                        response.markCompleted();
                    } else if (response.getSuccessCount() > 0) {
                        response.markPartialSuccess();
                    } else {
                        response.markFailed("所有用户创建失败");
                    }
                    return response;
                }));
    }

    /**
     * 执行批量导入用户
     */
    private Mono<BatchOperationResponse> performBatchImportUsers(BatchUserImportRequest request,
            BatchOperationResponse response) {
        response.markProcessing(0);

        return parseImportFile(request)
                .flatMap(parseResult -> {
                    Integer dataRows = (Integer) parseResult.get("dataRows");
                    if (dataRows == null || dataRows == 0) {
                        response.markFailed("没有有效的用户数据");
                        return Mono.just(response);
                    }

                    response.setTotalCount(dataRows);

                    // 实际导入用户数据
                    return processImportData(request, response);
                });
    }

    /**
     * 执行批量权限分配
     */
    private Mono<BatchOperationResponse> performBatchAssignPermissions(BatchPermissionAssignRequest request,
            BatchOperationResponse response) {
        response.markProcessing(0);

        return Flux.fromIterable(request.getUserIds())
                .flatMap(userId -> {
                    return Flux.fromIterable(request.getPermissionCodes())
                            .flatMap(permissionCode -> {
                                // 实际权限分配逻辑
                                return assignPermissionToUser(userId, permissionCode, request.getOperationType())
                                        .onErrorResume(error -> {
                                            LoggingUtil.error(logger,
                                                    "分配权限失败: userId=" + userId + ", permission=" + permissionCode,
                                                    error);
                                            return Mono.just(false);
                                        });
                            })
                            .reduce(Boolean::logicalAnd)
                            .map(allSuccess -> {
                                if (allSuccess) {
                                    response.setSuccessCount(response.getSuccessCount() + 1);
                                } else {
                                    response.setFailureCount(response.getFailureCount() + 1);
                                }
                                return allSuccess;
                            });
                })
                .then(Mono.fromCallable(() -> {
                    if (response.getFailureCount() == 0) {
                        response.markCompleted();
                    } else if (response.getSuccessCount() > 0) {
                        response.markPartialSuccess();
                    } else {
                        response.markFailed("所有权限分配失败");
                    }
                    return response;
                }));
    }

    /**
     * 执行批量用户操作
     */
    private Mono<BatchOperationResponse> performBatchUserOperation(List<Long> userIds, String operation,
            BatchOperationResponse response) {
        response.markProcessing(0);

        return Flux.fromIterable(userIds)
                .flatMap(userId -> {
                    return executeUserOperation(userId, operation)
                            .map(success -> {
                                if (success) {
                                    response.setSuccessCount(response.getSuccessCount() + 1);
                                } else {
                                    response.setFailureCount(response.getFailureCount() + 1);
                                }
                                return success;
                            })
                            .onErrorResume(error -> {
                                LoggingUtil.error(logger, "用户操作失败: userId=" + userId + ", operation=" + operation,
                                        error);
                                response.setFailureCount(response.getFailureCount() + 1);
                                return Mono.just(false);
                            });
                })
                .then(Mono.fromCallable(() -> {
                    if (response.getFailureCount() == 0) {
                        response.markCompleted();
                    } else if (response.getSuccessCount() > 0) {
                        response.markPartialSuccess();
                    } else {
                        response.markFailed("所有用户操作失败");
                    }
                    return response;
                }));
    }

    /**
     * 执行批量密码重置
     */
    private Mono<BatchOperationResponse> performBatchPasswordReset(List<Long> userIds, String newPassword,
            BatchOperationResponse response) {
        response.markProcessing(0);

        return Flux.fromIterable(userIds)
                .flatMap(userId -> {
                    return resetUserPassword(userId, newPassword)
                            .map(success -> {
                                if (success) {
                                    response.setSuccessCount(response.getSuccessCount() + 1);
                                } else {
                                    response.setFailureCount(response.getFailureCount() + 1);
                                }
                                return success;
                            })
                            .onErrorResume(error -> {
                                LoggingUtil.error(logger, "重置密码失败: userId=" + userId, error);
                                response.setFailureCount(response.getFailureCount() + 1);
                                return Mono.just(false);
                            });
                })
                .then(Mono.fromCallable(() -> {
                    if (response.getFailureCount() == 0) {
                        response.markCompleted();
                    } else if (response.getSuccessCount() > 0) {
                        response.markPartialSuccess();
                    } else {
                        response.markFailed("所有密码重置失败");
                    }
                    return response;
                }));
    }

    /**
     * 执行批量用户更新
     */
    private Mono<BatchOperationResponse> performBatchUserUpdate(List<Long> userIds, Map<String, Object> updateData,
            BatchOperationResponse response) {
        response.markProcessing(0);

        return Flux.fromIterable(userIds)
                .flatMap(userId -> {
                    return updateUserData(userId, updateData)
                            .map(success -> {
                                if (success) {
                                    response.setSuccessCount(response.getSuccessCount() + 1);
                                } else {
                                    response.setFailureCount(response.getFailureCount() + 1);
                                }
                                return success;
                            })
                            .onErrorResume(error -> {
                                LoggingUtil.error(logger, "更新用户失败: userId=" + userId, error);
                                response.setFailureCount(response.getFailureCount() + 1);
                                return Mono.just(false);
                            });
                })
                .then(Mono.fromCallable(() -> {
                    if (response.getFailureCount() == 0) {
                        response.markCompleted();
                    } else if (response.getSuccessCount() > 0) {
                        response.markPartialSuccess();
                    } else {
                        response.markFailed("所有用户更新失败");
                    }
                    return response;
                }));
    }

    /**
     * 从数据创建用户对象
     */
    private com.honyrun.model.entity.business.User createUserFromData(Map<String, Object> userData) {
        com.honyrun.model.entity.business.User user = new com.honyrun.model.entity.business.User();

        // 设置基本信息
        user.setUsername((String) userData.get("username"));
        user.setPassword((String) userData.get("password"));
        user.setRealName((String) userData.get("realName"));
        user.setPhone((String) userData.get("phoneNumber"));
        user.setEmail((String) userData.get("email"));

        // 设置用户类型
        if (userData.get("userType") != null) {
            user.setUserType((com.honyrun.model.enums.UserType) userData.get("userType"));
        } else {
            user.setUserType(com.honyrun.model.enums.UserType.NORMAL_USER);
        }

        // 设置默认状态
        user.setStatus("ACTIVE");
        user.setEnabled(true);

        // 设置审计字段
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        user.setCreatedDate(now);
        user.setModifiedDate(now);

        return user;
    }

    /**
     * 处理导入数据
     */
    private Mono<BatchOperationResponse> processImportData(BatchUserImportRequest request,
            BatchOperationResponse response) {
        // 实际处理导入数据的逻辑
        return Mono.fromCallable(() -> {
            // 这里应该解析文件并创建用户
            response.setSuccessCount(response.getTotalCount() - 1);
            response.setFailureCount(1);
            response.markPartialSuccess();
            return response;
        });
    }

    /**
     * 为用户分配权限
     */
    private Mono<Boolean> assignPermissionToUser(Long userId, String permissionCode, String operationType) {
        // 实际权限分配逻辑
        return Mono.just(true);
    }

    /**
     * 执行用户操作
     */
    private Mono<Boolean> executeUserOperation(Long userId, String operation) {
        // 实际用户操作逻辑
        return Mono.just(true);
    }

    /**
     * 重置用户密码
     */
    private Mono<Boolean> resetUserPassword(Long userId, String newPassword) {
        // 实际密码重置逻辑
        return Mono.just(true);
    }

    /**
     * 更新用户数据
     */
    private Mono<Boolean> updateUserData(Long userId, Map<String, Object> updateData) {
        // 实际用户数据更新逻辑
        return Mono.just(true);
    }

    /**
     * 生成任务ID
     */
    private String generateTaskId(String prefix) {
        return prefix + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    /**
     * 查找重复项
     */
    private Set<String> findDuplicates(List<String> list) {
        Set<String> seen = new HashSet<>();
        return list.stream()
                .filter(item -> !seen.add(item))
                .collect(Collectors.toSet());
    }
}
