package com.honyrun.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.honyrun.config.DatabaseStructureValidator;
import com.honyrun.util.LoggingUtil;

import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Mono;

/**
 * 数据库健康监控服务
 * 定期检查数据库连接状态，监控R2DBC连接池健康状况
 *
 * @author: Mr.Rey Copyright © 2025
 * @created: 2025-10-25 19:01:56
 * @version: 1.0.0
 */
@Service
public class DatabaseHealthService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseHealthService.class);

    private final DatabaseStructureValidator databaseStructureValidator;
    private final ConnectionFactory connectionFactory;

    /**
     * 构造函数注入依赖
     *
     * @param databaseStructureValidator 数据库结构验证器
     * @param connectionFactory          连接工厂
     * @author Mr.Rey Copyright © 2025
     * @created 2025-10-26 01:36:12
     * @modified 2025-10-26 01:36:12
     * @version 1.0.0
     */
    public DatabaseHealthService(DatabaseStructureValidator databaseStructureValidator,
            ConnectionFactory connectionFactory) {
        this.databaseStructureValidator = databaseStructureValidator;
        this.connectionFactory = connectionFactory;
    }

    /**
     * 定期检查数据库连接健康状态
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void checkDatabaseHealth() {
        LoggingUtil.debug(logger, "开始数据库健康检查...");

        databaseStructureValidator.validateConnectionHealth()
                .doOnSuccess(healthy -> {
                    if (healthy) {
                        LoggingUtil.debug(logger, "数据库连接健康检查通过");
                    } else {
                        LoggingUtil.warn(logger, "数据库连接健康检查失败，可能需要重建连接池");
                        // 这里可以添加连接池重建逻辑
                    }
                })
                .doOnError(error -> {
                    LoggingUtil.error(logger, "数据库健康检查异常: " + error.getMessage(), error);
                    // 记录异常，可能需要告警
                })
                .subscribe();
    }

    /**
     * 手动触发数据库健康检查
     *
     * @return 健康检查结果
     */
    public Mono<Boolean> performHealthCheck() {
        LoggingUtil.info(logger, "执行手动数据库健康检查");
        return databaseStructureValidator.validateConnectionHealth();
    }

    /**
     * 检查数据库表结构完整性
     *
     * @return 表结构检查结果
     */
    public Mono<Boolean> checkTableStructure() {
        LoggingUtil.info(logger, "执行数据库表结构检查");
        return databaseStructureValidator.validateUsersTable();
    }

    /**
     * 获取连接池统计信息
     *
     * @return 连接池状态信息
     */
    public Mono<String> getConnectionPoolStats() {
        // 这里可以添加连接池统计信息获取逻辑
        // 由于R2DBC连接池API限制，暂时返回基本信息
        return Mono.just("R2DBC连接池运行中");
    }
}
