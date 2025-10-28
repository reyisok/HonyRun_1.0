package com.honyrun.util.system;

import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统监控工具类
 *
 * 提供真实的系统资源监控数据获取功能，包括CPU使用率、内存使用情况、磁盘使用情况等。
 * 替换原有的模拟数据，提供准确的系统监控信息。
 *
 * 主要功能：
 * - CPU使用率监控
 * - 内存使用情况监控
 * - 磁盘使用情况监控
 * - 系统负载监控
 * - JVM运行时信息监控
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  23:30:00
 * @modified 2025-07-01 23:30:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public class SystemMonitorUtil {

    private static final Logger logger = LoggerFactory.getLogger(SystemMonitorUtil.class);

    private static final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    private static final RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

    // CPU监控相关变量
    private static int availableProcessors = Runtime.getRuntime().availableProcessors();

    /**
     * 获取CPU使用率
     *
     * @return CPU使用率百分比（0-100）
     */
    public static double getCpuUsage() {
        try {
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunOsBean = 
                    (com.sun.management.OperatingSystemMXBean) osBean;
                
                // 获取进程CPU使用率
                double processCpuLoad = sunOsBean.getProcessCpuLoad();
                if (processCpuLoad >= 0) {
                    return Math.round(processCpuLoad * 100.0 * 100.0) / 100.0;
                }
                
                // 获取系统CPU使用率
                double systemCpuLoad = sunOsBean.getCpuLoad();
                if (systemCpuLoad >= 0) {
                    return Math.round(systemCpuLoad * 100.0 * 100.0) / 100.0;
                }
            }

            // 备用方法：通过系统负载计算
            double systemLoadAverage = osBean.getSystemLoadAverage();
            if (systemLoadAverage >= 0) {
                double cpuUsage = (systemLoadAverage / availableProcessors) * 100.0;
                return Math.min(Math.round(cpuUsage * 100.0) / 100.0, 100.0);
            }

            LoggingUtil.warn(logger, "无法获取CPU使用率，返回默认值");
            return 0.0;

        } catch (Exception e) {
            LoggingUtil.error(logger, "获取CPU使用率失败", e);
            return 0.0;
        }
    }

    /**
     * 获取内存使用情况
     *
     * @return 内存使用情况Map，包含总内存、已用内存、可用内存、使用率等信息
     */
    public static Map<String, Object> getMemoryUsage() {
        Map<String, Object> memoryInfo = new HashMap<>();
        
        try {
            // JVM内存信息
            long totalMemory = Runtime.getRuntime().totalMemory();
            long freeMemory = Runtime.getRuntime().freeMemory();
            long maxMemory = Runtime.getRuntime().maxMemory();
            long usedMemory = totalMemory - freeMemory;

            memoryInfo.put("jvm.total", totalMemory / 1024 / 1024); // MB
            memoryInfo.put("jvm.used", usedMemory / 1024 / 1024); // MB
            memoryInfo.put("jvm.free", freeMemory / 1024 / 1024); // MB
            memoryInfo.put("jvm.max", maxMemory / 1024 / 1024); // MB
            memoryInfo.put("jvm.usage", Math.round((double) usedMemory / maxMemory * 100.0 * 100.0) / 100.0);

            // 系统内存信息（如果可用）
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunOsBean = 
                    (com.sun.management.OperatingSystemMXBean) osBean;
                
                long totalPhysicalMemory = sunOsBean.getTotalMemorySize();
                long freePhysicalMemory = sunOsBean.getFreeMemorySize();
                long usedPhysicalMemory = totalPhysicalMemory - freePhysicalMemory;

                if (totalPhysicalMemory > 0) {
                    memoryInfo.put("system.total", totalPhysicalMemory / 1024 / 1024); // MB
                    memoryInfo.put("system.used", usedPhysicalMemory / 1024 / 1024); // MB
                    memoryInfo.put("system.free", freePhysicalMemory / 1024 / 1024); // MB
                    memoryInfo.put("system.usage", Math.round((double) usedPhysicalMemory / totalPhysicalMemory * 100.0 * 100.0) / 100.0);
                }

                // 交换空间信息
                long totalSwapSpace = sunOsBean.getTotalSwapSpaceSize();
                long freeSwapSpace = sunOsBean.getFreeSwapSpaceSize();
                if (totalSwapSpace > 0) {
                    memoryInfo.put("swap.total", totalSwapSpace / 1024 / 1024); // MB
                    memoryInfo.put("swap.free", freeSwapSpace / 1024 / 1024); // MB
                    memoryInfo.put("swap.used", (totalSwapSpace - freeSwapSpace) / 1024 / 1024); // MB
                    memoryInfo.put("swap.usage", Math.round((double) (totalSwapSpace - freeSwapSpace) / totalSwapSpace * 100.0 * 100.0) / 100.0);
                }
            }

            LoggingUtil.debug(logger, "内存使用情况获取成功，JVM使用率: {}%", memoryInfo.get("jvm.usage"));

        } catch (Exception e) {
            LoggingUtil.error(logger, "获取内存使用情况失败", e);
            // 返回基本的JVM内存信息
            long totalMemory = Runtime.getRuntime().totalMemory();
            long freeMemory = Runtime.getRuntime().freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            memoryInfo.put("jvm.total", totalMemory / 1024 / 1024);
            memoryInfo.put("jvm.used", usedMemory / 1024 / 1024);
            memoryInfo.put("jvm.free", freeMemory / 1024 / 1024);
            memoryInfo.put("jvm.usage", Math.round((double) usedMemory / totalMemory * 100.0 * 100.0) / 100.0);
        }

        return memoryInfo;
    }

    /**
     * 获取磁盘使用情况
     *
     * @return 磁盘使用情况Map，包含各个磁盘分区的使用情况
     */
    public static Map<String, Object> getDiskUsage() {
        Map<String, Object> diskInfo = new HashMap<>();
        
        try {
            File[] roots = File.listRoots();
            double totalUsage = 0.0;
            int validRoots = 0;

            StringBuilder diskSummary = new StringBuilder("磁盘使用情况: ");
            
            for (File root : roots) {
                try {
                    String rootPath = root.getAbsolutePath();
                    long totalSpace = root.getTotalSpace();
                    long freeSpace = root.getFreeSpace();
                    long usedSpace = totalSpace - freeSpace;

                    if (totalSpace > 0) {
                        double usage = (double) usedSpace / totalSpace * 100.0;
                        
                        Map<String, Object> rootInfo = new HashMap<>();
                        rootInfo.put("total", totalSpace / 1024 / 1024 / 1024); // GB
                        rootInfo.put("used", usedSpace / 1024 / 1024 / 1024); // GB
                        rootInfo.put("free", freeSpace / 1024 / 1024 / 1024); // GB
                        rootInfo.put("usage", Math.round(usage * 100.0) / 100.0);
                        
                        diskInfo.put("disk." + rootPath.replace(":", "").replace("\\", ""), rootInfo);
                        
                        totalUsage += usage;
                        validRoots++;
                        
                        // 添加到汇总信息中
                        if (diskSummary.length() > 8) { // 如果不是第一个磁盘，添加分隔符
                            diskSummary.append(" | ");
                        }
                        diskSummary.append(String.format("%s %.1f%% (%dGB/%dGB)", 
                            rootPath, Math.round(usage * 100.0) / 100.0,
                            usedSpace / 1024 / 1024 / 1024, totalSpace / 1024 / 1024 / 1024));
                    }
                } catch (Exception e) {
                    LoggingUtil.warn(logger, "获取磁盘 {} 使用情况失败: {}", root.getAbsolutePath(), e.getMessage());
                }
            }
            
            // 输出一行汇总的磁盘使用情况
            if (validRoots > 0) {
                LoggingUtil.debug(logger, diskSummary.toString());
            }

            // 计算平均磁盘使用率
            if (validRoots > 0) {
                double averageUsage = totalUsage / validRoots;
                diskInfo.put("average.usage", Math.round(averageUsage * 100.0) / 100.0);
                LoggingUtil.debug(logger, "平均磁盘使用率: {}%", Math.round(averageUsage * 100.0) / 100.0);
            } else {
                diskInfo.put("average.usage", 0.0);
                LoggingUtil.warn(logger, "未找到有效的磁盘分区");
            }

        } catch (Exception e) {
            LoggingUtil.error(logger, "获取磁盘使用情况失败", e);
            diskInfo.put("average.usage", 0.0);
        }

        return diskInfo;
    }

    /**
     * 获取系统负载信息
     *
     * @return 系统负载信息Map
     */
    public static Map<String, Object> getSystemLoad() {
        Map<String, Object> loadInfo = new HashMap<>();
        
        try {
            // 系统负载平均值
            double loadAverage = osBean.getSystemLoadAverage();
            loadInfo.put("loadAverage", loadAverage >= 0 ? Math.round(loadAverage * 100.0) / 100.0 : -1);
            
            // 可用处理器数量
            loadInfo.put("availableProcessors", availableProcessors);
            
            // 系统架构
            loadInfo.put("arch", osBean.getArch());
            
            // 操作系统名称和版本
            loadInfo.put("osName", osBean.getName());
            loadInfo.put("osVersion", osBean.getVersion());
            
            LoggingUtil.debug(logger, "系统负载信息获取成功，负载平均值: {}", loadAverage);

        } catch (Exception e) {
            LoggingUtil.error(logger, "获取系统负载信息失败", e);
        }

        return loadInfo;
    }

    /**
     * 获取JVM运行时信息
     *
     * @return JVM运行时信息Map
     */
    public static Map<String, Object> getJvmInfo() {
        Map<String, Object> jvmInfo = new HashMap<>();
        
        try {
            // JVM基本信息
            jvmInfo.put("vmName", runtimeBean.getVmName());
            jvmInfo.put("vmVersion", runtimeBean.getVmVersion());
            jvmInfo.put("vmVendor", runtimeBean.getVmVendor());
            
            // 运行时间
            long uptime = runtimeBean.getUptime();
            jvmInfo.put("uptime", uptime);
            jvmInfo.put("uptimeFormatted", formatUptime(uptime));
            
            // 启动时间
            jvmInfo.put("startTime", runtimeBean.getStartTime());
            
            // 类路径和库路径
            jvmInfo.put("classPath", runtimeBean.getClassPath());
            jvmInfo.put("libraryPath", runtimeBean.getLibraryPath());
            
            // JVM参数
            jvmInfo.put("inputArguments", runtimeBean.getInputArguments());
            
            // 线程信息
            jvmInfo.put("activeThreadCount", Thread.activeCount());
            
            LoggingUtil.debug(logger, "JVM运行时信息获取成功，运行时间: {}", formatUptime(uptime));

        } catch (Exception e) {
            LoggingUtil.error(logger, "获取JVM运行时信息失败", e);
        }

        return jvmInfo;
    }

    /**
     * 获取综合系统资源使用情况
     *
     * @return 综合系统资源使用情况Map
     */
    public static Map<String, Object> getSystemResourceUsage() {
        Map<String, Object> resourceUsage = new HashMap<>();
        
        try {
            // CPU使用率
            double cpuUsage = getCpuUsage();
            resourceUsage.put("cpuUsage", cpuUsage);
            
            // 内存使用情况
            Map<String, Object> memoryUsage = getMemoryUsage();
            resourceUsage.put("memoryUsage", memoryUsage.get("jvm.usage"));
            resourceUsage.put("memoryInfo", memoryUsage);
            
            // 磁盘使用情况
            Map<String, Object> diskUsage = getDiskUsage();
            resourceUsage.put("diskUsage", diskUsage.get("average.usage"));
            resourceUsage.put("diskInfo", diskUsage);
            
            // 系统负载
            Map<String, Object> systemLoad = getSystemLoad();
            resourceUsage.put("systemLoad", systemLoad);
            
            // JVM信息
            Map<String, Object> jvmInfo = getJvmInfo();
            resourceUsage.put("jvmInfo", jvmInfo);
            
            // 时间戳
            resourceUsage.put("timestamp", System.currentTimeMillis());
            
            LoggingUtil.info(logger, "系统资源使用情况获取成功，CPU: {}%, 内存: {}%, 磁盘: {}%", 
                cpuUsage, memoryUsage.get("jvm.usage"), diskUsage.get("average.usage"));

        } catch (Exception e) {
            LoggingUtil.error(logger, "获取系统资源使用情况失败", e);
        }

        return resourceUsage;
    }

    /**
     * 格式化运行时间
     *
     * @param uptime 运行时间（毫秒）
     * @return 格式化的运行时间字符串
     */
    private static String formatUptime(long uptime) {
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%d天 %d小时 %d分钟", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%d小时 %d分钟", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d分钟 %d秒", minutes, seconds % 60);
        } else {
            return String.format("%d秒", seconds);
        }
    }

    /**
     * 检查系统资源是否正常
     *
     * @param cpuThreshold CPU使用率阈值
     * @param memoryThreshold 内存使用率阈值
     * @param diskThreshold 磁盘使用率阈值
     * @return 系统资源状态检查结果
     */
    public static Map<String, Object> checkSystemResourceHealth(double cpuThreshold, 
                                                               double memoryThreshold, 
                                                               double diskThreshold) {
        Map<String, Object> healthCheck = new HashMap<>();
        
        try {
            double cpuUsage = getCpuUsage();
            Map<String, Object> memoryUsage = getMemoryUsage();
            Map<String, Object> diskUsage = getDiskUsage();
            
            double memoryUsagePercent = (Double) memoryUsage.getOrDefault("jvm.usage", 0.0);
            double diskUsagePercent = (Double) diskUsage.getOrDefault("average.usage", 0.0);
            
            boolean cpuHealthy = cpuUsage <= cpuThreshold;
            boolean memoryHealthy = memoryUsagePercent <= memoryThreshold;
            boolean diskHealthy = diskUsagePercent <= diskThreshold;
            
            healthCheck.put("cpu.usage", cpuUsage);
            healthCheck.put("cpu.healthy", cpuHealthy);
            healthCheck.put("cpu.threshold", cpuThreshold);
            
            healthCheck.put("memory.usage", memoryUsagePercent);
            healthCheck.put("memory.healthy", memoryHealthy);
            healthCheck.put("memory.threshold", memoryThreshold);
            
            healthCheck.put("disk.usage", diskUsagePercent);
            healthCheck.put("disk.healthy", diskHealthy);
            healthCheck.put("disk.threshold", diskThreshold);
            
            healthCheck.put("overall.healthy", cpuHealthy && memoryHealthy && diskHealthy);
            healthCheck.put("timestamp", System.currentTimeMillis());
            
            LoggingUtil.info(logger, "系统资源健康检查完成，整体状态: {}", 
                healthCheck.get("overall.healthy"));

        } catch (Exception e) {
            LoggingUtil.error(logger, "系统资源健康检查失败", e);
            healthCheck.put("overall.healthy", false);
            healthCheck.put("error", e.getMessage());
        }

        return healthCheck;
    }
}


