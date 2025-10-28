package com.honyrun.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.honyrun.util.LoggingUtil;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Mono;

/**
 * 数据库结构验证器
 * 在应用启动时自动验证关键表结构，确保数据库与应用期望一致
 *
 * @author: Mr.Rey Copyright © 2025
 * @created: 2025-10-25 19:01:56
 * @version: 1.0.0
 */
@Component
public class DatabaseStructureValidator {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseStructureValidator.class);

    private final ConnectionFactory connectionFactory;

    /**
     * 构造函数注入 - 符合Spring Boot 3最佳实践
     *
     * @param connectionFactory 数据库连接工厂
     */
    public DatabaseStructureValidator(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * 应用启动完成后验证数据库表结构
     */
    @EventListener(ApplicationReadyEvent.class)
    public void validateTableStructure() {
        LoggingUtil.info(logger, "开始验证数据库表结构...");

        validateUsersTable()
                .doOnSuccess(result -> {
                    if (result) {
                        LoggingUtil.info(logger, "数据库表结构验证通过");
                    } else {
                        LoggingUtil.error(logger, "数据库表结构验证失败");
                    }
                })
                .doOnError(error -> {
                    LoggingUtil.error(logger, "数据库表结构验证异常: " + error.getMessage(), error);
                })
                .subscribe();
    }

    /**
     * 验证users表结构
     *
     * @return 验证结果
     */
    public Mono<Boolean> validateUsersTable() {
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> {
                    String sql = "DESCRIBE sys_users";
                    return Mono.from(connection.createStatement(sql).execute())
                            .flatMapMany(result -> result.map((row, metadata) -> {
                                String fieldName = row.get("Field", String.class);
                                String fieldType = row.get("Type", String.class);
                                LoggingUtil.debug(logger, "发现字段: " + fieldName + " (" + fieldType + ")");
                                return fieldName;
                            }))
                            .collectList()
                            .map(fields -> {
                                // 验证必需字段是否存在（使用实际数据库字段名称）
                                boolean hasId = fields.contains("id");
                                boolean hasUsername = fields.contains("username");
                                boolean hasPassword = fields.contains("password");
                                boolean hasEmail = fields.contains("email");
                                boolean hasPhone = fields.contains("phone");
                                boolean hasFullName = fields.contains("full_name"); // 实际字段名
                                boolean hasUserType = fields.contains("user_type");
                                boolean hasStatus = fields.contains("status");
                                boolean hasEnabled = fields.contains("enabled");
                                boolean hasDeleted = fields.contains("deleted");
                                boolean hasCreatedDate = fields.contains("created_at"); // 实际字段名
                                boolean hasLastModifiedDate = fields.contains("last_modified_date"); // 实际字段名

                                if (!hasId || !hasUsername || !hasPassword || !hasEmail ||
                                        !hasPhone || !hasFullName || !hasUserType || !hasStatus ||
                                        !hasEnabled || !hasDeleted || !hasCreatedDate || !hasLastModifiedDate) {

                                    LoggingUtil.error(logger, "users表缺少必需字段:");
                                    if (!hasId)
                                        LoggingUtil.error(logger, "  - 缺少字段: id");
                                    if (!hasUsername)
                                        LoggingUtil.error(logger, "  - 缺少字段: username");
                                    if (!hasPassword)
                                        LoggingUtil.error(logger, "  - 缺少字段: password");
                                    if (!hasEmail)
                                        LoggingUtil.error(logger, "  - 缺少字段: email");
                                    if (!hasPhone)
                                        LoggingUtil.error(logger, "  - 缺少字段: phone");
                                    if (!hasFullName)
                                        LoggingUtil.error(logger, "  - 缺少字段: full_name");
                                    if (!hasUserType)
                                        LoggingUtil.error(logger, "  - 缺少字段: user_type");
                                    if (!hasStatus)
                                        LoggingUtil.error(logger, "  - 缺少字段: status");
                                    if (!hasEnabled)
                                        LoggingUtil.error(logger, "  - 缺少字段: enabled");
                                    if (!hasDeleted)
                                        LoggingUtil.error(logger, "  - 缺少字段: deleted");
                                    if (!hasCreatedDate)
                                        LoggingUtil.error(logger, "  - 缺少字段: created_at");
                                    if (!hasLastModifiedDate)
                                        LoggingUtil.error(logger, "  - 缺少字段: last_modified_date");

                                    return false;
                                }

                                LoggingUtil.info(logger, "users表结构验证通过，包含所有必需字段");
                                return true;
                            });
                },
                Connection::close);
    }

    /**
     * 验证R2DBC连接健康状态
     *
     * @return 连接健康状态
     */
    public Mono<Boolean> validateConnectionHealth() {
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> {
                    String sql = "SELECT 1 as test";
                    return Mono.from(connection.createStatement(sql).execute())
                            .flatMap(result -> Mono.from(result.map((row, metadata) -> row.get("test", Integer.class))))
                            .map(result -> result != null && result == 1)
                            .doOnSuccess(healthy -> {
                                if (healthy) {
                                    LoggingUtil.info(logger, "R2DBC连接健康检查通过");
                                } else {
                                    LoggingUtil.error(logger, "R2DBC连接健康检查失败");
                                }
                            });
                },
                Connection::close);
    }
}
