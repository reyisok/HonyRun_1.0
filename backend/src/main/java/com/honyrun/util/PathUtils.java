package com.honyrun.util;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.model.enums.PathType;

import reactor.core.publisher.Mono;

/**
 * 路径管理工具类
 * 提供统一的路径计算、解析和操作功能
 * 所有路径计算都以backend目录为项目根目录
 *
 * 修复内容：
 * 1. 将String类型的路径操作改为Path对象操作
 * 2. 添加PathType枚举支持
 * 3. 提供响应式和非响应式两种API
 * 4. 增强路径验证和错误处理
 *
 * @author Mr.Rey Copyright © 2025
 * @version 1.1.0
 * @created 2025-10-26 01:36:12
 * @modified 2025-10-26 01:36:12
 */
@Component
public class PathUtils {

    private static final Logger logger = LoggerFactory.getLogger(PathUtils.class);

    private final UnifiedConfigManager configurationManager;

    // 项目根目录缓存
    private Path projectRoot;

    /**
     * 构造函数注入 - 符合Spring Boot 3最佳实践
     *
     * @param configurationManager 统一配置管理器
     */
    public PathUtils(UnifiedConfigManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    /**
     * 获取项目根目录（backend目录）
     * 查找逻辑：从当前运行目录向上查找，直到找到包含src目录的父目录
     *
     * @return 项目根目录的Path对象
     */
    public Path getProjectRoot() {
        if (projectRoot == null) {
            // 初始化项目根目录
            projectRoot = findProjectRoot();
        }
        return projectRoot;
    }

    /**
     * 获取项目根目录的字符串表示
     *
     * @deprecated 建议使用 {@link #getProjectRoot()} 返回Path对象
     * @return 项目根目录的绝对路径字符串
     */
    @Deprecated
    public String getProjectRootString() {
        return getProjectRoot().toString();
    }

    /**
     * 根据PathType获取路径
     *
     * @param pathType 路径类型枚举
     * @return 对应的Path对象
     */
    public Path getPath(PathType pathType) {
        Objects.requireNonNull(pathType, "PathType不能为空");

        try {
            // 直接使用默认路径，避免阻塞调用
            String relativePath = pathType.getDefaultPath();
            Path fullPath = getProjectRoot().resolve(relativePath);

            LoggingUtil.debug(logger, "获取路径: {} -> {}", pathType.getConfigKey(), fullPath);
            return fullPath;
        } catch (Exception e) {
            LoggingUtil.warn(logger, "获取路径配置失败，使用默认路径: {}", pathType.getConfigKey(), e);
            return getProjectRoot().resolve(pathType.getDefaultPath());
        }
    }

    /**
     * 响应式获取路径
     *
     * @param pathType 路径类型枚举
     * @return 包含Path对象的Mono
     */
    public Mono<Path> getPathReactive(PathType pathType) {
        Objects.requireNonNull(pathType, "PathType不能为空");

        return Mono.fromCallable(() -> pathType.getDefaultPath())
                .map(relativePath -> getProjectRoot().resolve(relativePath))
                .doOnNext(path -> LoggingUtil.debug(logger, "响应式获取路径: {} -> {}", pathType.getConfigKey(), path))
                .doOnError(error -> LoggingUtil.error(logger, "响应式获取路径失败: {}", pathType.getConfigKey(), error))
                .onErrorReturn(getProjectRoot().resolve(pathType.getDefaultPath()));
    }

    /**
     * 根据配置获取src基础目录
     *
     * @deprecated 建议使用 {@link #getPath(PathType)} 配合 {@link PathType#SRC_BASE}
     * @return src目录的Path对象
     */
    @Deprecated
    public Path getSrcBaseDir() {
        return getPath(PathType.SRC_BASE);
    }

    /**
     * 根据配置获取日志目录
     *
     * @deprecated 建议使用 {@link #getPath(PathType)} 配合 {@link PathType#LOGS_DIR}
     * @return 日志目录的Path对象
     */
    @Deprecated
    public Path getLogsDir() {
        return getPath(PathType.LOGS_DIR);
    }

    /**
     * 根据配置获取临时目录
     *
     * @deprecated 建议使用 {@link #getPath(PathType)} 配合 {@link PathType#TEMP_DIR}
     * @return 临时目录的Path对象
     */
    @Deprecated
    public Path getTempDir() {
        return getPath(PathType.TEMP_DIR);
    }

    /**
     * 根据配置获取监控目录
     *
     * @deprecated 建议使用 {@link #getPath(PathType)} 配合 {@link PathType#MONITORING_DIR}
     * @return 监控目录的Path对象
     */
    @Deprecated
    public Path getMonitoringDir() {
        return getPath(PathType.MONITORING_DIR);
    }

    /**
     * 根据配置获取报告目录
     *
     * @deprecated 建议使用 {@link #getPath(PathType)} 配合 {@link PathType#REPORTS_DIR}
     * @return 报告目录的Path对象
     */
    @Deprecated
    public Path getReportsDir() {
        return getPath(PathType.REPORTS_DIR);
    }

    /**
     * 获取相对路径的完整路径
     *
     * @deprecated 建议使用 {@link #resolvePath(String)} 返回Path对象
     * @param relativePath 相对路径
     * @return 完整路径字符串
     */
    @Deprecated
    public String getPath(String relativePath) {
        return resolvePath(relativePath).toString();
    }

    /**
     * 解析相对路径为完整Path对象
     *
     * @param relativePath 相对路径
     * @return 完整的Path对象
     */
    public Path resolvePath(String relativePath) {
        Objects.requireNonNull(relativePath, "相对路径不能为空");
        return getProjectRoot().resolve(relativePath);
    }

    /**
     * 确保目录存在，如果不存在则创建
     *
     * @param directoryPath 目录路径（Path对象）
     * @return 创建或已存在的目录Path对象
     * @throws RuntimeException 如果创建目录失败
     */
    public Path ensureDirectoryExists(Path directoryPath) {
        Objects.requireNonNull(directoryPath, "目录路径不能为空");

        try {
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
                LoggingUtil.info(logger, "创建目录: {}", directoryPath);
            } else {
                LoggingUtil.debug(logger, "目录已存在: {}", directoryPath);
            }
            return directoryPath;
        } catch (Exception e) {
            LoggingUtil.error(logger, "创建目录失败: {}", directoryPath, e);
            throw new RuntimeException("创建目录失败: " + directoryPath, e);
        }
    }

    /**
     * 确保目录存在（兼容性方法）
     *
     * @deprecated 建议使用 {@link #ensureDirectoryExists(Path)}
     * @param directoryPath 目录路径字符串
     * @return 创建或已存在的目录Path对象
     */
    @Deprecated
    public Path ensureDirectoryExists(String directoryPath) {
        return ensureDirectoryExists(Paths.get(directoryPath));
    }

    /**
     * 检查路径是否存在
     *
     * @param path 要检查的路径（Path对象）
     * @return 如果路径存在返回true
     */
    public boolean exists(Path path) {
        Objects.requireNonNull(path, "路径不能为空");
        boolean exists = Files.exists(path);
        LoggingUtil.debug(logger, "路径存在性检查: {} -> {}", path, exists);
        return exists;
    }

    /**
     * 检查路径是否存在（兼容性方法）
     *
     * @deprecated 建议使用 {@link #exists(Path)}
     * @param path 要检查的路径字符串
     * @return 如果路径存在返回true
     */
    @Deprecated
    public boolean exists(String path) {
        return exists(Paths.get(path));
    }

    /**
     * 查找项目根目录
     * 从当前工作目录开始向上查找，直到找到包含src目录的目录
     *
     * @return 项目根目录的Path对象
     */
    private Path findProjectRoot() {
        LoggingUtil.debug(logger, "查找项目根目录");

        // 从当前工作目录开始查找
        Path currentDir = Paths.get(System.getProperty("user.dir"));

        // 向上查找，直到找到包含src目录的目录
        Path parent = currentDir;
        while (parent != null) {
            Path srcDir = parent.resolve("src");
            if (Files.exists(srcDir) && Files.isDirectory(srcDir)) {
                LoggingUtil.info(logger, "项目根目录找到: {}", parent);
                return parent;
            }
            parent = parent.getParent();
        }

        // 如果找不到，使用当前目录作为回退方案
        LoggingUtil.warn(logger, "项目根目录未找到，使用当前工作目录作为回退方案");
        return currentDir;
    }

    /**
     * 构建响应式的路径获取方法（兼容性方法）
     *
     * @deprecated 建议使用 {@link #getPathReactive(PathType)}
     * @param pathKey 路径配置键名
     * @return 包含路径字符串的Mono
     */
    @Deprecated
    public Mono<String> getPathReactive(String pathKey) {
        return Mono.fromCallable(() -> "")
                .map(relativePath -> getProjectRoot().resolve(relativePath).toString())
                .doOnError(error -> LoggingUtil.error(logger, "获取路径失败，键: {}", pathKey, error))
                .onErrorReturn(getProjectRoot().toString());
    }

    /**
     * 验证路径是否安全（防止路径遍历攻击）
     *
     * @param path 要验证的路径
     * @return 如果路径安全返回true
     */
    public boolean isPathSafe(Path path) {
        Objects.requireNonNull(path, "路径不能为空");

        try {
            Path normalizedPath = path.normalize();
            Path projectRoot = getProjectRoot();

            // 检查路径是否在项目根目录内
            boolean isSafe = normalizedPath.startsWith(projectRoot);

            if (!isSafe) {
                LoggingUtil.warn(logger, "检测到不安全路径: {} 不在项目根目录 {} 内", normalizedPath, projectRoot);
            }

            return isSafe;
        } catch (Exception e) {
            LoggingUtil.error(logger, "路径安全性验证失败: {}", path, e);
            return false;
        }
    }

    /**
     * 获取两个路径之间的相对路径
     *
     * @param from 起始路径
     * @param to   目标路径
     * @return 相对路径
     */
    public Path getRelativePath(Path from, Path to) {
        Objects.requireNonNull(from, "起始路径不能为空");
        Objects.requireNonNull(to, "目标路径不能为空");

        try {
            return from.relativize(to);
        } catch (Exception e) {
            LoggingUtil.error(logger, "计算相对路径失败: {} -> {}", from, to, e);
            throw new RuntimeException("计算相对路径失败", e);
        }
    }
}
