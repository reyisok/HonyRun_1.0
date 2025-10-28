package com.honyrun.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.model.enums.PathType;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.PathUtils;

import reactor.core.publisher.Mono;

/**
 * 路径服务类
 * 负责业务层面的路径管理，提供统一的路径获取和操作接口
 * 集成配置管理和路径计算功能
 *
 * @author Mr.Rey Copyright © 2025
 * @version 1.0.0
 * @created 2025-10-26 01:36:12
 */
@Service
public class PathService {

    private static final Logger logger = LoggerFactory.getLogger(PathService.class);

    private final PathUtils pathUtils;
    private final UnifiedConfigManager configurationManager;

    /**
     * 构造函数注入 - 符合Spring Boot 3最佳实践
     *
     * @param pathUtils            路径工具类
     * @param configurationManager 统一配置管理器
     */
    public PathService(PathUtils pathUtils, UnifiedConfigManager configurationManager) {
        this.pathUtils = pathUtils;
        this.configurationManager = configurationManager;
    }

    /**
     * 响应式获取可配置路径
     * 优先从配置中获取，如果配置不存在则使用默认路径
     *
     * @param pathType 路径类型
     * @return 包含路径的Mono
     */
    public Mono<Path> getConfigurablePath(PathType pathType) {
        Objects.requireNonNull(pathType, "路径类型不能为空");

        LoggingUtil.debug(logger, "获取可配置路径: {}", pathType.getConfigKey());

        // 直接使用默认路径，避免复杂的配置查找逻辑
        return Mono.fromCallable(() -> {
            Path defaultPath = Paths.get(pathUtils.getProjectRoot().toString(), pathType.getDefaultPath());
            LoggingUtil.debug(logger, "使用默认路径: {} -> {}", pathType.getConfigKey(), defaultPath);
            return defaultPath;
        })
        .doOnError(error -> LoggingUtil.error(logger, "获取路径失败: {}", pathType.getConfigKey(), error));
    }

    /**
     * 同步获取可配置路径
     * 用于不需要响应式处理的场景
     *
     * @param pathType 路径类型
     * @return 路径对象
     */
    public Path getConfigurablePathSync(PathType pathType) {
        Objects.requireNonNull(pathType, "路径类型不能为空");

        try {
            // 直接使用默认路径，避免阻塞调用
            Path defaultPath = Paths.get(pathUtils.getProjectRoot().toString(), pathType.getDefaultPath());
            LoggingUtil.debug(logger, "使用默认路径: {} -> {}", pathType.getConfigKey(), defaultPath);
            return defaultPath;
        } catch (Exception e) {
            LoggingUtil.error(logger, "同步获取路径失败，使用默认路径: {}", pathType.getConfigKey(), e);
            return Paths.get(pathUtils.getProjectRoot().toString(), pathType.getDefaultPath());
        }
    }

    /**
     * 确保路径目录存在
     * 如果目录不存在则创建
     *
     * @param pathType 路径类型
     * @return 是否成功创建或目录已存在的Mono
     */
    public Mono<Boolean> ensureDirectoryExists(PathType pathType) {
        Objects.requireNonNull(pathType, "路径类型不能为空");

        return getConfigurablePath(pathType)
                .map(path -> {
                    try {
                        Path result = pathUtils.ensureDirectoryExists(path);
                        LoggingUtil.debug(logger, "目录确保存在: {}", result);
                        return true;
                    } catch (RuntimeException e) {
                        LoggingUtil.error(logger, "目录创建失败: {}", path, e);
                        return false;
                    }
                })
                .doOnError(error -> LoggingUtil.error(logger, "确保目录存在时发生错误: {}", pathType.getConfigKey(), error));
    }

    /**
     * 检查路径是否存在
     *
     * @param pathType 路径类型
     * @return 路径是否存在的Mono
     */
    public Mono<Boolean> pathExists(PathType pathType) {
        Objects.requireNonNull(pathType, "路径类型不能为空");

        return getConfigurablePath(pathType)
                .map(path -> {
                    boolean exists = pathUtils.exists(path);
                    LoggingUtil.debug(logger, "路径存在性检查: {} -> {}", path, exists);
                    return exists;
                })
                .doOnError(error -> LoggingUtil.error(logger, "检查路径存在性时发生错误: {}", pathType.getConfigKey(), error));
    }

    /**
     * 获取相对于指定路径类型的子路径
     *
     * @param pathType 基础路径类型
     * @param subPath  子路径
     * @return 完整路径的Mono
     */
    public Mono<Path> getSubPath(PathType pathType, String subPath) {
        Objects.requireNonNull(pathType, "路径类型不能为空");
        Objects.requireNonNull(subPath, "子路径不能为空");

        return getConfigurablePath(pathType)
                .map(basePath -> {
                    Path fullPath = basePath.resolve(subPath);
                    LoggingUtil.debug(logger, "子路径解析: {} + {} -> {}", basePath, subPath, fullPath);
                    return fullPath;
                })
                .doOnError(error -> LoggingUtil.error(logger, "获取子路径失败: {} + {}", pathType.getConfigKey(), subPath,
                        error));
    }

    /**
     * 获取项目根目录路径
     *
     * @return 项目根目录路径的Mono
     */
    public Mono<Path> getProjectRoot() {
        return Mono.fromCallable(() -> pathUtils.getProjectRoot())
                .doOnNext(path -> LoggingUtil.debug(logger, "项目根目录: {}", path))
                .doOnError(error -> LoggingUtil.error(logger, "获取项目根目录失败", error));
    }

    /**
     * 批量确保多个目录存在
     *
     * @param pathTypes 路径类型数组
     * @return 所有目录是否都成功创建或已存在的Mono
     */
    public Mono<Boolean> ensureMultipleDirectoriesExist(PathType... pathTypes) {
        Objects.requireNonNull(pathTypes, "路径类型数组不能为空");

        if (pathTypes.length == 0) {
            return Mono.just(true);
        }

        return Mono.fromCallable(() -> {
            boolean allSuccess = true;
            for (PathType pathType : pathTypes) {
                try {
                    // 直接使用同步方式创建目录，避免阻塞调用
                    Path path = getConfigurablePathSync(pathType);
                    pathUtils.ensureDirectoryExists(path);
                    LoggingUtil.debug(logger, "目录确保存在: {}", path);
                } catch (Exception e) {
                    allSuccess = false;
                    LoggingUtil.error(logger, "批量创建目录时发生错误: {}", pathType.getConfigKey(), e);
                }
            }
            LoggingUtil.info(logger, "批量目录创建完成，成功: {}", allSuccess);
            return allSuccess;
        });
    }
}
