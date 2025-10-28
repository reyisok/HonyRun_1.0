package com.honyrun.util.version;

import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 版本变更日志工具类
 *
 * 管理版本变更记录和日志，提供版本变更历史的记录、查询和分析功能
 * 支持响应式操作，提供非阻塞的变更日志管理
 *
 * 主要功能：
 * - 版本变更记录管理
 * - 变更日志生成和查询
 * - 变更类型分类和统计
 * - 变更历史分析
 * - 变更日志导出
 *
 * 变更类型：
 * - FEATURE: 新功能添加
 * - BUGFIX: 错误修复
 * - IMPROVEMENT: 功能改进
 * - REFACTOR: 代码重构
 * - SECURITY: 安全更新
 * - BREAKING: 破坏性变更
 *
 * 响应式特性：
 * - 非阻塞操作：变更日志操作均为非阻塞
 * - 流式处理：支持变更记录的流式查询
 * - 并发安全：变更记录的并发安全管理
 * - 异步更新：支持变更日志的异步更新
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 00:40:00
 * @modified 2025-07-01 00:40:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
public class VersionChangelogUtil {

    private static final Logger logger = LoggerFactory.getLogger(VersionChangelogUtil.class);

    /**
     * 变更记录存储
     */
    private final ConcurrentHashMap<String, List<ChangelogEntry>> versionChangelogs = new ConcurrentHashMap<>();

    /**
     * 变更记录ID生成器
     */
    private final AtomicLong changelogIdGenerator = new AtomicLong(1);

    /**
     * 添加版本变更记录
     *
     * @param version 版本号
     * @param changeType 变更类型
     * @param description 变更描述
     * @param author 变更作者
     * @return 变更记录的Mono
     */
    public Mono<ChangelogEntry> addChangelogEntry(String version, ChangeType changeType, String description, String author) {
        LoggingUtil.info(logger, "添加版本变更记录: 版本={}, 类型={}, 作者={}", version, changeType, author);

        return Mono.fromCallable(() -> {
            ChangelogEntry entry = new ChangelogEntry();
            entry.setId(changelogIdGenerator.getAndIncrement());
            entry.setVersion(version);
            entry.setChangeType(changeType);
            entry.setDescription(description);
            entry.setAuthor(author);
            entry.setTimestamp(LocalDateTime.now());

            // 添加到版本变更列表
            versionChangelogs.computeIfAbsent(version, k -> new ArrayList<>()).add(entry);

            LoggingUtil.info(logger, "版本变更记录添加成功: ID={}", entry.getId());
            return entry;
        })
        .doOnError(error -> LoggingUtil.error(logger, "添加版本变更记录失败", error));
    }

    /**
     * 获取指定版本的变更日志
     *
     * @param version 版本号
     * @return 变更日志列表的Flux
     */
    public Flux<ChangelogEntry> getVersionChangelog(String version) {
        LoggingUtil.debug(logger, "获取版本变更日志: {}", version);

        return Mono.fromCallable(() -> versionChangelogs.getOrDefault(version, new ArrayList<>()))
                .flatMapMany(Flux::fromIterable)
                .doOnNext(entry -> LoggingUtil.debug(logger, "获取变更记录: ID={}, 类型={}", entry.getId(), entry.getChangeType()))
                .doOnComplete(() -> LoggingUtil.debug(logger, "版本变更日志获取完成: {}", version))
                .doOnError(error -> LoggingUtil.error(logger, "获取版本变更日志失败", error));
    }

    /**
     * 生成版本变更报告
     *
     * @param version 版本号
     * @return 变更报告的Mono
     */
    public Mono<ChangelogReport> generateChangelogReport(String version) {
        LoggingUtil.info(logger, "生成版本变更报告: {}", version);

        return getVersionChangelog(version)
                .collectList()
                .map(entries -> {
                    ChangelogReport report = new ChangelogReport();
                    report.setVersion(version);
                    report.setEntries(entries);
                    report.setTotalChanges(entries.size());

                    // 统计各类型变更数量
                    for (ChangeType type : ChangeType.values()) {
                        long count = entries.stream().filter(e -> e.getChangeType() == type).count();
                        report.getChangeTypeCounts().put(type, (int) count);
                    }

                    report.setGeneratedTime(LocalDateTime.now());

                    LoggingUtil.info(logger, "版本变更报告生成完成: 版本={}, 变更数={}", version, report.getTotalChanges());
                    return report;
                })
                .doOnError(error -> LoggingUtil.error(logger, "生成版本变更报告失败", error));
    }

    /**
     * 分页获取版本变更日志
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分页变更日志的Mono
     */
    public Mono<ChangelogPage> getChangelogPage(int page, int size) {
        LoggingUtil.debug(logger, "分页获取版本变更日志: page={}, size={}", page, size);

        return Mono.fromCallable(() -> {
            List<ChangelogEntry> allEntries = new ArrayList<>();

            // 收集所有版本的变更记录
            for (List<ChangelogEntry> entries : versionChangelogs.values()) {
                allEntries.addAll(entries);
            }

            // 按时间倒序排序
            allEntries.sort((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()));

            // 计算分页
            int totalElements = allEntries.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, totalElements);

            List<ChangelogEntry> pageEntries = startIndex < totalElements ?
                allEntries.subList(startIndex, endIndex) : new ArrayList<>();

            ChangelogPage changelogPage = new ChangelogPage();
            changelogPage.setContent(pageEntries);
            changelogPage.setPage(page);
            changelogPage.setSize(size);
            changelogPage.setTotalElements(totalElements);
            changelogPage.setTotalPages(totalPages);
            changelogPage.setFirst(page == 0);
            changelogPage.setLast(page >= totalPages - 1);

            LoggingUtil.debug(logger, "分页查询完成: 总记录数={}, 当前页={}, 页面大小={}", totalElements, page, size);
            return changelogPage;
        })
        .doOnError(error -> LoggingUtil.error(logger, "分页获取版本变更日志失败", error));
    }

    /**
     * 搜索版本变更日志
     *
     * @param keyword 搜索关键词
     * @param changeType 变更类型（可选）
     * @return 搜索结果的Flux
     */
    public Flux<ChangelogEntry> searchChangelog(String keyword, ChangeType changeType) {
        LoggingUtil.debug(logger, "搜索版本变更日志: keyword={}, changeType={}", keyword, changeType);

        return Mono.fromCallable(() -> {
            List<ChangelogEntry> allEntries = new ArrayList<>();

            // 收集所有版本的变更记录
            for (List<ChangelogEntry> entries : versionChangelogs.values()) {
                allEntries.addAll(entries);
            }

            return allEntries;
        })
        .flatMapMany(Flux::fromIterable)
        .filter(entry -> {
            // 关键词过滤
            boolean keywordMatch = keyword == null || keyword.trim().isEmpty() ||
                entry.getDescription().toLowerCase().contains(keyword.toLowerCase()) ||
                entry.getVersion().toLowerCase().contains(keyword.toLowerCase()) ||
                (entry.getAuthor() != null && entry.getAuthor().toLowerCase().contains(keyword.toLowerCase()));

            // 变更类型过滤
            boolean typeMatch = changeType == null || entry.getChangeType() == changeType;

            return keywordMatch && typeMatch;
        })
        .sort((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()))
        .doOnNext(entry -> LoggingUtil.debug(logger, "搜索到变更记录: ID={}, 版本={}, 类型={}",
            entry.getId(), entry.getVersion(), entry.getChangeType()))
        .doOnComplete(() -> LoggingUtil.debug(logger, "版本变更日志搜索完成"))
        .doOnError(error -> LoggingUtil.error(logger, "搜索版本变更日志失败", error));
    }

    // ==================== 枚举和内部类 ====================

    /**
     * 变更类型枚举
     */
    public enum ChangeType {
        FEATURE,        // 新功能
        BUGFIX,         // 错误修复
        IMPROVEMENT,    // 功能改进
        REFACTOR,       // 代码重构
        SECURITY,       // 安全更新
        BREAKING        // 破坏性变更
    }

    /**
     * 变更日志条目类
     */
    public static class ChangelogEntry {
        private Long id;
        private String version;
        private ChangeType changeType;
        private String description;
        private String author;
        private LocalDateTime timestamp;

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public ChangeType getChangeType() {
            return changeType;
        }

        public void setChangeType(ChangeType changeType) {
            this.changeType = changeType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }

    /**
     * 变更日志报告类
     */
    public static class ChangelogReport {
        private String version;
        private List<ChangelogEntry> entries;
        private int totalChanges;
        private java.util.Map<ChangeType, Integer> changeTypeCounts = new java.util.HashMap<>();
        private LocalDateTime generatedTime;

        // Getters and Setters
        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public List<ChangelogEntry> getEntries() {
            return entries == null ? null : new ArrayList<>(entries);
        }

        public void setEntries(List<ChangelogEntry> entries) {
            this.entries = entries == null ? null : new ArrayList<>(entries);
        }

        public int getTotalChanges() {
            return totalChanges;
        }

        public void setTotalChanges(int totalChanges) {
            this.totalChanges = totalChanges;
        }

        public java.util.Map<ChangeType, Integer> getChangeTypeCounts() {
            return changeTypeCounts == null ? null : new java.util.HashMap<>(changeTypeCounts);
        }

        public void setChangeTypeCounts(java.util.Map<ChangeType, Integer> changeTypeCounts) {
            this.changeTypeCounts = changeTypeCounts == null ? null : new java.util.HashMap<>(changeTypeCounts);
        }

        public LocalDateTime getGeneratedTime() {
            return generatedTime;
        }

        public void setGeneratedTime(LocalDateTime generatedTime) {
            this.generatedTime = generatedTime;
        }
    }

    /**
     * 分页变更日志类
     */
    public static class ChangelogPage {
        private List<ChangelogEntry> content;
        private int page;
        private int size;
        private int totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;

        // Getters and Setters
        public List<ChangelogEntry> getContent() {
            return content == null ? null : new ArrayList<>(content);
        }

        public void setContent(List<ChangelogEntry> content) {
            this.content = content == null ? null : new ArrayList<>(content);
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getTotalElements() {
            return totalElements;
        }

        public void setTotalElements(int totalElements) {
            this.totalElements = totalElements;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public boolean isFirst() {
            return first;
        }

        public void setFirst(boolean first) {
            this.first = first;
        }

        public boolean isLast() {
            return last;
        }

        public void setLast(boolean last) {
            this.last = last;
        }
    }
}

