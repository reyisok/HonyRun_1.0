package com.honyrun.service.reactive;

import com.honyrun.model.dto.response.ApiResponse;
import com.honyrun.model.entity.system.VersionChangeLog;
import com.honyrun.model.dto.response.VersionDeprecationInfo;
import com.honyrun.model.dto.response.VersionMigrationGuide;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * API版本管理服务接口
 * 
 * 提供API版本演进策略、弃用管理、迁移指导等功能
 * 支持版本变更日志、迁移窗口管理和文档变更标记
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-01-11 16:57:03
 * @modified 2025-01-11 16:57:03
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public interface ReactiveApiVersionService {

    /**
     * 获取当前支持的API版本列表
     *
     * @return 支持的版本列表
     */
    Flux<String> getSupportedVersions();

    /**
     * 获取API版本变更日志
     *
     * @param version 版本号
     * @return 版本变更日志
     */
    Mono<VersionChangeLog> getVersionChangeLog(String version);

    /**
     * 获取所有版本变更日志
     *
     * @return 所有版本变更日志
     */
    Flux<VersionChangeLog> getAllVersionChangeLogs();

    /**
     * 获取版本弃用信息
     *
     * @param version 版本号
     * @return 版本弃用信息
     */
    Mono<VersionDeprecationInfo> getVersionDeprecationInfo(String version);

    /**
     * 获取版本迁移指南
     *
     * @param fromVersion 源版本
     * @param toVersion 目标版本
     * @return 迁移指南
     */
    Mono<VersionMigrationGuide> getVersionMigrationGuide(String fromVersion, String toVersion);

    /**
     * 检查版本兼容性
     *
     * @param clientVersion 客户端版本
     * @param serverVersion 服务端版本
     * @return 兼容性检查结果
     */
    Mono<Boolean> checkVersionCompatibility(String clientVersion, String serverVersion);

    /**
     * 标记版本为弃用状态
     *
     * @param version 版本号
     * @param deprecationMessage 弃用消息
     * @param migrationDeadline 迁移截止时间
     * @return 操作结果
     */
    Mono<ApiResponse<Void>> deprecateVersion(String version, String deprecationMessage, String migrationDeadline);

    /**
     * 获取版本使用统计
     *
     * @return 版本使用统计信息
     */
    Flux<Object> getVersionUsageStats();

    /**
     * 验证API版本格式
     *
     * @param version 版本号
     * @return 验证结果
     */
    Mono<Boolean> validateVersionFormat(String version);

    /**
     * 获取版本发布时间线
     *
     * @return 版本发布时间线
     */
    Flux<Object> getVersionTimeline();
}

