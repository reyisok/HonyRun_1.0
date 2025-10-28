package com.honyrun.util.validation;

import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 响应式验证器
 *
 * 提供响应式数据验证功能，支持Bean Validation和自定义验证规则
 * 采用非阻塞模式进行数据校验，支持复杂的业务验证逻辑
 *
 * 主要功能：
 * - Bean Validation集成
 * - 自定义验证规则
 * - 批量数据验证
 * - 条件验证
 * - 异步验证
 *
 * 响应式特性：
 * - 非阻塞验证：所有验证操作均为非阻塞
 * - 流式处理：支持数据流的验证
 * - 错误收集：收集并返回详细的验证错误信息
 * - 验证链：支持多个验证规则的链式组合
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01 00:25:00
 * @modified 2025-07-01 00:25:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
@Component
public class ReactiveValidator {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveValidator.class);

    private final Validator validator;

    public ReactiveValidator(Validator validator) {
        this.validator = validator;
    }

    /**
     * 验证单个对象
     *
     * @param object 待验证对象的Mono
     * @param <T> 对象类型
     * @return 验证结果的Mono
     */
    public <T> Mono<ValidationResult<T>> validate(Mono<T> object) {
        LoggingUtil.debug(logger, "开始执行单个对象验证");

        return object
                .map(obj -> {
                    Set<ConstraintViolation<T>> violations = validator.validate(obj);
                    ValidationResult<T> result = new ValidationResult<>();
                    result.setObject(obj);
                    result.setValid(violations.isEmpty());
                    result.setViolations(violations);
                    return result;
                })
                .doOnSuccess(result -> LoggingUtil.debug(logger, "单个对象验证完成，有效: {}", result.isValid()))
                .doOnError(error -> LoggingUtil.error(logger, "单个对象验证失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "验证过程出错，返回无效结果", error);
                    return Mono.just(ValidationResult.invalid(null, error.getMessage()));
                });
    }

    /**
     * 验证对象流
     *
     * @param objects 待验证对象的Flux
     * @param <T> 对象类型
     * @return 验证结果的Flux
     */
    public <T> Flux<ValidationResult<T>> validateStream(Flux<T> objects) {
        LoggingUtil.debug(logger, "开始执行对象流验证");

        return objects
                .map(obj -> {
                    Set<ConstraintViolation<T>> violations = validator.validate(obj);
                    ValidationResult<T> result = new ValidationResult<>();
                    result.setObject(obj);
                    result.setValid(violations.isEmpty());
                    result.setViolations(violations);
                    return result;
                })
                .doOnNext(result -> LoggingUtil.debug(logger, "对象流验证处理一个对象，有效: {}", result.isValid()))
                .doOnComplete(() -> LoggingUtil.debug(logger, "对象流验证完成"))
                .doOnError(error -> LoggingUtil.error(logger, "对象流验证失败", error))
                .onErrorContinue((error, obj) -> {
                    LoggingUtil.error(logger, "验证单个对象时出错: {}", obj, error);
                });
    }

    /**
     * 自定义验证
     *
     * @param object 待验证对象的Mono
     * @param predicate 验证谓词
     * @param errorMessage 错误消息
     * @param <T> 对象类型
     * @return 验证结果的Mono
     */
    public <T> Mono<ValidationResult<T>> validateCustom(Mono<T> object, Predicate<T> predicate, String errorMessage) {
        LoggingUtil.debug(logger, "开始执行自定义验证");

        return object
                .map(obj -> {
                    ValidationResult<T> result = new ValidationResult<>();
                    result.setObject(obj);
                    result.setValid(predicate.test(obj));
                    if (!result.isValid()) {
                        result.addError(errorMessage);
                    }
                    return result;
                })
                .doOnSuccess(result -> LoggingUtil.debug(logger, "自定义验证完成，有效: {}", result.isValid()))
                .doOnError(error -> LoggingUtil.error(logger, "自定义验证失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "自定义验证过程出错", error);
                    return Mono.just(ValidationResult.invalid(null, error.getMessage()));
                });
    }

    /**
     * 条件验证
     *
     * @param object 待验证对象的Mono
     * @param condition 验证条件
     * @param predicate 验证谓词
     * @param errorMessage 错误消息
     * @param <T> 对象类型
     * @return 验证结果的Mono
     */
    public <T> Mono<ValidationResult<T>> validateIf(Mono<T> object, Predicate<T> condition,
                                                   Predicate<T> predicate, String errorMessage) {
        LoggingUtil.debug(logger, "开始执行条件验证");

        return object
                .map(obj -> {
                    ValidationResult<T> result = new ValidationResult<>();
                    result.setObject(obj);

                    if (condition.test(obj)) {
                        result.setValid(predicate.test(obj));
                        if (!result.isValid()) {
                            result.addError(errorMessage);
                        }
                    } else {
                        result.setValid(true); // 条件不满足时默认有效
                    }

                    return result;
                })
                .doOnSuccess(result -> LoggingUtil.debug(logger, "条件验证完成，有效: {}", result.isValid()))
                .doOnError(error -> LoggingUtil.error(logger, "条件验证失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "条件验证过程出错", error);
                    return Mono.just(ValidationResult.invalid(null, error.getMessage()));
                });
    }

    /**
     * 异步验证
     *
     * @param object 待验证对象的Mono
     * @param asyncValidator 异步验证函数
     * @param <T> 对象类型
     * @return 验证结果的Mono
     */
    public <T> Mono<ValidationResult<T>> validateAsync(Mono<T> object, Function<T, Mono<Boolean>> asyncValidator) {
        LoggingUtil.debug(logger, "开始执行异步验证");

        return object
                .flatMap(obj ->
                    asyncValidator.apply(obj)
                            .map(isValid -> {
                                ValidationResult<T> result = new ValidationResult<>();
                                result.setObject(obj);
                                result.setValid(isValid);
                                if (!isValid) {
                                    result.addError("异步验证失败");
                                }
                                return result;
                            })
                )
                .doOnSuccess(result -> LoggingUtil.debug(logger, "异步验证完成，有效: {}", result.isValid()))
                .doOnError(error -> LoggingUtil.error(logger, "异步验证失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "异步验证过程出错", error);
                    return Mono.just(ValidationResult.invalid(null, error.getMessage()));
                });
    }

    /**
     * 批量验证
     *
     * @param objects 待验证对象列表的Mono
     * @param <T> 对象类型
     * @return 批量验证结果的Mono
     */
    public <T> Mono<BatchValidationResult<T>> validateBatch(Mono<List<T>> objects) {
        LoggingUtil.debug(logger, "开始执行批量验证");

        return objects
                .flatMapMany(Flux::fromIterable)
                .flatMap(obj -> validate(Mono.just(obj)))
                .collectList()
                .map(results -> {
                    BatchValidationResult<T> batchResult = new BatchValidationResult<>();
                    batchResult.setResults(results);
                    batchResult.setTotalCount(results.size());
                    batchResult.setValidCount((int) results.stream().filter(ValidationResult::isValid).count());
                    batchResult.setInvalidCount(batchResult.getTotalCount() - batchResult.getValidCount());
                    return batchResult;
                })
                .doOnSuccess(result -> LoggingUtil.debug(logger, "批量验证完成，总数: {}, 有效: {}, 无效: {}",
                        result.getTotalCount(), result.getValidCount(), result.getInvalidCount()))
                .doOnError(error -> LoggingUtil.error(logger, "批量验证失败", error));
    }

    /**
     * 验证链
     *
     * @param object 待验证对象的Mono
     * @param validators 验证器链
     * @param <T> 对象类型
     * @return 验证结果的Mono
     */
    @SafeVarargs
    public final <T> Mono<ValidationResult<T>> validateChain(Mono<T> object, Function<T, Boolean>... validators) {
        LoggingUtil.debug(logger, "开始执行验证链，验证器数量: {}", validators.length);

        return object
                .map(obj -> {
                    ValidationResult<T> result = new ValidationResult<>();
                    result.setObject(obj);
                    result.setValid(true);

                    for (int i = 0; i < validators.length; i++) {
                        try {
                            if (!validators[i].apply(obj)) {
                                result.setValid(false);
                                result.addError("验证器" + (i + 1) + "验证失败");
                                break; // 短路验证
                            }
                        } catch (Exception e) {
                            LoggingUtil.error(logger, "验证器{}执行失败", i + 1, e);
                            result.setValid(false);
                            result.addError("验证器" + (i + 1) + "执行异常: " + e.getMessage());
                            break;
                        }
                    }

                    return result;
                })
                .doOnSuccess(result -> LoggingUtil.debug(logger, "验证链完成，有效: {}", result.isValid()))
                .doOnError(error -> LoggingUtil.error(logger, "验证链失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "验证链过程出错", error);
                    return Mono.just(ValidationResult.invalid(null, error.getMessage()));
                });
    }

    /**
     * 分组验证
     *
     * @param object 待验证对象的Mono
     * @param groups 验证分组
     * @param <T> 对象类型
     * @return 验证结果的Mono
     */
    public <T> Mono<ValidationResult<T>> validateGroups(Mono<T> object, Class<?>... groups) {
        LoggingUtil.debug(logger, "开始执行分组验证，分组数量: {}", groups.length);

        return object
                .map(obj -> {
                    Set<ConstraintViolation<T>> violations = validator.validate(obj, groups);
                    ValidationResult<T> result = new ValidationResult<>();
                    result.setObject(obj);
                    result.setValid(violations.isEmpty());
                    result.setViolations(violations);
                    return result;
                })
                .doOnSuccess(result -> LoggingUtil.debug(logger, "分组验证完成，有效: {}", result.isValid()))
                .doOnError(error -> LoggingUtil.error(logger, "分组验证失败", error))
                .onErrorResume(error -> {
                    LoggingUtil.error(logger, "分组验证过程出错", error);
                    return Mono.just(ValidationResult.invalid(null, error.getMessage()));
                });
    }

    /**
     * 验证结果类
     */
    public static class ValidationResult<T> {
        private T object;
        private boolean valid;
        private Set<ConstraintViolation<T>> violations;
        private List<String> errors;

        public ValidationResult() {
            this.errors = new java.util.ArrayList<>();
        }

        public static <T> ValidationResult<T> invalid(T object, String errorMessage) {
            ValidationResult<T> result = new ValidationResult<>();
            result.setObject(object);
            result.setValid(false);
            result.addError(errorMessage);
            return result;
        }

        public static <T> ValidationResult<T> valid(T object) {
            ValidationResult<T> result = new ValidationResult<>();
            result.setObject(object);
            result.setValid(true);
            return result;
        }

        // Getters and Setters
        public T getObject() {
            return object;
        }

        public void setObject(T object) {
            this.object = object;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public Set<ConstraintViolation<T>> getViolations() {
            return violations == null ? null : new HashSet<>(violations);
        }

        public void setViolations(Set<ConstraintViolation<T>> violations) {
            this.violations = violations == null ? null : new HashSet<>(violations);
        }

        public List<String> getErrors() {
            return errors == null ? null : new ArrayList<>(errors);
        }

        public void setErrors(List<String> errors) {
            this.errors = errors == null ? null : new ArrayList<>(errors);
        }

        public void addError(String error) {
            this.errors.add(error);
        }

        /**
         * 获取所有错误消息
         *
         * @return 错误消息列表
         */
        public List<String> getAllErrorMessages() {
            List<String> allErrors = new java.util.ArrayList<>(errors);
            if (violations != null) {
                violations.forEach(violation -> allErrors.add(violation.getMessage()));
            }
            return allErrors;
        }
    }

    /**
     * 批量验证结果类
     */
    public static class BatchValidationResult<T> {
        private List<ValidationResult<T>> results;
        private int totalCount;
        private int validCount;
        private int invalidCount;

        // Getters and Setters
        public List<ValidationResult<T>> getResults() {
            return results;
        }

        public void setResults(List<ValidationResult<T>> results) {
            this.results = results;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public int getValidCount() {
            return validCount;
        }

        public void setValidCount(int validCount) {
            this.validCount = validCount;
        }

        public int getInvalidCount() {
            return invalidCount;
        }

        public void setInvalidCount(int invalidCount) {
            this.invalidCount = invalidCount;
        }

        /**
         * 获取有效率
         *
         * @return 有效率百分比
         */
        public double getValidRate() {
            return totalCount > 0 ? (double) validCount / totalCount * 100 : 0.0;
        }

        /**
         * 获取所有无效对象
         *
         * @return 无效对象列表
         */
        public List<ValidationResult<T>> getInvalidResults() {
            return results.stream()
                    .filter(result -> !result.isValid())
                    .collect(java.util.stream.Collectors.toList());
        }

        /**
         * 获取所有有效对象
         *
         * @return 有效对象列表
         */
        public List<ValidationResult<T>> getValidResults() {
            return results.stream()
                    .filter(ValidationResult::isValid)
                    .collect(java.util.stream.Collectors.toList());
        }
    }
}

