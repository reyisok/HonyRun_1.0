package com.honyrun.model.dto.reactive;

import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * 流式响应类
 *
 * 用于处理响应式流数据传输，支持Server-Sent Events和WebSocket等流式通信。
 * 该类提供了流式数据的封装和管理功能，支持背压控制和错误处理。
 *
 * 特性：
 * - 支持响应式流传输
 * - 背压控制
 * - 流式错误处理
 * - Server-Sent Events支持
 *
 * @author Mr.Rey
 * @since 2.0.0
 * @created 2025-07-01  16:50:00
 * @modified 2025-07-01 16:50:00
 * @version 2.0.0
 */
public class StreamingResponse<T> extends ReactiveResponse<Flux<T>> {

    private static final long serialVersionUID = 1L;

    /**
     * 流类型
     * SSE-Server-Sent Events, WEBSOCKET-WebSocket, STREAM-普通流
     */
    private StreamType streamType;

    /**
     * 流状态
     * STARTING-启动中, STREAMING-流式传输中, COMPLETED-已完成, ERROR-错误, CANCELLED-已取消
     */
    private StreamStatus streamStatus;

    /**
     * 流开始时间
     */
    private LocalDateTime streamStartTime;

    /**
     * 流结束时间
     */
    private LocalDateTime streamEndTime;

    /**
     * 已传输元素数量
     */
    private Long transmittedCount;

    /**
     * 总元素数量（如果已知）
     */
    private Long totalCount;

    /**
     * 流配置参数
     */
    private transient StreamConfig config;

    /**
     * 错误处理器
     */
    private transient Consumer<Throwable> errorHandler;

    /**
     * 完成处理器
     */
    private transient Runnable completionHandler;

    /**
     * 默认构造函数
     */
    public StreamingResponse() {
        super();
        this.streamStatus = StreamStatus.STARTING;
        this.streamStartTime = LocalDateTime.now();
        this.transmittedCount = 0L;
        this.config = new StreamConfig();
    }

    /**
     * 带流数据的构造函数
     *
     * @param dataStream 流数据
     * @param streamType 流类型
     */
    public StreamingResponse(Flux<T> dataStream, StreamType streamType) {
        super(dataStream);
        this.streamType = streamType;
        this.streamStatus = StreamStatus.STARTING;
        this.streamStartTime = LocalDateTime.now();
        this.transmittedCount = 0L;
        this.config = new StreamConfig();
    }

    /**
     * 获取流类型
     *
     * @return 流类型
     */
    public StreamType getStreamType() {
        return streamType;
    }

    /**
     * 设置流类型
     *
     * @param streamType 流类型
     */
    public void setStreamType(StreamType streamType) {
        this.streamType = streamType;
    }

    /**
     * 获取流状态
     *
     * @return 流状态
     */
    public StreamStatus getStreamStatus() {
        return streamStatus;
    }

    /**
     * 设置流状态
     *
     * @param streamStatus 流状态
     */
    public void setStreamStatus(StreamStatus streamStatus) {
        this.streamStatus = streamStatus;
    }

    /**
     * 获取流开始时间
     *
     * @return 流开始时间
     */
    public LocalDateTime getStreamStartTime() {
        return streamStartTime;
    }

    /**
     * 设置流开始时间
     *
     * @param streamStartTime 流开始时间
     */
    public void setStreamStartTime(LocalDateTime streamStartTime) {
        this.streamStartTime = streamStartTime;
    }

    /**
     * 获取流结束时间
     *
     * @return 流结束时间
     */
    public LocalDateTime getStreamEndTime() {
        return streamEndTime;
    }

    /**
     * 设置流结束时间
     *
     * @param streamEndTime 流结束时间
     */
    public void setStreamEndTime(LocalDateTime streamEndTime) {
        this.streamEndTime = streamEndTime;
    }

    /**
     * 获取已传输元素数量
     *
     * @return 已传输元素数量
     */
    public Long getTransmittedCount() {
        return transmittedCount;
    }

    /**
     * 设置已传输元素数量
     *
     * @param transmittedCount 已传输元素数量
     */
    public void setTransmittedCount(Long transmittedCount) {
        this.transmittedCount = transmittedCount;
    }

    /**
     * 获取总元素数量
     *
     * @return 总元素数量
     */
    public Long getTotalCount() {
        return totalCount;
    }

    /**
     * 设置总元素数量
     *
     * @param totalCount 总元素数量
     */
    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    /**
     * 获取流配置
     *
     * @return 流配置
     */
    public StreamConfig getConfig() {
        return config;
    }

    /**
     * 设置流配置
     *
     * @param config 流配置
     */
    public void setConfig(StreamConfig config) {
        this.config = config;
    }

    /**
     * 获取错误处理器
     *
     * @return 错误处理器
     */
    public Consumer<Throwable> getErrorHandler() {
        return errorHandler;
    }

    /**
     * 设置错误处理器
     *
     * @param errorHandler 错误处理器
     */
    public void setErrorHandler(Consumer<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * 获取完成处理器
     *
     * @return 完成处理器
     */
    public Runnable getCompletionHandler() {
        return completionHandler;
    }

    /**
     * 设置完成处理器
     *
     * @param completionHandler 完成处理器
     */
    public void setCompletionHandler(Runnable completionHandler) {
        this.completionHandler = completionHandler;
    }

    /**
     * 开始流传输
     */
    public void startStreaming() {
        this.streamStatus = StreamStatus.STREAMING;
        this.streamStartTime = LocalDateTime.now();
    }

    /**
     * 完成流传输
     */
    public void completeStreaming() {
        this.streamStatus = StreamStatus.COMPLETED;
        this.streamEndTime = LocalDateTime.now();
        if (completionHandler != null) {
            completionHandler.run();
        }
    }

    /**
     * 流传输出错
     *
     * @param error 错误信息
     */
    public void errorStreaming(Throwable error) {
        this.streamStatus = StreamStatus.ERROR;
        this.streamEndTime = LocalDateTime.now();
        this.setSuccess(false);
        this.setMessage("流传输出错: " + error.getMessage());
        if (errorHandler != null) {
            errorHandler.accept(error);
        }
    }

    /**
     * 取消流传输
     */
    public void cancelStreaming() {
        this.streamStatus = StreamStatus.CANCELLED;
        this.streamEndTime = LocalDateTime.now();
        this.setSuccess(false);
        this.setMessage("流传输已取消");
    }

    /**
     * 增加传输计数
     */
    public void incrementTransmittedCount() {
        if (this.transmittedCount == null) {
            this.transmittedCount = 0L;
        }
        this.transmittedCount++;
    }

    /**
     * 计算传输进度
     *
     * @return 传输进度百分比
     */
    public double getProgress() {
        if (totalCount == null || totalCount == 0 || transmittedCount == null) {
            return 0.0;
        }
        return (double) transmittedCount / totalCount * 100;
    }

    /**
     * 判断流是否正在传输
     *
     * @return 如果正在传输返回true，否则返回false
     */
    public boolean isStreaming() {
        return this.streamStatus == StreamStatus.STREAMING;
    }

    /**
     * 判断流是否已完成
     *
     * @return 如果已完成返回true，否则返回false
     */
    public boolean isCompleted() {
        return this.streamStatus == StreamStatus.COMPLETED;
    }

    /**
     * 判断流是否出错
     *
     * @return 如果出错返回true，否则返回false
     */
    public boolean isError() {
        return this.streamStatus == StreamStatus.ERROR;
    }

    /**
     * 获取流持续时间
     *
     * @return 流持续时间（毫秒）
     */
    public Long getStreamDuration() {
        if (streamStartTime == null) {
            return null;
        }
        LocalDateTime endTime = streamEndTime != null ? streamEndTime : LocalDateTime.now();
        return java.time.Duration.between(streamStartTime, endTime).toMillis();
    }

    /**
     * 创建SSE流响应
     *
     * @param dataStream 数据流
     * @param <T> 数据类型
     * @return SSE流响应
     */
    public static <T> StreamingResponse<T> sse(Flux<T> dataStream) {
        return new StreamingResponse<>(dataStream, StreamType.SSE);
    }

    /**
     * 创建WebSocket流响应
     *
     * @param dataStream 数据流
     * @param <T> 数据类型
     * @return WebSocket流响应
     */
    public static <T> StreamingResponse<T> websocket(Flux<T> dataStream) {
        return new StreamingResponse<>(dataStream, StreamType.WEBSOCKET);
    }

    /**
     * 创建普通流响应
     *
     * @param dataStream 数据流
     * @param <T> 数据类型
     * @return 普通流响应
     */
    public static <T> StreamingResponse<T> stream(Flux<T> dataStream) {
        return new StreamingResponse<>(dataStream, StreamType.STREAM);
    }

    /**
     * 流类型枚举
     */
    public enum StreamType {
        /** Server-Sent Events */
        SSE("Server-Sent Events"),
        /** WebSocket */
        WEBSOCKET("WebSocket"),
        /** 普通流 */
        STREAM("普通流");

        private final String description;

        StreamType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 流状态枚举
     */
    public enum StreamStatus {
        /** 启动中 */
        STARTING("启动中"),
        /** 流式传输中 */
        STREAMING("流式传输中"),
        /** 已完成 */
        COMPLETED("已完成"),
        /** 错误 */
        ERROR("错误"),
        /** 已取消 */
        CANCELLED("已取消");

        private final String description;

        StreamStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 流配置类
     */
    public static class StreamConfig {
        /** 缓冲区大小 */
        private int bufferSize = 256;
        /** 背压策略 */
        private String backpressureStrategy = "BUFFER";
        /** 超时时间（毫秒） */
        private long timeoutMs = 30000;
        /** 是否启用压缩 */
        private boolean compressionEnabled = false;

        public int getBufferSize() {
            return bufferSize;
        }

        public void setBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
        }

        public String getBackpressureStrategy() {
            return backpressureStrategy;
        }

        public void setBackpressureStrategy(String backpressureStrategy) {
            this.backpressureStrategy = backpressureStrategy;
        }

        public long getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
        }

        public boolean isCompressionEnabled() {
            return compressionEnabled;
        }

        public void setCompressionEnabled(boolean compressionEnabled) {
            this.compressionEnabled = compressionEnabled;
        }
    }

    @Override
    public String toString() {
        return String.format("StreamingResponse{type=%s, status=%s, transmitted=%d, total=%d, progress=%.2f%%}",
                streamType, streamStatus, transmittedCount, totalCount, getProgress());
    }
}


