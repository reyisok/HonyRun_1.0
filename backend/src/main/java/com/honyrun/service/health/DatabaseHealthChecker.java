package com.honyrun.service.health;

import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 数据库健康检查器
 *
 * <p>实现MySQL数据库连接和基本查询的健康检查，确保数据库服务可用性。
 *
 * <p><strong>检查内容：</strong>
 * <ul>
 *   <li>数据库连接状态</li>
 *   <li>基本SELECT查询</li>
 *   <li>响应时间监控</li>
 * </ul>
 *
 * @author Mr.Rey Copyright © 2025
 * @created 2025-10-25 19:52:56
 * @version 1.0.0
 */
@Component
public class DatabaseHealthChecker implements ComponentHealthChecker {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseHealthChecker.class);

    private final DatabaseClient databaseClient;

    /**
     * 构造函数注入
     *
     * @param databaseClient 数据库客户端
     */
    public DatabaseHealthChecker(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Boolean> checkHealth() {
        LoggingUtil.debug(logger, "开始执行数据库健康检查");

        return databaseClient.sql("SELECT 1 as health_check")
            .fetch()
            .first()
            .map(result -> {
                Object value = result.get("health_check");
                boolean isHealthy = "1".equals(String.valueOf(value));
                LoggingUtil.debug(logger, "数据库查询结果: {}, 健康状态: {}", value, isHealthy);
                return isHealthy;
            })
            .onErrorResume(error -> {
                LoggingUtil.error(logger, "数据库健康检查失败", error);
                return Mono.just(false);
            })
            .timeout(Duration.ofSeconds(getTimeoutSeconds()))
            .doOnSuccess(healthy ->
                LoggingUtil.info(logger, "数据库健康检查完成，状态: {}", healthy ? "健康" : "不健康"))
            .doOnError(error ->
                LoggingUtil.error(logger, "数据库健康检查超时或异常", error));
    }

    @Override
    public String getComponentName() {
        return "database";
    }

    @Override
    public int getTimeoutSeconds() {
        return 8; // 增加数据库查询超时时间到8秒，适应启动时的连接建立延迟
    }

    @Override
    public String getComponentDescription() {
        return "MySQL数据库服务健康检查";
    }

    @Override
    public boolean isEnabled() {
        return databaseClient != null;
    }
}
