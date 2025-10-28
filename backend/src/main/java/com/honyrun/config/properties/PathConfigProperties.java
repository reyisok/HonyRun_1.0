package com.honyrun.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 路径配置属性类
 * 与统一配置管理整合，支持从application.properties中配置路径
 * 所有路径计算都以backend目录为项目根目录
 *
 * 【重要】：所有配置值必须从环境配置文件中读取
 * 配置文件路径：
 * - 开发环境：application-dev.properties
 * - 测试环境：application-test.properties
 * - 生产环境：application-prod.properties
 *
 * @author Mr.Rey Copyright © 2025
 * @version 1.0.1 - 移除硬编码，严格遵循统一配置管理规范
 * @created 2025-10-26 01:36:12
 * @modified 2025-10-27 12:26:43
 */
@Configuration
@ConfigurationProperties(prefix = "honyrun.path")
public class PathConfigProperties {

    /**
     * src目录相对路径（所有路径计算的基准点）
     * 必须从配置文件获取：honyrun.path.src-base
     */
    private String srcBase;

    /**
     * 日志目录路径（相对于项目根目录）
     * 必须从配置文件获取：honyrun.path.logs-dir
     */
    private String logsDir;

    /**
     * 临时文件目录路径（相对于项目根目录）
     * 必须从配置文件获取：honyrun.path.temp-dir
     */
    private String tempDir;

    /**
     * 监控数据目录路径（相对于项目根目录）
     * 必须从配置文件获取：honyrun.path.monitoring-dir
     */
    private String monitoringDir;

    /**
     * 报告输出目录路径（相对于项目根目录）
     * 必须从配置文件获取：honyrun.path.reports-dir
     */
    private String reportsDir;

    /**
     * 获取src目录相对路径
     *
     * @return src目录相对路径
     */
    public String getSrcBase() {
        return srcBase;
    }

    /**
     * 设置src目录相对路径
     *
     * @param srcBase src目录相对路径
     */
    public void setSrcBase(String srcBase) {
        this.srcBase = srcBase;
    }

    /**
     * 获取日志目录路径
     *
     * @return 日志目录路径
     */
    public String getLogsDir() {
        return logsDir;
    }

    /**
     * 设置日志目录路径
     *
     * @param logsDir 日志目录路径
     */
    public void setLogsDir(String logsDir) {
        this.logsDir = logsDir;
    }

    /**
     * 获取临时文件目录路径
     *
     * @return 临时文件目录路径
     */
    public String getTempDir() {
        return tempDir;
    }

    /**
     * 设置临时文件目录路径
     *
     * @param tempDir 临时文件目录路径
     */
    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    /**
     * 获取监控数据目录路径
     *
     * @return 监控数据目录路径
     */
    public String getMonitoringDir() {
        return monitoringDir;
    }

    /**
     * 设置监控数据目录路径
     *
     * @param monitoringDir 监控数据目录路径
     */
    public void setMonitoringDir(String monitoringDir) {
        this.monitoringDir = monitoringDir;
    }

    /**
     * 获取报告输出目录路径
     *
     * @return 报告输出目录路径
     */
    public String getReportsDir() {
        return reportsDir;
    }

    /**
     * 设置报告输出目录路径
     *
     * @param reportsDir 报告输出目录路径
     */
    public void setReportsDir(String reportsDir) {
        this.reportsDir = reportsDir;
    }
}
