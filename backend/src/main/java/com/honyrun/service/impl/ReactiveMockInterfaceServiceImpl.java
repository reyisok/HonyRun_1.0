package com.honyrun.service.impl;

import com.honyrun.model.entity.system.MockInterface;
import com.honyrun.service.reactive.ReactiveMockInterfaceService;
import com.honyrun.util.LoggingUtil;
import com.honyrun.exception.BusinessException;
import com.honyrun.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 响应式模拟接口服务实现类
 *
 * 实现ReactiveMockInterfaceService接口，提供模拟接口管理相关的业务逻辑
 * 包含模拟接口CRUD操作、动态路由处理、响应生成等功能的具体实现
 * 所有方法返回Mono或Flux类型，支持响应式数据流处理
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  21:00:00
 * @modified 2025-07-01 21:00:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Service
@Transactional
public class ReactiveMockInterfaceServiceImpl implements ReactiveMockInterfaceService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveMockInterfaceServiceImpl.class);

    private final DatabaseClient databaseClient;

    /**
     * 构造函数注入
     *
     * @param databaseClient 数据库客户端
     */
    public ReactiveMockInterfaceServiceImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    /**
     * 创建模拟接口
     * 创建一个新的模拟接口配置
     *
     * @param mockInterface 模拟接口实体
     * @return 创建的模拟接口
     */
    @Override
    public Mono<MockInterface> createMockInterface(MockInterface mockInterface) {
        LoggingUtil.info(logger, "开始创建模拟接口: {}", mockInterface.getInterfaceName());

        return validateMockInterface(mockInterface)
            .then(validatePathUniqueness(mockInterface.getInterfacePath(), mockInterface.getRequestMethod(), null))
            .flatMap(isUnique -> {
                if (!isUnique) {
                    return Mono.error(new ValidationException("接口路径和方法组合已存在"));
                }

                // 设置默认值
                if (mockInterface.getEnabled() == null) {
                    mockInterface.setEnabled(1);
                }
                if (mockInterface.getResponseStatus() == null) {
                    mockInterface.setResponseStatus(200);
                }
                if (mockInterface.getDelayTime() == null) {
                    mockInterface.setDelayTime(0L);
                }
                if (mockInterface.getAccessCount() == null) {
                    mockInterface.setAccessCount(0L);
                }
                if (mockInterface.getSuccessCount() == null) {
                    mockInterface.setSuccessCount(0L);
                }
                if (mockInterface.getFailureCount() == null) {
                    mockInterface.setFailureCount(0L);
                }
                if (mockInterface.getLogEnabled() == null) {
                    mockInterface.setLogEnabled(1);
                }

                mockInterface.setCreatedDate(LocalDateTime.now());
                mockInterface.setLastModifiedDate(LocalDateTime.now());

                return saveMockInterface(mockInterface);
            })
            .doOnSuccess(created -> LoggingUtil.info(logger, "模拟接口创建成功: ID={}", created.getId()))
            .doOnError(error -> LoggingUtil.error(logger, "创建模拟接口失败: {}", error.getMessage()));
    }

    /**
     * 更新模拟接口
     * 更新现有的模拟接口配置
     *
     * @param id 接口ID
     * @param mockInterface 更新的模拟接口实体
     * @return 更新后的模拟接口
     */
    @Override
    public Mono<MockInterface> updateMockInterface(Long id, MockInterface mockInterface) {
        LoggingUtil.info(logger, "开始更新模拟接口: ID={}", id);

        return getMockInterfaceById(id)
            .switchIfEmpty(Mono.error(new BusinessException("模拟接口不存在")))
            .flatMap(existing -> {
                return validateMockInterface(mockInterface)
                    .then(validatePathUniqueness(mockInterface.getInterfacePath(), mockInterface.getRequestMethod(), id))
                    .flatMap(isUnique -> {
                        if (!isUnique) {
                            return Mono.error(new ValidationException("接口路径和方法组合已存在"));
                        }

                        // 更新字段
                        existing.setInterfaceName(mockInterface.getInterfaceName());
                        existing.setInterfacePath(mockInterface.getInterfacePath());
                        existing.setRequestMethod(mockInterface.getRequestMethod());
                        existing.setDescription(mockInterface.getDescription());
                        existing.setRequestTemplate(mockInterface.getRequestTemplate());
                        existing.setResponseTemplate(mockInterface.getResponseTemplate());
                        existing.setResponseStatus(mockInterface.getResponseStatus());
                        existing.setResponseHeaders(mockInterface.getResponseHeaders());
                        existing.setDelayTime(mockInterface.getDelayTime());
                        existing.setEnabled(mockInterface.getEnabled());
                        existing.setStatus(mockInterface.getStatus());
                        existing.setCategory(mockInterface.getCategory());
                        existing.setTags(mockInterface.getTags());
                        existing.setLogEnabled(mockInterface.getLogEnabled());
                        existing.setLastModifiedDate(LocalDateTime.now());

                        return saveMockInterface(existing);
                    });
            })
            .doOnSuccess(updated -> LoggingUtil.info(logger, "模拟接口更新成功: ID={}", updated.getId()))
            .doOnError(error -> LoggingUtil.error(logger, "更新模拟接口失败: ID={}, 错误: {}", id, error.getMessage()));
    }

    /**
     * 删除模拟接口
     * 删除指定的模拟接口
     *
     * @param id 接口ID
     * @return 删除操作结果
     */
    @Override
    public Mono<Void> deleteMockInterface(Long id) {
        LoggingUtil.info(logger, "开始删除模拟接口: ID={}", id);

        return getMockInterfaceById(id)
            .switchIfEmpty(Mono.error(new BusinessException("模拟接口不存在")))
            .flatMap(existing -> {
                String sql = "DELETE FROM sys_mock_interface WHERE id = :id";
                return databaseClient.sql(sql)
                    .bind("id", id)
                    .fetch()
                    .rowsUpdated()
                    .then();
            })
            .doOnSuccess(result -> LoggingUtil.info(logger, "模拟接口删除成功: ID={}", id))
            .doOnError(error -> LoggingUtil.error(logger, "删除模拟接口失败: ID={}, 错误: {}", id, error.getMessage()));
    }

    /**
     * 根据ID获取模拟接口
     * 获取指定ID的模拟接口详情
     *
     * @param id 接口ID
     * @return 模拟接口实体
     */
    @Override
    public Mono<MockInterface> getMockInterfaceById(Long id) {
        LoggingUtil.debug(logger, "获取模拟接口详情: ID={}", id);

        String sql = "SELECT * FROM sys_mock_interface WHERE id = :id";
        return databaseClient.sql(sql)
            .bind("id", id)
            .map((row, metadata) -> mapRowToMockInterface(row))
            .one()
            .doOnSuccess(result -> LoggingUtil.debug(logger, "模拟接口详情获取成功: ID={}", id))
            .doOnError(error -> LoggingUtil.error(logger, "获取模拟接口详情失败: ID={}, 错误: {}", id, error.getMessage()));
    }

    /**
     * 根据路径和方法获取模拟接口
     * 根据接口路径和HTTP方法查找模拟接口
     *
     * @param path 接口路径
     * @param method HTTP方法
     * @return 模拟接口实体
     */
    @Override
    public Mono<MockInterface> getMockInterfaceByPathAndMethod(String path, String method) {
        LoggingUtil.debug(logger, "根据路径和方法获取模拟接口: {} {}", method, path);

        String sql = "SELECT * FROM sys_mock_interface WHERE interface_path = :path AND request_method = :method";
        return databaseClient.sql(sql)
            .bind("path", path)
            .bind("method", method)
            .map((row, metadata) -> mapRowToMockInterface(row))
            .one()
            .doOnSuccess(result -> LoggingUtil.debug(logger, "模拟接口获取成功: {} {}", method, path))
            .doOnError(error -> LoggingUtil.debug(logger, "未找到匹配的模拟接口: {} {}", method, path));
    }

    /**
     * 获取所有模拟接口
     * 获取系统中所有的模拟接口列表
     *
     * @return 模拟接口流
     */
    @Override
    public Flux<MockInterface> getAllMockInterfaces() {
        LoggingUtil.info(logger, "获取所有模拟接口列表");

        String sql = "SELECT * FROM sys_mock_interface ORDER BY created_at DESC";
        return databaseClient.sql(sql)
            .map((row, metadata) -> mapRowToMockInterface(row))
            .all()
            .doOnComplete(() -> LoggingUtil.info(logger, "模拟接口列表获取完成"));
    }

    /**
     * 根据分类获取模拟接口
     * 获取指定分类下的所有模拟接口
     *
     * @param category 接口分类
     * @return 模拟接口流
     */
    @Override
    public Flux<MockInterface> getMockInterfacesByCategory(String category) {
        LoggingUtil.info(logger, "根据分类获取模拟接口: {}", category);

        String sql = "SELECT * FROM sys_mock_interface WHERE category = :category ORDER BY created_at DESC";
        return databaseClient.sql(sql)
            .bind("category", category)
            .map((row, metadata) -> mapRowToMockInterface(row))
            .all()
            .doOnComplete(() -> LoggingUtil.info(logger, "分类模拟接口列表获取完成: {}", category));
    }

    /**
     * 获取启用的模拟接口
     * 获取所有启用状态的模拟接口
     *
     * @return 启用的模拟接口流
     */
    @Override
    public Flux<MockInterface> getEnabledMockInterfaces() {
        LoggingUtil.info(logger, "获取启用的模拟接口列表");

        String sql = "SELECT * FROM sys_mock_interface WHERE enabled = 1 ORDER BY created_at DESC";
        return databaseClient.sql(sql)
            .map((row, metadata) -> mapRowToMockInterface(row))
            .all()
            .doOnComplete(() -> LoggingUtil.info(logger, "启用模拟接口列表获取完成"));
    }

    /**
     * 启用模拟接口
     * 启用指定的模拟接口
     *
     * @param id 接口ID
     * @return 操作结果
     */
    @Override
    public Mono<MockInterface> enableMockInterface(Long id) {
        LoggingUtil.info(logger, "启用模拟接口: ID={}", id);

        return updateMockInterfaceStatus(id, 1)
            .doOnSuccess(result -> LoggingUtil.info(logger, "模拟接口启用成功: ID={}", id))
            .doOnError(error -> LoggingUtil.error(logger, "启用模拟接口失败: ID={}, 错误: {}", id, error.getMessage()));
    }

    /**
     * 禁用模拟接口
     * 禁用指定的模拟接口
     *
     * @param id 接口ID
     * @return 操作结果
     */
    @Override
    public Mono<MockInterface> disableMockInterface(Long id) {
        LoggingUtil.info(logger, "禁用模拟接口: ID={}", id);

        return updateMockInterfaceStatus(id, 0)
            .doOnSuccess(result -> LoggingUtil.info(logger, "模拟接口禁用成功: ID={}", id))
            .doOnError(error -> LoggingUtil.error(logger, "禁用模拟接口失败: ID={}, 错误: {}", id, error.getMessage()));
    }

    /**
     * 处理模拟接口请求
     * 根据配置处理模拟接口的请求并生成响应
     *
     * @param mockInterface 模拟接口配置
     * @param request 服务器请求
     * @return 服务器响应
     */
    @Override
    public Mono<ServerResponse> handleMockRequest(MockInterface mockInterface, ServerRequest request) {
        LoggingUtil.info(logger, "处理模拟接口请求: {}", mockInterface.getInterfaceName());

        return Mono.delay(java.time.Duration.ofMillis(mockInterface.getDelayTime() != null ? mockInterface.getDelayTime() : 0))
            .then(generateMockResponse(mockInterface, null))
            .flatMap(responseBody -> {
                ServerResponse.BodyBuilder builder = ServerResponse.status(mockInterface.getResponseStatus() != null ? mockInterface.getResponseStatus() : 200);

                // 添加响应头
                if (StringUtils.hasText(mockInterface.getResponseHeaders())) {
                    try {
                        // 简单解析响应头（实际项目中应使用JSON解析）
                        builder.header("Content-Type", "application/json");
                    } catch (Exception e) {
                        LoggingUtil.warn(logger, "解析响应头失败: {}", e.getMessage());
                        builder.header("Content-Type", "application/json");
                    }
                } else {
                    builder.header("Content-Type", "application/json");
                }

                return builder.bodyValue(responseBody);
            })
            .doOnSuccess(response -> LoggingUtil.info(logger, "模拟接口请求处理成功: {}", mockInterface.getInterfaceName()))
            .doOnError(error -> LoggingUtil.error(logger, "处理模拟接口请求失败: {}, 错误: {}", mockInterface.getInterfaceName(), error.getMessage()));
    }

    /**
     * 生成模拟响应
     * 根据模拟接口配置生成响应内容
     *
     * @param mockInterface 模拟接口配置
     * @param requestParams 请求参数
     * @return 响应内容
     */
    @Override
    public Mono<String> generateMockResponse(MockInterface mockInterface, Map<String, Object> requestParams) {
        LoggingUtil.debug(logger, "生成模拟响应: {}", mockInterface.getInterfaceName());

        String responseTemplate = mockInterface.getResponseTemplate();
        if (!StringUtils.hasText(responseTemplate)) {
            responseTemplate = "{\"message\": \"Mock response\", \"timestamp\": \"" + LocalDateTime.now() + "\"}";
        }

        // 简单的模板替换（实际项目中应使用模板引擎）
        String response = responseTemplate;
        if (requestParams != null) {
            for (Map.Entry<String, Object> entry : requestParams.entrySet()) {
                response = response.replace("${" + entry.getKey() + "}", String.valueOf(entry.getValue()));
            }
        }

        return Mono.just(response)
            .doOnSuccess(result -> LoggingUtil.debug(logger, "模拟响应生成成功: {}", mockInterface.getInterfaceName()));
    }

    /**
     * 更新接口访问统计
     * 更新模拟接口的访问次数和统计信息
     *
     * @param id 接口ID
     * @param success 是否成功访问
     * @param responseTime 响应时间（毫秒）
     * @return 更新操作结果
     */
    @Override
    public Mono<Void> updateAccessStatistics(Long id, boolean success, long responseTime) {
        LoggingUtil.debug(logger, "更新接口访问统计: ID={}, 成功={}, 响应时间={}ms", id, success, responseTime);

        String sql = success ?
            "UPDATE sys_mock_interface SET access_count = access_count + 1, success_count = success_count + 1, last_modified_date = :lastModifiedDate WHERE id = :id" :
            "UPDATE sys_mock_interface SET access_count = access_count + 1, failure_count = failure_count + 1, last_modified_date = :lastModifiedDate WHERE id = :id";

        return databaseClient.sql(sql)
            .bind("id", id)
            .bind("lastModifiedDate", LocalDateTime.now())
            .fetch()
            .rowsUpdated()
            .then()
            .doOnSuccess(result -> LoggingUtil.debug(logger, "接口访问统计更新成功: ID={}", id))
            .doOnError(error -> LoggingUtil.warn(logger, "更新接口访问统计失败: ID={}, 错误: {}", id, error.getMessage()));
    }

    /**
     * 获取接口访问统计
     * 获取指定模拟接口的访问统计信息
     *
     * @param id 接口ID
     * @return 统计信息
     */
    @Override
    public Mono<MockInterfaceStatistics> getAccessStatistics(Long id) {
        LoggingUtil.debug(logger, "获取接口访问统计: ID={}", id);

        return getMockInterfaceById(id)
            .map(mockInterface -> {
                MockInterfaceStatistics stats = new MockInterfaceStatistics(
                    mockInterface.getId(),
                    mockInterface.getInterfaceName(),
                    mockInterface.getInterfacePath()
                );

                stats.setTotalAccess(mockInterface.getAccessCount() != null ? mockInterface.getAccessCount() : 0L);
                stats.setSuccessCount(mockInterface.getSuccessCount() != null ? mockInterface.getSuccessCount() : 0L);
                stats.setFailureCount(mockInterface.getFailureCount() != null ? mockInterface.getFailureCount() : 0L);

                if (stats.getTotalAccess() > 0) {
                    stats.setSuccessRate((double) stats.getSuccessCount() / stats.getTotalAccess() * 100);
                } else {
                    stats.setSuccessRate(0.0);
                }

                stats.setAvgResponseTime(mockInterface.getAvgResponseTime() != null ? mockInterface.getAvgResponseTime().longValue() : 0L);

                return stats;
            })
            .doOnSuccess(result -> LoggingUtil.debug(logger, "接口访问统计获取成功: ID={}", id))
            .doOnError(error -> LoggingUtil.error(logger, "获取接口访问统计失败: ID={}, 错误: {}", id, error.getMessage()));
    }

    /**
     * 验证接口路径唯一性
     * 检查接口路径和方法组合是否已存在
     *
     * @param path 接口路径
     * @param method HTTP方法
     * @param excludeId 排除的接口ID（用于更新时检查）
     * @return 验证结果，true表示路径唯一
     */
    @Override
    public Mono<Boolean> validatePathUniqueness(String path, String method, Long excludeId) {
        LoggingUtil.debug(logger, "验证接口路径唯一性: {} {}", method, path);

        String sql = excludeId != null ?
            "SELECT COUNT(*) FROM sys_mock_interface WHERE interface_path = :path AND request_method = :method AND id != :excludeId" :
            "SELECT COUNT(*) FROM sys_mock_interface WHERE interface_path = :path AND request_method = :method";

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql)
            .bind("path", path)
            .bind("method", method);

        if (excludeId != null) {
            spec = spec.bind("excludeId", excludeId);
        }

        return spec.map((row, metadata) -> row.get(0, Long.class))
            .one()
            .map(count -> count == 0)
            .doOnSuccess(result -> LoggingUtil.debug(logger, "接口路径唯一性验证完成: {} {} - 唯一={}", method, path, result));
    }

    /**
     * 批量导入模拟接口
     * 批量创建多个模拟接口
     *
     * @param mockInterfaces 模拟接口流
     * @return 导入结果流
     */
    @Override
    public Flux<MockInterface> batchImportMockInterfaces(Flux<MockInterface> mockInterfaces) {
        LoggingUtil.info(logger, "开始批量导入模拟接口");

        return mockInterfaces
            .flatMap(this::createMockInterface)
            .doOnComplete(() -> LoggingUtil.info(logger, "批量导入模拟接口完成"));
    }

    /**
     * 导出模拟接口配置
     * 导出指定分类或所有模拟接口的配置
     *
     * @param category 分类（可选，null表示导出所有）
     * @return 模拟接口配置流
     */
    @Override
    public Flux<MockInterface> exportMockInterfaces(String category) {
        LoggingUtil.info(logger, "导出模拟接口配置: 分类={}", category);

        if (StringUtils.hasText(category)) {
            return getMockInterfacesByCategory(category);
        } else {
            return getAllMockInterfaces();
        }
    }

    /**
     * 搜索模拟接口
     * 根据关键词搜索模拟接口
     *
     * @param keyword 搜索关键词
     * @return 匹配的模拟接口流
     */
    @Override
    public Flux<MockInterface> searchMockInterfaces(String keyword) {
        LoggingUtil.info(logger, "搜索模拟接口: 关键词={}", keyword);

        String sql = "SELECT * FROM sys_mock_interface WHERE " +
                    "interface_name LIKE :keyword OR " +
                    "interface_path LIKE :keyword OR " +
                    "description LIKE :keyword OR " +
                    "category LIKE :keyword OR " +
                    "tags LIKE :keyword " +
                    "ORDER BY created_at DESC";

        String searchPattern = "%" + keyword + "%";
        return databaseClient.sql(sql)
            .bind("keyword", searchPattern)
            .map((row, metadata) -> mapRowToMockInterface(row))
            .all()
            .doOnComplete(() -> LoggingUtil.info(logger, "模拟接口搜索完成: 关键词={}", keyword));
    }

    // 私有辅助方法

    /**
     * 验证模拟接口数据
     */
    private Mono<Void> validateMockInterface(MockInterface mockInterface) {
        if (!StringUtils.hasText(mockInterface.getInterfaceName())) {
            return Mono.error(new ValidationException("接口名称不能为空"));
        }
        if (!StringUtils.hasText(mockInterface.getInterfacePath())) {
            return Mono.error(new ValidationException("接口路径不能为空"));
        }
        if (!StringUtils.hasText(mockInterface.getRequestMethod())) {
            return Mono.error(new ValidationException("请求方法不能为空"));
        }
        return Mono.empty();
    }

    /**
     * 保存模拟接口
     */
    private Mono<MockInterface> saveMockInterface(MockInterface mockInterface) {
        if (mockInterface.getId() == null) {
            // 插入新记录
            String sql = "INSERT INTO sys_mock_interface (interface_name, interface_path, request_method, description, " +
                        "request_template, response_template, response_status, response_headers, delay_time, enabled, " +
                        "status, category, tags, access_count, success_count, failure_count, avg_response_time, " +
                        "log_enabled, created_at, last_modified_date) VALUES " +
                        "(:interfaceName, :interfacePath, :requestMethod, :description, :requestTemplate, " +
                        ":responseTemplate, :responseStatus, :responseHeaders, :delayTime, :enabled, :status, " +
                        ":category, :tags, :accessCount, :successCount, :failureCount, :avgResponseTime, " +
                        ":logEnabled, :createdDate, :lastModifiedDate)";

            return databaseClient.sql(sql)
                .bind("interfaceName", mockInterface.getInterfaceName())
                .bind("interfacePath", mockInterface.getInterfacePath())
                .bind("requestMethod", mockInterface.getRequestMethod())
                .bind("description", mockInterface.getDescription())
                .bind("requestTemplate", mockInterface.getRequestTemplate())
                .bind("responseTemplate", mockInterface.getResponseTemplate())
                .bind("responseStatus", mockInterface.getResponseStatus())
                .bind("responseHeaders", mockInterface.getResponseHeaders())
                .bind("delayTime", mockInterface.getDelayTime())
                .bind("enabled", mockInterface.getEnabled())
                .bind("status", mockInterface.getStatus())
                .bind("category", mockInterface.getCategory())
                .bind("tags", mockInterface.getTags())
                .bind("accessCount", mockInterface.getAccessCount())
                .bind("successCount", mockInterface.getSuccessCount())
                .bind("failureCount", mockInterface.getFailureCount())
                .bind("avgResponseTime", mockInterface.getAvgResponseTime())
                .bind("logEnabled", mockInterface.getLogEnabled())
                .bind("createdDate", mockInterface.getCreatedDate())
                .bind("lastModifiedDate", mockInterface.getLastModifiedDate())
                .filter((statement, executeFunction) -> statement.returnGeneratedValues("id").execute())
                .map((row, metadata) -> {
                    mockInterface.setId(row.get("id", Long.class));
                    return mockInterface;
                })
                .one();
        } else {
            // 更新现有记录
            String sql = "UPDATE sys_mock_interface SET interface_name = :interfaceName, interface_path = :interfacePath, " +
                        "request_method = :requestMethod, description = :description, request_template = :requestTemplate, " +
                        "response_template = :responseTemplate, response_status = :responseStatus, " +
                        "response_headers = :responseHeaders, delay_time = :delayTime, enabled = :enabled, " +
                        "status = :status, category = :category, tags = :tags, access_count = :accessCount, " +
                        "success_count = :successCount, failure_count = :failureCount, avg_response_time = :avgResponseTime, " +
                        "log_enabled = :logEnabled, last_modified_date = :lastModifiedDate WHERE id = :id";

            return databaseClient.sql(sql)
                .bind("id", mockInterface.getId())
                .bind("interfaceName", mockInterface.getInterfaceName())
                .bind("interfacePath", mockInterface.getInterfacePath())
                .bind("requestMethod", mockInterface.getRequestMethod())
                .bind("description", mockInterface.getDescription())
                .bind("requestTemplate", mockInterface.getRequestTemplate())
                .bind("responseTemplate", mockInterface.getResponseTemplate())
                .bind("responseStatus", mockInterface.getResponseStatus())
                .bind("responseHeaders", mockInterface.getResponseHeaders())
                .bind("delayTime", mockInterface.getDelayTime())
                .bind("enabled", mockInterface.getEnabled())
                .bind("status", mockInterface.getStatus())
                .bind("category", mockInterface.getCategory())
                .bind("tags", mockInterface.getTags())
                .bind("accessCount", mockInterface.getAccessCount())
                .bind("successCount", mockInterface.getSuccessCount())
                .bind("failureCount", mockInterface.getFailureCount())
                .bind("avgResponseTime", mockInterface.getAvgResponseTime())
                .bind("logEnabled", mockInterface.getLogEnabled())
                .bind("lastModifiedDate", mockInterface.getLastModifiedDate())
                .fetch()
                .rowsUpdated()
                .then(Mono.just(mockInterface));
        }
    }

    /**
     * 更新模拟接口状态
     */
    private Mono<MockInterface> updateMockInterfaceStatus(Long id, Integer enabled) {
        return getMockInterfaceById(id)
            .switchIfEmpty(Mono.error(new BusinessException("模拟接口不存在")))
            .flatMap(existing -> {
                existing.setEnabled(enabled);
                existing.setLastModifiedDate(LocalDateTime.now());
                return saveMockInterface(existing);
            });
    }

    /**
     * 将数据库行映射为MockInterface对象
     */
    private MockInterface mapRowToMockInterface(io.r2dbc.spi.Row row) {
        MockInterface mockInterface = new MockInterface();
        mockInterface.setId(row.get("id", Long.class));
        mockInterface.setInterfaceName(row.get("interface_name", String.class));
        mockInterface.setInterfacePath(row.get("interface_path", String.class));
        mockInterface.setRequestMethod(row.get("request_method", String.class));
        mockInterface.setDescription(row.get("description", String.class));
        mockInterface.setRequestTemplate(row.get("request_template", String.class));
        mockInterface.setResponseTemplate(row.get("response_template", String.class));
        mockInterface.setResponseStatus(row.get("response_status", Integer.class));
        mockInterface.setResponseHeaders(row.get("response_headers", String.class));
        mockInterface.setDelayTime(row.get("delay_time", Long.class));
        mockInterface.setEnabled(row.get("enabled", Integer.class));
        mockInterface.setStatus(row.get("status", String.class));
        mockInterface.setCategory(row.get("category", String.class));
        mockInterface.setTags(row.get("tags", String.class));
        mockInterface.setAccessCount(row.get("access_count", Long.class));
        mockInterface.setSuccessCount(row.get("success_count", Long.class));
        mockInterface.setFailureCount(row.get("failure_count", Long.class));
        mockInterface.setAvgResponseTime(row.get("avg_response_time", Double.class));
        mockInterface.setLogEnabled(row.get("log_enabled", Integer.class));
        mockInterface.setCreatedDate(row.get("created_at", LocalDateTime.class));
                mockInterface.setLastModifiedDate(row.get("last_modified_date", LocalDateTime.class));
        return mockInterface;
    }
}


