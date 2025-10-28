package com.honyrun.exception;

/**
 * 数据库异常类
 * 专门处理数据库相关的异常情况
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 21:03:21
 * @modified 2025-07-01 21:03:21
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class DatabaseException extends RuntimeException {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final ErrorCode errorCode;

    /**
     * 数据库操作类型
     */
    private String operation;

    /**
     * 表名
     */
    private String tableName;

    /**
     * SQL语句
     */
    private String sql;

    /**
     * 构造函数
     *
     * @param message 错误消息
     */
    public DatabaseException(String message) {
        super(message);
        this.errorCode = ErrorCode.DATABASE_ERROR;
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     * @param cause 原始异常
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.DATABASE_ERROR;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message 错误消息
     */
    public DatabaseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message 错误消息
     * @param cause 原始异常
     */
    public DatabaseException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建连接异常
     *
     * @param cause 原始异常
     * @return 数据库异常实例
     */
    public static DatabaseException connectionError(Throwable cause) {
        return new DatabaseException(ErrorCode.DATABASE_CONNECTION_ERROR, "数据库连接失败", cause);
    }

    /**
     * 创建超时异常
     *
     * @param operation 操作类型
     * @param cause 原始异常
     * @return 数据库异常实例
     */
    public static DatabaseException timeoutError(String operation, Throwable cause) {
        DatabaseException exception = new DatabaseException(ErrorCode.DATABASE_TIMEOUT, 
                String.format("数据库操作超时: %s", operation), cause);
        exception.setOperation(operation);
        return exception;
    }

    /**
     * 创建SQL执行异常
     *
     * @param sql SQL语句
     * @param cause 原始异常
     * @return 数据库异常实例
     */
    public static DatabaseException sqlExecutionError(String sql, Throwable cause) {
        DatabaseException exception = new DatabaseException(ErrorCode.SQL_EXECUTION_ERROR, 
                "SQL执行失败", cause);
        exception.setSql(sql);
        return exception;
    }

    /**
     * 创建事务异常
     *
     * @param operation 事务操作
     * @param cause 原始异常
     * @return 数据库异常实例
     */
    public static DatabaseException transactionError(String operation, Throwable cause) {
        DatabaseException exception = new DatabaseException(ErrorCode.TRANSACTION_ERROR,
                String.format("事务处理失败: %s", operation), cause);
        exception.setOperation(operation);
        return exception;
    }

    /**
     * 创建数据完整性异常
     *
     * @param tableName 表名
     * @param constraint 约束名称
     * @param cause 原始异常
     * @return 数据库异常实例
     */
    public static DatabaseException dataIntegrityError(String tableName, String constraint, Throwable cause) {
        DatabaseException exception = new DatabaseException(ErrorCode.DATA_INTEGRITY_ERROR,
                String.format("数据完整性约束违反: %s.%s", tableName, constraint), cause);
        exception.setTableName(tableName);
        return exception;
    }

    // ==================== Getter和Setter方法 ====================

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    // ==================== 链式调用方法 ====================

    /**
     * 设置操作类型（链式调用）
     *
     * @param operation 操作类型
     * @return 当前异常实例
     */
    public DatabaseException withOperation(String operation) {
        this.operation = operation;
        return this;
    }

    /**
     * 设置表名（链式调用）
     *
     * @param tableName 表名
     * @return 当前异常实例
     */
    public DatabaseException withTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    /**
     * 设置SQL语句（链式调用）
     *
     * @param sql SQL语句
     * @return 当前异常实例
     */
    public DatabaseException withSql(String sql) {
        this.sql = sql;
        return this;
    }

    @Override
    public String toString() {
        return String.format("DatabaseException{errorCode=%s, operation='%s', tableName='%s', sql='%s', message='%s'}",
                errorCode, operation, tableName, sql, getMessage());
    }
}


