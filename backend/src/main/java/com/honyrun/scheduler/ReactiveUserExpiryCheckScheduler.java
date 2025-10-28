package com.honyrun.scheduler;

import com.honyrun.config.UnifiedConfigManager;
import com.honyrun.service.reactive.ReactiveSystemService;
import com.honyrun.util.LoggingUtil;
import com.honyrun.util.MonitoringLogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 响应式普通用户有效期检查定时任务
 *
 * 提供普通用户有效期检查和自动处理功能，支持用户过期自动禁用和通知
 * 支持响应式异步执行，避免阻塞主线程
 *
 * 主要功能：
 * - 普通用户有效期检查
 * - 过期用户自动禁用
 * - 即将过期用户提醒
 * - 用户有效期统计
 * - 用户状态更新
 * - 过期用户清理
 *
 * 响应式特性：
 * - 非阻塞执行：用户检查异步执行，不影响系统正常运行
 * - 错误恢复：检查失败时提供错误恢复机制
 * - 流式处理：支持批量用户数据的流式处理
 * - 背压控制：控制用户检查的执行速度
 *
 * 有效期规则：
 * - 普通用户账号有有效期限制，过期后自动禁用
 * - 以午夜12点为分界，过期检查在每日午夜执行
 * - 过期用户自动设置为禁用状态，需要系统用户重新激活
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 00:05:00
 * @modified 2025-10-26 01:36:12
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
@ConditionalOnProperty(name = "honyrun.scheduler.user-expiry-check.enabled", havingValue = "true", matchIfMissing = true)
public class ReactiveUserExpiryCheckScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveUserExpiryCheckScheduler.class);

    private final ReactiveSystemService systemService;
    private final UnifiedConfigManager unifiedConfigManager;

    /**
     * 构造函数注入 - 符合Spring Boot 3最佳实践
     * 
     * @param systemService 系统服务
     * @param unifiedConfigManager 统一配置管理器
     */
    public ReactiveUserExpiryCheckScheduler(ReactiveSystemService systemService, 
                                          UnifiedConfigManager unifiedConfigManager) {
        this.systemService = systemService;
        this.unifiedConfigManager = unifiedConfigManager;
    }

    // 系统启动时间
    private final LocalDateTime startTime = LocalDateTime.now();

    // 用户有效期检查计数器
    private final AtomicLong userExpiryCheckCount = new AtomicLong(0);
    private final AtomicLong expiredUserCount = new AtomicLong(0);
    private final AtomicLong warningUserCount = new AtomicLong(0);
    private final AtomicLong disabledUserCount = new AtomicLong(0);
    private final AtomicLong cleanupUserCount = new AtomicLong(0);

    // 最后执行时间
    private volatile LocalDateTime lastUserExpiryCheckTime;
    private volatile LocalDateTime lastExpiredUserProcessTime;
    private volatile LocalDateTime lastWarningNotificationTime;
    private volatile LocalDateTime lastUserCleanupTime;

    // ==================== 用户有效期检查任务 ====================

    /**
     * 用户有效期检查任务
     * 每天午夜12点执行，检查所有普通用户的有效期
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void userExpiryCheckTask() {
        LoggingUtil.info(logger, "开始执行用户有效期检查任务");

        executeUserExpiryCheck()
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> {
                    userExpiryCheckCount.incrementAndGet();
                    lastUserExpiryCheckTime = LocalDateTime.now();
                    LoggingUtil.info(logger, "用户有效期检查任务完成，累计执行次数: {}", userExpiryCheckCount.get());
                })
                .doOnError(error -> LoggingUtil.error(logger, "用户有效期检查任务执行失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "用户有效期检查任务执行失败", error);
                    MonitoringLogUtil.logSystemAlert("USER_EXPIRY_CHECK", "ERROR", 
                            "用户有效期检查任务执行失败: " + error.getMessage(), null);
                    return Mono.empty();
                })
                .subscribe();
    }

    /**
     * 过期用户处理任务
     * 每天凌晨1点执行，处理已过期的用户
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void expiredUserProcessTask() {
        LoggingUtil.info(logger, "开始执行过期用户处理任务");

        executeExpiredUserProcess()
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> {
                    lastExpiredUserProcessTime = LocalDateTime.now();
                    LoggingUtil.info(logger, "过期用户处理任务完成");
                })
                .doOnError(error -> LoggingUtil.error(logger, "过期用户处理任务执行失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "过期用户处理任务执行失败", error);
                    MonitoringLogUtil.logSystemAlert("EXPIRED_USER_PROCESS", "ERROR", 
                            "过期用户处理任务执行失败: " + error.getMessage(), null);
                    return Mono.empty();
                })
                .subscribe();
    }

    /**
     * 用户过期提醒任务
     * 每天上午9点执行，提醒即将过期的用户
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void userExpiryWarningTask() {
        // 注意：这里使用同步方法是必要的，因为@Scheduled方法需要同步检查配置决定是否执行任务
        boolean notificationEnabled = Boolean.parseBoolean(unifiedConfigManager.getProperty("honyrun.user.expiry.notification-enabled", "true"));
        
        if (!notificationEnabled) {
            LoggingUtil.debug(logger, "用户过期提醒已禁用，跳过执行");
            return;
        }

        LoggingUtil.info(logger, "开始执行用户过期提醒任务");

        executeUserExpiryWarning()
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> {
                    lastWarningNotificationTime = LocalDateTime.now();
                    LoggingUtil.info(logger, "用户过期提醒任务完成");
                })
                .doOnError(error -> LoggingUtil.error(logger, "用户过期提醒任务执行失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "用户过期提醒任务执行失败", error);
                    MonitoringLogUtil.logSystemAlert("USER_EXPIRY_WARNING", "ERROR", 
                            "用户过期提醒任务执行失败: " + error.getMessage(), null);
                    return Mono.empty();
                })
                .subscribe();
    }

    /**
     * 过期用户清理任务
     * 每周凌晨3点执行，清理长期禁用的过期用户
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    public void expiredUserCleanupTask() {
        LoggingUtil.info(logger, "开始执行过期用户清理任务");

        executeExpiredUserCleanup()
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> {
                    lastUserCleanupTime = LocalDateTime.now();
                    LoggingUtil.info(logger, "过期用户清理任务完成");
                })
                .doOnError(error -> LoggingUtil.error(logger, "过期用户清理任务执行失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "过期用户清理任务执行失败", error);
                    MonitoringLogUtil.logSystemAlert("EXPIRED_USER_CLEANUP", "ERROR", 
                            "过期用户清理任务执行失败: " + error.getMessage(), null);
                    return Mono.empty();
                })
                .subscribe();
    }

    // ==================== 用户有效期检查实现 ====================

    /**
     * 执行用户有效期检查
     */
    private Mono<Void> executeUserExpiryCheck() {
        return Mono.fromRunnable(() -> LoggingUtil.info(logger, "开始用户有效期检查"))
                .then(checkExpiredUsers())
                .then(checkWarningUsers())
                .then(generateExpiryReport())
                .then(Mono.fromRunnable(() -> 
                    MonitoringLogUtil.logSystemEvent("USER_EXPIRY_CHECK", "INFO", "用户有效期检查完成")))
                .then(Mono.empty());
    }

    /**
     * 执行过期用户处理
     */
    private Mono<Void> executeExpiredUserProcess() {
        return Mono.fromRunnable(() -> LoggingUtil.info(logger, "开始过期用户处理"))
                .then(processExpiredUsers())
                .then(updateUserStatistics())
                .then(Mono.fromRunnable(() -> 
                    MonitoringLogUtil.logSystemEvent("EXPIRED_USER_PROCESS", "INFO", "过期用户处理完成")))
                .then(Mono.empty());
    }

    /**
     * 执行用户过期提醒
     */
    private Mono<Void> executeUserExpiryWarning() {
        return Mono.fromRunnable(() -> LoggingUtil.info(logger, "开始用户过期提醒"))
                .then(sendExpiryWarningNotifications())
                .then(Mono.fromRunnable(() -> 
                    MonitoringLogUtil.logSystemEvent("USER_EXPIRY_WARNING", "INFO", "用户过期提醒完成")))
                .then(Mono.empty());
    }

    /**
     * 执行过期用户清理
     */
    private Mono<Void> executeExpiredUserCleanup() {
        return Mono.fromRunnable(() -> LoggingUtil.info(logger, "开始过期用户清理"))
                .then(cleanupDisabledUsers())
                .then(Mono.fromRunnable(() -> 
                    MonitoringLogUtil.logSystemEvent("EXPIRED_USER_CLEANUP", "INFO", "过期用户清理完成")))
                .then(Mono.empty());
    }

    /**
     * 检查过期用户
     */
    private Mono<Void> checkExpiredUsers() {
        LoggingUtil.info(logger, "检查过期用户");

        return unifiedConfigManager.getStringConfig("honyrun.user.expiry.batch-size", "50")
                .map(Integer::parseInt)
                .flatMap(batchSize -> systemService.findExpiredUsers(batchSize)
                        .flatMap(expiredUsers -> {
                            long expiredCount = expiredUsers.size();
                            expiredUserCount.addAndGet(expiredCount);

                            LoggingUtil.info(logger, "发现{}个过期用户", expiredCount);

                            if (expiredCount > 0) {
                                return systemService.recordSystemEvent(
                                        "EXPIRED_USERS_FOUND",
                                        "WARN",
                                        "USER_MANAGEMENT",
                                        String.format("发现%d个过期用户需要处理", expiredCount)
                                );
                            }
                            return Mono.empty();
                        }))
                .then();
    }

    /**
     * 检查即将过期用户
     */
    private Mono<Void> checkWarningUsers() {
        LoggingUtil.info(logger, "检查即将过期用户");

        return Mono.zip(
                unifiedConfigManager.getStringConfig("honyrun.user.expiry.warning-days", "7"),
                unifiedConfigManager.getStringConfig("honyrun.user.expiry.batch-size", "50")
        ).flatMap(tuple -> {
            int warningDays = Integer.parseInt(tuple.getT1());
            int batchSize = Integer.parseInt(tuple.getT2());
            
            LoggingUtil.info(logger, "检查即将过期用户，提前{}天提醒", warningDays);

            return systemService.findUsersExpiringInDays(warningDays, batchSize)
                    .flatMap(warningUsers -> {
                        long warningCount = warningUsers.size();
                        warningUserCount.addAndGet(warningCount);

                        LoggingUtil.info(logger, "发现{}个即将过期用户", warningCount);

                        if (warningCount > 0) {
                            return systemService.recordSystemEvent(
                                    "USERS_EXPIRING_SOON",
                                    "INFO",
                                    "USER_MANAGEMENT",
                                    String.format("发现%d个用户将在%d天内过期", warningCount, warningDays)
                            );
                        }
                        return Mono.empty();
                    });
        }).then();
    }

    /**
     * 处理过期用户
     */
    private Mono<Void> processExpiredUsers() {
        return Mono.zip(
                unifiedConfigManager.getStringConfig("honyrun.user.expiry.auto-disable-enabled", "true"),
                unifiedConfigManager.getStringConfig("honyrun.user.expiry.batch-size", "50")
        ).flatMap(tuple -> {
            boolean autoDisableEnabled = Boolean.parseBoolean(tuple.getT1());
            int batchSize = Integer.parseInt(tuple.getT2());
            
            if (!autoDisableEnabled) {
                LoggingUtil.info(logger, "自动禁用过期用户功能已禁用，跳过处理");
                return Mono.empty();
            }

            LoggingUtil.info(logger, "处理过期用户，自动禁用");

            return systemService.disableExpiredUsers(batchSize)
                    .flatMap(disabledCount -> {
                        disabledUserCount.addAndGet(disabledCount);

                        LoggingUtil.info(logger, "自动禁用{}个过期用户", disabledCount);

                        if (disabledCount > 0) {
                            return systemService.recordSystemEvent(
                                    "EXPIRED_USERS_DISABLED",
                                    "WARN",
                                    "USER_MANAGEMENT",
                                    String.format("自动禁用%d个过期用户", disabledCount)
                            );
                        }
                        return Mono.empty();
                    });
        }).then();
    }

    /**
     * 发送过期提醒通知
     */
    private Mono<Void> sendExpiryWarningNotifications() {
        LoggingUtil.info(logger, "发送用户过期提醒通知");

        return Mono.zip(
                unifiedConfigManager.getStringConfig("honyrun.user.expiry.warning-days", "7"),
                unifiedConfigManager.getStringConfig("honyrun.user.expiry.batch-size", "50")
        ).flatMap(tuple -> {
            int warningDays = Integer.parseInt(tuple.getT1());
            int batchSize = Integer.parseInt(tuple.getT2());

            return systemService.sendExpiryWarningNotifications(warningDays, batchSize)
                    .flatMap(notificationCount -> {
                        LoggingUtil.info(logger, "发送{}条过期提醒通知", notificationCount);

                        if (notificationCount > 0) {
                            return systemService.recordSystemEvent(
                                    "EXPIRY_WARNING_SENT",
                                    "INFO",
                                    "USER_MANAGEMENT",
                                    String.format("发送%d条用户过期提醒通知", notificationCount)
                            );
                        }
                        return Mono.empty();
                    });
        }).then();
    }

    /**
     * 清理长期禁用的用户
     */
    private Mono<Void> cleanupDisabledUsers() {
        return Mono.zip(
                unifiedConfigManager.getStringConfig("honyrun.user.expiry.cleanup-disabled-days", "30"),
                unifiedConfigManager.getStringConfig("honyrun.user.expiry.batch-size", "50")
        ).flatMap(tuple -> {
            int cleanupDisabledDays = Integer.parseInt(tuple.getT1());
            int batchSize = Integer.parseInt(tuple.getT2());
            
            LoggingUtil.info(logger, "清理长期禁用的用户，清理{}天前禁用的用户", cleanupDisabledDays);

            return systemService.cleanupLongTermDisabledUsers(cleanupDisabledDays, batchSize)
                    .flatMap(cleanupCount -> {
                        cleanupUserCount.addAndGet(cleanupCount);

                        LoggingUtil.info(logger, "清理{}个长期禁用用户", cleanupCount);

                        if (cleanupCount > 0) {
                            return systemService.recordSystemEvent(
                                    "DISABLED_USERS_CLEANED",
                                    "INFO",
                                    "USER_MANAGEMENT",
                                    String.format("清理%d个长期禁用用户", cleanupCount)
                            );
                        }
                        return Mono.empty();
                    });
        }).then();
    }

    /**
     * 生成有效期报告
     */
    private Mono<Void> generateExpiryReport() {
        LoggingUtil.info(logger, "生成用户有效期报告");

        return systemService.generateUserExpiryReport()
                .doOnSuccess(unused -> LoggingUtil.info(logger, "用户有效期报告生成完成"))
                .doOnError(error -> LoggingUtil.error(logger, "用户有效期报告生成失败", error))
                .then();
    }

    /**
     * 更新用户统计
     */
    private Mono<Void> updateUserStatistics() {
        LoggingUtil.info(logger, "更新用户统计信息");

        return systemService.updateUserExpiryStatistics()
                .doOnSuccess(unused -> LoggingUtil.info(logger, "用户统计信息更新完成"))
                .doOnError(error -> LoggingUtil.error(logger, "用户统计信息更新失败", error))
                .then();
    }

    // ==================== 辅助方法 ====================

    // 已移除createSystemLog和createErrorLog方法，现在使用MonitoringLogUtil进行文件日志记录

    // ==================== 监控状态查询方法 ====================

    /**
     * 获取调度器运行状态
     */
    public java.util.Map<String, Object> getSchedulerStatus() {
        java.util.Map<String, Object> status = new java.util.HashMap<>();
        status.put("startTime", startTime);
        status.put("uptime", java.time.Duration.between(startTime, LocalDateTime.now()).toMillis());
        status.put("userExpiryCheckCount", userExpiryCheckCount.get());
        status.put("expiredUserCount", expiredUserCount.get());
        status.put("warningUserCount", warningUserCount.get());
        status.put("disabledUserCount", disabledUserCount.get());
        status.put("cleanupUserCount", cleanupUserCount.get());
        status.put("lastUserExpiryCheckTime", lastUserExpiryCheckTime);
        status.put("lastExpiredUserProcessTime", lastExpiredUserProcessTime);
        status.put("lastWarningNotificationTime", lastWarningNotificationTime);
        status.put("lastUserCleanupTime", lastUserCleanupTime);
        
        // 使用默认值，避免在状态查询时阻塞
        status.put("warningDays", unifiedConfigManager.getProperty("honyrun.user.expiry.warning-days", "7"));
        status.put("autoDisableEnabled", unifiedConfigManager.getProperty("honyrun.user.expiry.auto-disable-enabled", "true"));
        status.put("notificationEnabled", unifiedConfigManager.getProperty("honyrun.user.expiry.notification-enabled", "true"));
        status.put("cleanupDisabledDays", unifiedConfigManager.getProperty("honyrun.user.expiry.cleanup-disabled-days", "30"));
        status.put("batchSize", unifiedConfigManager.getProperty("honyrun.user.expiry.batch-size", "50"));
        return status;
    }

    /**
     * 重置用户有效期检查计数器
     */
    public void resetCounters() {
        LoggingUtil.info(logger, "重置用户有效期检查计数器");
        userExpiryCheckCount.set(0);
        expiredUserCount.set(0);
        warningUserCount.set(0);
        disabledUserCount.set(0);
        cleanupUserCount.set(0);
    }

    /**
     * 获取用户有效期统计信息
     */
    public String getUserExpiryStatistics() {
        return String.format("用户有效期统计 - 检查次数: %d, 过期用户: %d, 提醒用户: %d, 禁用用户: %d, 清理用户: %d",
                userExpiryCheckCount.get(),
                expiredUserCount.get(),
                warningUserCount.get(),
                disabledUserCount.get(),
                cleanupUserCount.get());
    }
}


