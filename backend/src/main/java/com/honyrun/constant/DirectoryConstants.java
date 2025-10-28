package com.honyrun.constant;

/**
 * 目录结构常量类
 * 定义项目中的目录结构，以backend目录为项目根目录
 *
 * @author Mr.Rey Copyright © 2025
 * @version 1.0.0
 * @created 2025-10-26 01:36:12
 */
public final class DirectoryConstants {
    
    private DirectoryConstants() {
        // 常量类，禁止实例化
    }
    
    // ========== 项目基础目录（相对于backend目录） ==========
    
    /**
     * 源代码目录
     */
    public static final String SRC_DIR = "src";
    
    /**
     * 主源代码目录
     */
    public static final String MAIN_DIR = SRC_DIR + "/main";
    
    /**
     * 测试源代码目录
     */
    public static final String TEST_DIR = SRC_DIR + "/test";
    
    /**
     * 资源目录
     */
    public static final String RESOURCES_DIR = MAIN_DIR + "/resources";
    
    /**
     * 测试资源目录
     */
    public static final String TEST_RESOURCES_DIR = TEST_DIR + "/resources";
    
    /**
     * 目标输出目录
     */
    public static final String TARGET_DIR = "target";
    
    /**
     * 日志目录
     */
    public static final String LOGS_DIR = "logs";
    
    // ========== 特定用途目录 ==========
    
    /**
     * 监控数据目录
     */
    public static final String MONITORING_DIR = TARGET_DIR + "/monitoring";
    
    /**
     * 报告输出目录
     */
    public static final String REPORTS_DIR = TARGET_DIR + "/reports";
    
    /**
     * 测试结果目录
     */
    public static final String TEST_RESULTS_DIR = TARGET_DIR + "/surefire-reports";
    
    /**
     * 数据库脚本目录
     */
    public static final String DB_SCRIPTS_DIR = RESOURCES_DIR + "/db";
    
    /**
     * 临时文件目录
     */
    public static final String TEMP_DIR = TARGET_DIR + "/temp";
}
