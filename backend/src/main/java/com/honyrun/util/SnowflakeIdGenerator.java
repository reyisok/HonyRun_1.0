package com.honyrun.util;

import java.net.InetAddress;
import java.net.NetworkInterface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 雪花算法ID生成器工具类
 *
 * 基于Snowflake算法实现，生成64位长整型唯一ID
 *
 * ID结构（64位）：
 * - 1位符号位（固定为0）
 * - 41位时间戳（毫秒级，可使用约69年）
 * - 10位机器ID（5位数据中心ID + 5位机器ID，支持1024台机器）
 * - 12位序列号（同一毫秒内可生成4096个ID）
 *
 * 特性：
 * - 高性能：单机每秒可生成400万个ID
 * - 唯一性：分布式环境下保证全局唯一
 * - 有序性：生成的ID按时间递增
 * - 可读性：ID包含时间信息，便于调试
 * - 线程安全：支持多线程并发访问
 *
 * 使用场景：
 * - 用户ID生成：确保用户ID全局唯一且有序
 * - 订单号生成：生成唯一的订单标识
 * - 消息ID生成：分布式消息系统中的消息标识
 * - 其他需要唯一ID的业务场景
 *
 * 注意事项：
 * - 依赖系统时钟，时钟回拨会导致ID重复
 * - 机器ID需要在集群中保持唯一
 * - 单机部署时自动获取机器ID
 * - 集群部署时建议手动配置机器ID
 *
 * @author Mr.Rey
 * @created 2025-07-01 16:00:00
 * @modified 2025-07-01 16:00:00
 * @version 1.0.0
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
public class SnowflakeIdGenerator {

    private static final Logger logger = LoggerFactory.getLogger(SnowflakeIdGenerator.class);

    // ==================== 雪花算法常量定义 ====================

    /**
     * 起始时间戳（2024-01-01 00:00:00 UTC）
     * 可使用约69年（到2093年）
     */
    private static final long EPOCH = 1704067200000L;

    /**
     * 机器ID位数（5位）
     */
    private static final long WORKER_ID_BITS = 5L;

    /**
     * 数据中心ID位数（5位）
     */
    private static final long DATACENTER_ID_BITS = 5L;

    /**
     * 序列号位数（12位）
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 机器ID最大值（31）
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /**
     * 数据中心ID最大值（31）
     */
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    /**
     * 序列号最大值（4095）
     */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    /**
     * 机器ID左移位数（12位）
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /**
     * 数据中心ID左移位数（17位）
     */
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * 时间戳左移位数（22位）
     */
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    // ==================== 实例变量 ====================

    /**
     * 机器ID（0-31）
     */
    private final long workerId;

    /**
     * 数据中心ID（0-31）
     */
    private final long datacenterId;

    /**
     * 序列号（0-4095）
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间戳
     */
    private long lastTimestamp = -1L;

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     * 自动获取机器ID和数据中心ID
     */
    public SnowflakeIdGenerator() {
        this.workerId = getWorkerId();
        this.datacenterId = getDatacenterId();

        LoggingUtil.info(logger, "雪花算法ID生成器初始化完成 - 机器ID: {}, 数据中心ID: {}",
                workerId, datacenterId);
    }

    /**
     * 指定机器ID和数据中心ID的构造函数
     *
     * @param workerId 机器ID（0-31）
     * @param datacenterId 数据中心ID（0-31）
     */
    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("机器ID必须在0到%d之间，当前值: %d", MAX_WORKER_ID, workerId));
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("数据中心ID必须在0到%d之间，当前值: %d", MAX_DATACENTER_ID, datacenterId));
        }

        this.workerId = workerId;
        this.datacenterId = datacenterId;

        LoggingUtil.info(logger, "雪花算法ID生成器初始化完成 - 机器ID: {}, 数据中心ID: {}",
                workerId, datacenterId);
    }

    // ==================== 核心方法 ====================

    /**
     * 生成下一个唯一ID
     *
     * @return 64位长整型唯一ID
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        // 检查时钟回拨
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            LoggingUtil.error(logger, "检测到时钟回拨 {}ms，拒绝生成ID", offset);
            throw new RuntimeException(
                    String.format("时钟回拨异常，拒绝生成ID。回拨时间: %dms", offset));
        }

        // 同一毫秒内，序列号递增
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // 序列号溢出，等待下一毫秒
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒，序列号重置为0
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 组装64位ID
        long id = ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;

        LoggingUtil.debug(logger, "生成ID: {}, 时间戳: {}, 数据中心ID: {}, 机器ID: {}, 序列号: {}",
                id, timestamp, datacenterId, workerId, sequence);

        return id;
    }

    /**
     * 生成用户ID
     * 为用户ID生成提供专门的方法，增强可读性
     *
     * @return 64位长整型用户ID
     */
    public long generateUserId() {
        long userId = nextId();
        LoggingUtil.info(logger, "生成用户ID: {}", userId);
        return userId;
    }

    /**
     * 批量生成用户ID
     *
     * @param count 生成数量
     * @return 用户ID数组
     */
    public long[] generateUserIds(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("生成数量必须大于0");
        }
        if (count > 1000) {
            throw new IllegalArgumentException("单次生成数量不能超过1000");
        }

        long[] userIds = new long[count];
        for (int i = 0; i < count; i++) {
            userIds[i] = nextId();
        }

        LoggingUtil.info(logger, "批量生成用户ID完成，数量: {}", count);
        return userIds;
    }

    // ==================== 辅助方法 ====================

    /**
     * 等待下一毫秒
     *
     * @param lastTimestamp 上次时间戳
     * @return 下一毫秒的时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 获取当前时间戳
     *
     * @return 当前毫秒时间戳
     */
    private long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * 自动获取机器ID
     * 基于MAC地址生成机器ID
     *
     * @return 机器ID（0-31）
     */
    private long getWorkerId() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);

            if (network == null) {
                LoggingUtil.warn(logger, "无法获取网络接口，使用默认机器ID: 1");
                return 1L;
            }

            byte[] mac = network.getHardwareAddress();
            if (mac == null) {
                LoggingUtil.warn(logger, "无法获取MAC地址，使用默认机器ID: 1");
                return 1L;
            }

            long id = ((0x000000FF & (long) mac[mac.length - 2])
                    | (0x0000FF00 & (((long) mac[mac.length - 1]) << 8))) >> 6;
            id = id % (MAX_WORKER_ID + 1);

            LoggingUtil.debug(logger, "自动获取机器ID: {}", id);
            return id;
        } catch (Exception e) {
            LoggingUtil.warn(logger, "获取机器ID失败，使用默认值: 1", e);
            return 1L;
        }
    }

    /**
     * 自动获取数据中心ID
     * 基于主机名生成数据中心ID
     *
     * @return 数据中心ID（0-31）
     */
    private long getDatacenterId() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            String hostName = ip.getHostName();

            if (hostName == null || hostName.isEmpty()) {
                LoggingUtil.warn(logger, "无法获取主机名，使用默认数据中心ID: 1");
                return 1L;
            }

            long id = hostName.hashCode() % (MAX_DATACENTER_ID + 1);
            if (id < 0) {
                id = -id;
            }

            LoggingUtil.debug(logger, "自动获取数据中心ID: {} (基于主机名: {})", id, hostName);
            return id;
        } catch (Exception e) {
            LoggingUtil.warn(logger, "获取数据中心ID失败，使用默认值: 1", e);
            return 1L;
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 解析ID中的时间戳
     *
     * @param id 雪花算法生成的ID
     * @return 时间戳（毫秒）
     */
    public static long parseTimestamp(long id) {
        return (id >> TIMESTAMP_LEFT_SHIFT) + EPOCH;
    }

    /**
     * 解析ID中的数据中心ID
     *
     * @param id 雪花算法生成的ID
     * @return 数据中心ID
     */
    public static long parseDatacenterId(long id) {
        return (id >> DATACENTER_ID_SHIFT) & MAX_DATACENTER_ID;
    }

    /**
     * 解析ID中的机器ID
     *
     * @param id 雪花算法生成的ID
     * @return 机器ID
     */
    public static long parseWorkerId(long id) {
        return (id >> WORKER_ID_SHIFT) & MAX_WORKER_ID;
    }

    /**
     * 解析ID中的序列号
     *
     * @param id 雪花算法生成的ID
     * @return 序列号
     */
    public static long parseSequence(long id) {
        return id & SEQUENCE_MASK;
    }

    /**
     * 格式化ID信息
     *
     * @param id 雪花算法生成的ID
     * @return 格式化的ID信息字符串
     */
    public static String formatId(long id) {
        return String.format("ID: %d, 时间戳: %d, 数据中心ID: %d, 机器ID: %d, 序列号: %d",
                id, parseTimestamp(id), parseDatacenterId(id), parseWorkerId(id), parseSequence(id));
    }

    // ==================== Getter方法 ====================

    /**
     * 获取机器ID
     *
     * @return 机器ID
     */
    public long getWorkerIdValue() {
        return workerId;
    }

    /**
     * 获取数据中心ID
     *
     * @return 数据中心ID
     */
    public long getDatacenterIdValue() {
        return datacenterId;
    }

    /**
     * 获取当前序列号
     *
     * @return 当前序列号
     */
    public long getCurrentSequence() {
        return sequence;
    }

    /**
     * 获取上次时间戳
     *
     * @return 上次时间戳
     */
    public long getLastTimestamp() {
        return lastTimestamp;
    }
}

