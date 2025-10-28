package com.honyrun.exception;

/**
 * 数据访问异常类
 * 实现数据库操作异常和连接异常
 *
 * @author Mr.Rey
 * @since 2025-07-01
 * @version 2.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class DataAccessException extends RuntimeException {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final ErrorCode errorCode;

    /**
     * 请求路径
     */
    private String path;

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
     * @param errorCode 错误码枚举
     */
    public DataAccessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码枚举
     * @param message 自定义错误消息
     */
    public DataAccessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码枚举
     * @param cause 原始异常
     */
    public DataAccessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码枚举
     * @param message 自定义错误消息
     * @param cause 原始异常
     */
    public DataAccessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     */
    public DataAccessException(String message) {
        super(message);
        this.errorCode = ErrorCode.DATABASE_ERROR;
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     * @param cause 原始异常
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.DATABASE_ERROR;
    }

    /**
     * 创建数据库连接异常
     *
     * @param cause 原始异常
     * @return 数据访问异常实例
     */
    public static DataAccessException connectionError(Throwable cause) {
        return new DataAccessException(ErrorCode.DATABASE_CONNECTION_ERROR,
                "数据库连接失败", cause);
    }

    /**
     * 创建数据库超时异常
     *
     * @param operation 操作类型
     * @param cause 原始异常
     * @return 数据访问异常实例
     */
    public static DataAccessException timeoutError(String operation, Throwable cause) {
        DataAccessException exception = new DataAccessException(ErrorCode.DATABASE_TIMEOUT,
                String.format("数据库操作超时: %s", operation), cause);
        exception.setOperation(operation);
        return exception;
    }

    /**
     * 创建SQL执行异常
     *
     * @param sql SQL语句
     * @param cause 原始异常
     * @return 数据访问异常实例
     */
    public static DataAccessException sqlExecutionError(String sql, Throwable cause) {
        DataAccessException exception = new DataAccessException(ErrorCode.SQL_EXECUTION_ERROR,
                "SQL执行失败", cause);
        exception.setSql(sql);
        return exception;
    }

    /**
     * 创建数据完整性异常
     *
     * @param tableName 表名
     * @param constraint 约束名称
     * @param cause 原始异常
     * @return 数据访问异常实例
     */
    public static DataAccessException dataIntegrityError(String tableName, String constraint, Throwable cause) {
        DataAccessException exception = new DataAccessException(ErrorCode.DATA_INTEGRITY_ERROR,
                String.format("数据完整性约束违反: %s.%s", tableName, constraint), cause);
        exception.setTableName(tableName);
        return exception;
    }

    /**
     * 创建事务异常
     *
     * @param operation 事务操作
     * @param cause 原始异常
     * @return 数据访问异常实例
     */
    public static DataAccessException transactionError(String operation, Throwable cause) {
        DataAccessException exception = new DataAccessException(ErrorCode.TRANSACTION_ERROR,
                String.format("事务处理失败: %s", operation), cause);
        exception.setOperation(operation);
        return exception;
    }

    /**
     * 创建查询异常
     *
     * @param tableName 表名
     * @param cause 原始异常
     * @return 数据访问异常实例
     */
    public static DataAccessException queryError(String tableName, Throwable cause) {
        DataAccessException exception = new DataAccessException(ErrorCode.DATABASE_ERROR,
                String.format("查询失败: %s", tableName), cause);
        exception.setTableName(tableName);
        exception.setOperation("SELECT");
        return exception;
    }

    /**
     * 创建插入异常
     *
     * @param tableName 表名
     * @param cause 原始异常
     * @return 数据访问异常实例
     */
    public static DataAccessException insertError(String tableName, Throwable cause) {
        DataAccessException exception = new DataAccessException(ErrorCode.DATABASE_ERROR,
                String.format("插入失败: %s", tableName), cause);
        exception.setTableName(tableName);
        exception.setOperation("INSERT");
        return exception;
    }

    /**
     * 创建更新异常
     *
     * @param tableName 表名
     * @param cause 原始异常
     * @return 数据访问异常实例
     */
    public static DataAccessException updateError(String tableName, Throwable cause) {
        DataAccessException exception = new DataAccessException(ErrorCode.DATABASE_ERROR,
                String.format("更新失败: %s", tableName), cause);
        exception.setTableName(tableName);
        exception.setOperation("UPDATE");
        return exception;
    }

    /**
     * 创建删除异常
     *
     * @param tableName 表名
     * @param cause 原始异常
     * @return 数据访问异常实例
     */
    public static DataAccessException deleteError(String tableName, Throwable cause) {
        DataAccessException exception = new DataAccessException(ErrorCode.DATABASE_ERROR,
                String.format("删除失败: %s", tableName), cause);
        exception.setTableName(tableName);
        exception.setOperation("DELETE");
        return exception;
    }

    /**
     * 创建R2DBC异常
     *
     * @param operation 操作类型
     * @param cause 原始异常
     * @return 数据访问异常实例
     */
    public static DataAccessException r2dbcError(String operation, Throwable cause) {
        DataAccessException exception = new DataAccessException(ErrorCode.DATABASE_ERROR,
                String.format("R2DBC操作失败: %s", operation), cause);
        exception.setOperation(operation);
        return exception;
    }

    // Getter和Setter方法
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public DataAccessException withPath(String path) {
        this.path = path;
        return this;
    }

    public DataAccessException withOperation(String operation) {
        this.operation = operation;
        return this;
    }

    public DataAccessException withTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public DataAccessException withSql(String sql) {
        this.sql = sql;
        return this;
    }

    @Override
    public String toString() {
        return String.format("DataAccessException{errorCode=%s, message='%s', operation='%s', tableName='%s'}",
                errorCode, getMessage(), operation, tableName);
    }
}

