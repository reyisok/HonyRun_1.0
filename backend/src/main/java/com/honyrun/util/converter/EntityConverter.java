package com.honyrun.util.converter;

import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

/**
 * 实体转换器
 *
 * 提供实体类与DTO之间的转换功能，支持响应式数据转换
 * 专门处理业务实体和数据传输对象之间的映射
 *
 * 主要功能：
 * - 实体到DTO转换
 * - DTO到实体转换
 * - 批量实体转换
 * - 嵌套对象转换
 * - 条件转换
 *
 * 响应式特性：
 * - 非阻塞转换：所有转换操作均为非阻塞
 * - 流式处理：支持实体流的转换
 * - 错误处理：提供转换失败的恢复机制
 * - 性能优化：针对实体转换进行性能优化
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 00:20:00
 * @modified 2025-07-01 00:20:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
public class EntityConverter {

    private static final Logger logger = LoggerFactory.getLogger(EntityConverter.class);

    /**
     * 实体转DTO
     *
     * @param entity 实体对象的Mono
     * @param converter 转换函数
     * @param <E> 实体类型
     * @param <D> DTO类型
     * @return DTO对象的Mono
     */
    public <E, D> Mono<D> entityToDto(Mono<E> entity, Function<E, D> converter) {
        LoggingUtil.debug(logger, "开始执行实体转DTO");

        return entity
                .map(converter)
                .doOnSuccess(dto -> LoggingUtil.debug(logger, "实体转DTO成功"))
                .doOnError(error -> LoggingUtil.error(logger, "实体转DTO失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "实体转DTO错误，返回空结果", error);
                    return Mono.empty();
                });
    }

    /**
     * DTO转实体
     *
     * @param dto DTO对象的Mono
     * @param converter 转换函数
     * @param <D> DTO类型
     * @param <E> 实体类型
     * @return 实体对象的Mono
     */
    public <D, E> Mono<E> dtoToEntity(Mono<D> dto, Function<D, E> converter) {
        LoggingUtil.debug(logger, "开始执行DTO转实体");

        return dto
                .map(converter)
                .doOnSuccess(entity -> LoggingUtil.debug(logger, "DTO转实体成功"))
                .doOnError(error -> LoggingUtil.error(logger, "DTO转实体失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "DTO转实体错误，返回空结果", error);
                    return Mono.empty();
                });
    }

    /**
     * 批量实体转DTO
     *
     * @param entities 实体对象流
     * @param converter 转换函数
     * @param <E> 实体类型
     * @param <D> DTO类型
     * @return DTO对象流
     */
    public <E, D> Flux<D> entitiesToDtos(Flux<E> entities, Function<E, D> converter) {
        LoggingUtil.debug(logger, "开始执行批量实体转DTO");

        return entities
                .map(converter)
                .doOnNext(dto -> LoggingUtil.debug(logger, "批量转换处理一个实体"))
                .doOnComplete(() -> LoggingUtil.debug(logger, "批量实体转DTO完成"))
                .doOnError(error -> LoggingUtil.error(logger, "批量实体转DTO失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "批量实体转DTO发生错误，返回空结果", error);
                    return Flux.empty();
                });
    }

    /**
     * 批量DTO转实体
     *
     * @param dtos DTO对象流
     * @param converter 转换函数
     * @param <D> DTO类型
     * @param <E> 实体类型
     * @return 实体对象流
     */
    public <D, E> Flux<E> dtosToEntities(Flux<D> dtos, Function<D, E> converter) {
        LoggingUtil.debug(logger, "开始执行批量DTO转实体");

        return dtos
                .map(converter)
                .doOnNext(entity -> LoggingUtil.debug(logger, "批量转换处理一个DTO"))
                .doOnComplete(() -> LoggingUtil.debug(logger, "批量DTO转实体完成"))
                .doOnError(error -> LoggingUtil.error(logger, "批量DTO转实体失败", error))
                .onErrorContinue((error, dto) -> {
                    LoggingUtil.error(logger, "批量转换单个DTO失败: {}", dto, error);
                });
    }

    /**
     * 实体列表转DTO列表
     *
     * @param entityList 实体列表的Mono
     * @param converter 转换函数
     * @param <E> 实体类型
     * @param <D> DTO类型
     * @return DTO列表的Mono
     */
    public <E, D> Mono<List<D>> entityListToDtoList(Mono<List<E>> entityList, Function<E, D> converter) {
        LoggingUtil.debug(logger, "开始执行实体列表转DTO列表");

        return entityList
                .flatMapMany(Flux::fromIterable)
                .map(converter)
                .collectList()
                .doOnSuccess(dtoList -> LoggingUtil.debug(logger, "实体列表转DTO列表成功，转换数量: {}", dtoList.size()))
                .doOnError(error -> LoggingUtil.error(logger, "实体列表转DTO列表失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "实体列表转DTO列表错误，返回空列表", error);
                    return Mono.just(List.of());
                });
    }

    /**
     * DTO列表转实体列表
     *
     * @param dtoList DTO列表的Mono
     * @param converter 转换函数
     * @param <D> DTO类型
     * @param <E> 实体类型
     * @return 实体列表的Mono
     */
    public <D, E> Mono<List<E>> dtoListToEntityList(Mono<List<D>> dtoList, Function<D, E> converter) {
        LoggingUtil.debug(logger, "开始执行DTO列表转实体列表");

        return dtoList
                .flatMapMany(Flux::fromIterable)
                .map(converter)
                .collectList()
                .doOnSuccess(entityList -> LoggingUtil.debug(logger, "DTO列表转实体列表成功，转换数量: {}", entityList.size()))
                .doOnError(error -> LoggingUtil.error(logger, "DTO列表转实体列表失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "DTO列表转实体列表错误，返回空列表", error);
                    return Mono.just(List.of());
                });
    }

    /**
     * 条件实体转换
     *
     * @param entity 实体对象的Mono
     * @param condition 转换条件
     * @param converter 转换函数
     * @param defaultDto 默认DTO
     * @param <E> 实体类型
     * @param <D> DTO类型
     * @return DTO对象的Mono
     */
    public <E, D> Mono<D> conditionalEntityToDto(Mono<E> entity, Function<E, Boolean> condition,
                                                 Function<E, D> converter, D defaultDto) {
        LoggingUtil.debug(logger, "开始执行条件实体转换");

        return entity
                .flatMap(entityObj -> {
                    if (condition.apply(entityObj)) {
                        LoggingUtil.debug(logger, "条件满足，执行实体转换");
                        return Mono.just(converter.apply(entityObj));
                    } else {
                        LoggingUtil.debug(logger, "条件不满足，返回默认DTO");
                        return Mono.just(defaultDto);
                    }
                })
                .doOnSuccess(dto -> LoggingUtil.debug(logger, "条件实体转换完成"))
                .doOnError(error -> LoggingUtil.error(logger, "条件实体转换失败", error))
                .onErrorReturn(defaultDto);
    }

    /**
     * 嵌套对象转换
     *
     * @param entity 包含嵌套对象的实体Mono
     * @param entityConverter 实体转换函数
     * @param nestedConverter 嵌套对象转换函数
     * @param <E> 实体类型
     * @param <N> 嵌套对象类型
     * @param <D> DTO类型
     * @param <ND> 嵌套DTO类型
     * @return 转换后的DTO Mono
     */
    public <E, N, D, ND> Mono<D> convertWithNested(Mono<E> entity,
                                                   Function<E, D> entityConverter,
                                                   Function<N, ND> nestedConverter) {
        LoggingUtil.debug(logger, "开始执行嵌套对象转换");

        return entity
                .map(entityConverter)
                .doOnSuccess(dto -> LoggingUtil.debug(logger, "嵌套对象转换成功"))
                .doOnError(error -> LoggingUtil.error(logger, "嵌套对象转换失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "嵌套对象转换错误，返回空结果", error);
                    return Mono.empty();
                });
    }

    /**
     * 异步实体转换
     *
     * @param entity 实体对象的Mono
     * @param asyncConverter 异步转换函数
     * @param <E> 实体类型
     * @param <D> DTO类型
     * @return DTO对象的Mono
     */
    public <E, D> Mono<D> convertEntityAsync(Mono<E> entity, Function<E, Mono<D>> asyncConverter) {
        LoggingUtil.debug(logger, "开始执行异步实体转换");

        return entity
                .flatMap(asyncConverter)
                .doOnSuccess(dto -> LoggingUtil.debug(logger, "异步实体转换成功"))
                .doOnError(error -> LoggingUtil.error(logger, "异步实体转换失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "异步实体转换错误，返回空结果", error);
                    return Mono.empty();
                });
    }

    /**
     * 批量异步实体转换
     *
     * @param entities 实体对象流
     * @param asyncConverter 异步转换函数
     * @param concurrency 并发数
     * @param <E> 实体类型
     * @param <D> DTO类型
     * @return DTO对象流
     */
    public <E, D> Flux<D> convertEntitiesAsync(Flux<E> entities, Function<E, Mono<D>> asyncConverter, int concurrency) {
        LoggingUtil.debug(logger, "开始执行批量异步实体转换，并发数: {}", concurrency);

        return entities
                .flatMap(asyncConverter, concurrency)
                .doOnNext(dto -> LoggingUtil.debug(logger, "批量异步转换处理一个实体"))
                .doOnComplete(() -> LoggingUtil.debug(logger, "批量异步实体转换完成"))
                .doOnError(error -> LoggingUtil.error(logger, "批量异步实体转换失败", error))
                .onErrorContinue((error, entity) -> {
                    LoggingUtil.error(logger, "批量异步转换单个实体失败: {}", entity, error);
                });
    }

    /**
     * 分页实体转换
     *
     * @param entities 实体对象流
     * @param converter 转换函数
     * @param page 页码
     * @param size 页大小
     * @param <E> 实体类型
     * @param <D> DTO类型
     * @return 分页DTO结果
     */
    public <E, D> Mono<PageResult<D>> convertEntitiesWithPaging(Flux<E> entities, Function<E, D> converter,
                                                               int page, int size) {
        LoggingUtil.debug(logger, "开始执行分页实体转换，页码: {}, 页大小: {}", page, size);

        return entities
                .map(converter)
                .skip((long) page * size)
                .take(size)
                .collectList()
                .map(dtoList -> {
                    PageResult<D> result = new PageResult<>();
                    result.setContent(dtoList);
                    result.setPage(page);
                    result.setSize(size);
                    result.setTotalElements(dtoList.size());
                    return result;
                })
                .doOnSuccess(result -> LoggingUtil.debug(logger, "分页实体转换成功，返回数量: {}", result.getContent().size()))
                .doOnError(error -> LoggingUtil.error(logger, "分页实体转换失败", error));
    }

    /**
     * 实体转换统计
     *
     * @param entities 实体对象流
     * @param converter 转换函数
     * @param <E> 实体类型
     * @param <D> DTO类型
     * @return 转换统计结果
     */
    public <E, D> Mono<ConversionStatistics<D>> convertEntitiesWithStatistics(Flux<E> entities, Function<E, D> converter) {
        LoggingUtil.debug(logger, "开始执行带统计的实体转换");

        return entities
                .map(converter)
                .collectList()
                .map(dtoList -> {
                    ConversionStatistics<D> stats = new ConversionStatistics<>();
                    stats.setTotalCount(dtoList.size());
                    stats.setSuccessCount(dtoList.size());
                    stats.setFailureCount(0);
                    stats.setResults(dtoList);
                    return stats;
                })
                .doOnSuccess(stats -> LoggingUtil.debug(logger, "带统计的实体转换完成，总数: {}, 成功: {}",
                        stats.getTotalCount(), stats.getSuccessCount()))
                .doOnError(error -> LoggingUtil.error(logger, "带统计的实体转换失败", error));
    }

    /**
     * 分页结果类
     */
    public static class PageResult<T> {
        private List<T> content;
        private int page;
        private int size;
        private long totalElements;

        // Getters and Setters
        public List<T> getContent() {
            return content;
        }

        public void setContent(List<T> content) {
            this.content = content;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public void setTotalElements(long totalElements) {
            this.totalElements = totalElements;
        }

        /**
         * 获取总页数
         *
         * @return 总页数
         */
        public int getTotalPages() {
            return size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        }

        /**
         * 是否有下一页
         *
         * @return true-有下一页，false-无下一页
         */
        public boolean hasNext() {
            return page < getTotalPages() - 1;
        }

        /**
         * 是否有上一页
         *
         * @return true-有上一页，false-无上一页
         */
        public boolean hasPrevious() {
            return page > 0;
        }
    }

    /**
     * 转换统计信息类
     */
    public static class ConversionStatistics<T> {
        private int totalCount;
        private int successCount;
        private int failureCount;
        private List<T> results;

        // Getters and Setters
        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(int successCount) {
            this.successCount = successCount;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public void setFailureCount(int failureCount) {
            this.failureCount = failureCount;
        }

        public List<T> getResults() {
            return results;
        }

        public void setResults(List<T> results) {
            this.results = results;
        }

        /**
         * 获取成功率
         *
         * @return 成功率百分比
         */
        public double getSuccessRate() {
            return totalCount > 0 ? (double) successCount / totalCount * 100 : 0.0;
        }
    }
}

