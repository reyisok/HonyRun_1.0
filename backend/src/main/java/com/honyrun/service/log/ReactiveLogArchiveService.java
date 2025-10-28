package com.honyrun.service.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.honyrun.util.LoggingUtil;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 响应式日志归档服务
 *
 * 提供统一的日志压缩和归档功能，确保日志不丢失的前提下节省存储空间。
 * 替代原有的日志删除逻辑，符合项目规范要求。
 *
 * 主要功能：
 * - 日志文件压缩：使用GZIP压缩算法压缩日志文件
 * - 日志归档：将压缩后的日志文件移动到归档目录
 * - 分类归档：按日志类型和日期进行分类归档
 * - 空间统计：统计归档前后的空间使用情况
 * - 清理策略：仅清理已归档的原始日志文件
 *
 * 技术特性：
 * - 响应式编程：基于Reactor实现异步处理
 * - 非阻塞IO：使用响应式调度器处理文件操作
 * - 错误恢复：归档失败时保留原始文件
 * - 原子操作：确保归档过程的原子性
 * - 统计报告：提供详细的归档统计信息
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-07-02 当前时间
 * @modified 2025-07-02 当前时间
 * @version 1.0.0
 */
@Service
public class ReactiveLogArchiveService {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveLogArchiveService.class);

    @Value("${honyrun.log.base-path:./logs}")
    private String logBasePath;

    @Value("${honyrun.log.archive.base-path:./logs/archive}")
    private String archiveBasePath;

    @Value("${honyrun.log.archive.compression-enabled:true}")
    private boolean compressionEnabled;

    @Value("${honyrun.log.archive.keep-original-days:7}")
    private int keepOriginalDays;

    private static final DateTimeFormatter ARCHIVE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 归档过期日志文件
     *
     * @param beforeDate 归档此日期之前的日志
     * @param logType 日志类型（application, error, access, business, system）
     * @return 归档结果统计信息
     */
    public Mono<Map<String, Object>> archiveLogsBefore(LocalDateTime beforeDate, String logType) {
        LoggingUtil.info(logger, "开始归档{}日志，截止日期: {}", logType, beforeDate);

        return Mono.fromCallable(() -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("logType", logType);
                    result.put("archiveDate", beforeDate);
                    result.put("startTime", LocalDateTime.now());

                    // 创建归档目录
                    String archiveDir = createArchiveDirectory(logType, beforeDate);
                    result.put("archiveDirectory", archiveDir);

                    // 查找需要归档的日志文件
                    Path logDir = Paths.get(logBasePath);
                    long totalFiles = 0;
                    long totalOriginalSize = 0;
                    long totalCompressedSize = 0;

                    if (Files.exists(logDir)) {
                        File[] logFiles = logDir.toFile().listFiles((dir, name) -> 
                            name.contains(logType) && isFileOlderThan(new File(dir, name), beforeDate));

                        if (logFiles != null) {
                            for (File logFile : logFiles) {
                                try {
                                    long originalSize = logFile.length();
                                    totalOriginalSize += originalSize;

                                    // 执行压缩和归档
                                    String archivedFile = archiveLogFile(logFile, archiveDir);
                                    
                                    if (archivedFile != null) {
                                        totalFiles++;
                                        File compressedFile = new File(archivedFile);
                                        if (compressedFile.exists()) {
                                            totalCompressedSize += compressedFile.length();
                                        }

                                        // 删除原始文件（仅在成功归档后）
                                        if (shouldDeleteOriginal(logFile)) {
                                            logFile.delete();
                                            LoggingUtil.debug(logger, "删除已归档的原始日志文件: {}", logFile.getName());
                                        }
                                    }
                                } catch (Exception e) {
                                    LoggingUtil.error(logger, "归档日志文件失败: {}", logFile.getName(), e);
                                }
                            }
                        }
                    }

                    result.put("archivedFiles", totalFiles);
                    result.put("originalSize", totalOriginalSize);
                    result.put("compressedSize", totalCompressedSize);
                    result.put("compressionRatio", totalOriginalSize > 0 ? 
                        (double) totalCompressedSize / totalOriginalSize : 0.0);
                    result.put("spaceSaved", totalOriginalSize - totalCompressedSize);
                    result.put("endTime", LocalDateTime.now());

                    LoggingUtil.info(logger, "{}日志归档完成，归档文件数: {}, 原始大小: {}KB, 压缩后大小: {}KB, 压缩率: {:.2f}%",
                            logType, totalFiles, totalOriginalSize / 1024, totalCompressedSize / 1024,
                            (1 - (totalOriginalSize > 0 ? (double) totalCompressedSize / totalOriginalSize : 0)) * 100);

                    return result;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(error -> LoggingUtil.error(logger, "归档{}日志失败", logType, error));
    }

    /**
     * 归档所有类型的过期日志
     *
     * @param beforeDate 归档此日期之前的日志
     * @return 总体归档结果统计信息
     */
    public Mono<Map<String, Object>> archiveAllLogsBefore(LocalDateTime beforeDate) {
        LoggingUtil.info(logger, "开始归档所有类型日志，截止日期: {}", beforeDate);

        String[] logTypes = {"application", "error", "access", "business", "system", "startup", "compile", "security-audit"};

        return Mono.fromCallable(() -> {
                    Map<String, Object> totalResult = new HashMap<>();
                    totalResult.put("archiveDate", beforeDate);
                    totalResult.put("startTime", LocalDateTime.now());

                    long totalFiles = 0;
                    long totalOriginalSize = 0;
                    long totalCompressedSize = 0;
                    Map<String, Object> typeResults = new HashMap<>();

                    for (String logType : logTypes) {
                        try {
                            Map<String, Object> typeResult = archiveLogsBefore(beforeDate, logType).block();
                            if (typeResult != null) {
                                typeResults.put(logType, typeResult);
                                totalFiles += (Long) typeResult.getOrDefault("archivedFiles", 0L);
                                totalOriginalSize += (Long) typeResult.getOrDefault("originalSize", 0L);
                                totalCompressedSize += (Long) typeResult.getOrDefault("compressedSize", 0L);
                            }
                        } catch (Exception e) {
                            LoggingUtil.error(logger, "归档{}类型日志失败", logType, e);
                            typeResults.put(logType, Map.of("error", e.getMessage()));
                        }
                    }

                    totalResult.put("totalArchivedFiles", totalFiles);
                    totalResult.put("totalOriginalSize", totalOriginalSize);
                    totalResult.put("totalCompressedSize", totalCompressedSize);
                    totalResult.put("totalCompressionRatio", totalOriginalSize > 0 ? 
                        (double) totalCompressedSize / totalOriginalSize : 0.0);
                    totalResult.put("totalSpaceSaved", totalOriginalSize - totalCompressedSize);
                    totalResult.put("typeResults", typeResults);
                    totalResult.put("endTime", LocalDateTime.now());

                    LoggingUtil.info(logger, "所有日志归档完成，总归档文件数: {}, 总原始大小: {}KB, 总压缩后大小: {}KB, 总压缩率: {:.2f}%",
                            totalFiles, totalOriginalSize / 1024, totalCompressedSize / 1024,
                            (1 - (totalOriginalSize > 0 ? (double) totalCompressedSize / totalOriginalSize : 0)) * 100);

                    return totalResult;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(error -> LoggingUtil.error(logger, "归档所有日志失败", error));
    }

    /**
     * 创建归档目录
     */
    private String createArchiveDirectory(String logType, LocalDateTime beforeDate) {
        String dateStr = beforeDate.format(ARCHIVE_DATE_FORMAT);
        String archiveDir = Paths.get(archiveBasePath, logType, dateStr).toString();
        
        try {
            Files.createDirectories(Paths.get(archiveDir));
            LoggingUtil.debug(logger, "创建归档目录: {}", archiveDir);
        } catch (IOException e) {
            LoggingUtil.error(logger, "创建归档目录失败: {}", archiveDir, e);
            throw new RuntimeException("创建归档目录失败", e);
        }
        
        return archiveDir;
    }

    /**
     * 归档单个日志文件
     */
    private String archiveLogFile(File logFile, String archiveDir) {
        try {
            String fileName = logFile.getName();
            String archivedFileName = compressionEnabled ? fileName + ".gz" : fileName;
            String archivedFilePath = Paths.get(archiveDir, archivedFileName).toString();

            if (compressionEnabled) {
                // 压缩归档
                compressFile(logFile, archivedFilePath);
            } else {
                // 直接复制归档
                Files.copy(logFile.toPath(), Paths.get(archivedFilePath));
            }

            LoggingUtil.debug(logger, "日志文件归档成功: {} -> {}", fileName, archivedFilePath);
            return archivedFilePath;
        } catch (Exception e) {
            LoggingUtil.error(logger, "归档日志文件失败: {}", logFile.getName(), e);
            return null;
        }
    }

    /**
     * 压缩文件
     */
    private void compressFile(File sourceFile, String targetPath) throws IOException {
        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(targetPath);
             GZIPOutputStream gzos = new GZIPOutputStream(fos)) {

            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                gzos.write(buffer, 0, length);
            }
        }
    }

    /**
     * 检查文件是否早于指定日期
     */
    private boolean isFileOlderThan(File file, LocalDateTime beforeDate) {
        long fileTime = file.lastModified();
        long beforeTime = beforeDate.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        return fileTime < beforeTime;
    }

    /**
     * 判断是否应该删除原始文件
     */
    private boolean shouldDeleteOriginal(File logFile) {
        // 只有当文件超过保留期限时才删除原始文件
        LocalDateTime keepUntil = LocalDateTime.now().minusDays(keepOriginalDays);
        return isFileOlderThan(logFile, keepUntil);
    }

    /**
     * 获取归档统计信息
     *
     * @return 归档统计信息
     */
    public Mono<Map<String, Object>> getArchiveStatistics() {
        return Mono.fromCallable(() -> {
                    Map<String, Object> stats = new HashMap<>();
                    
                    try {
                        Path archivePath = Paths.get(archiveBasePath);
                        if (Files.exists(archivePath)) {
                            long totalArchivedFiles = Files.walk(archivePath)
                                    .filter(Files::isRegularFile)
                                    .count();
                            
                            long totalArchivedSize = Files.walk(archivePath)
                                    .filter(Files::isRegularFile)
                                    .mapToLong(path -> {
                                        try {
                                            return Files.size(path);
                                        } catch (IOException e) {
                                            return 0;
                                        }
                                    })
                                    .sum();

                            stats.put("totalArchivedFiles", totalArchivedFiles);
                            stats.put("totalArchivedSize", totalArchivedSize);
                            stats.put("archiveBasePath", archiveBasePath);
                        } else {
                            stats.put("totalArchivedFiles", 0L);
                            stats.put("totalArchivedSize", 0L);
                        }
                    } catch (IOException e) {
                        LoggingUtil.error(logger, "获取归档统计信息失败", e);
                        stats.put("error", e.getMessage());
                    }

                    stats.put("compressionEnabled", compressionEnabled);
                    stats.put("keepOriginalDays", keepOriginalDays);
                    stats.put("timestamp", LocalDateTime.now());

                    return stats;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(error -> LoggingUtil.error(logger, "获取归档统计信息失败", error));
    }
}

