package com.honyrun.util.common;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 集合工具类
 * 提供集合操作、转换、过滤工具
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  16:40:00
 * @modified 2025-07-01 16:40:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public final class CollectionUtil {

    private CollectionUtil() {
        // 工具类，禁止实例化
    }

    // ==================== 基础判断方法 ====================

    /**
     * 判断集合是否为空或null
     *
     * @param collection 集合
     * @return 是否为空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断集合是否不为空
     *
     * @param collection 集合
     * @return 是否不为空
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * 判断Map是否为空或null
     *
     * @param map Map对象
     * @return 是否为空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 判断Map是否不为空
     *
     * @param map Map对象
     * @return 是否不为空
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * 判断数组是否为空或null
     *
     * @param array 数组
     * @return 是否为空
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 判断数组是否不为空
     *
     * @param array 数组
     * @return 是否不为空
     */
    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }

    // ==================== 安全获取方法 ====================

    /**
     * 安全获取集合大小
     *
     * @param collection 集合
     * @return 集合大小
     */
    public static int size(Collection<?> collection) {
        return collection != null ? collection.size() : 0;
    }

    /**
     * 安全获取Map大小
     *
     * @param map Map对象
     * @return Map大小
     */
    public static int size(Map<?, ?> map) {
        return map != null ? map.size() : 0;
    }

    /**
     * 安全获取数组长度
     *
     * @param array 数组
     * @return 数组长度
     */
    public static int size(Object[] array) {
        return array != null ? array.length : 0;
    }

    /**
     * 安全获取List中指定索引的元素
     *
     * @param list List对象
     * @param index 索引
     * @param <T> 泛型类型
     * @return 元素，如果索引越界返回null
     */
    public static <T> T get(List<T> list, int index) {
        if (list == null || index < 0 || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    /**
     * 安全获取List中第一个元素
     *
     * @param list List对象
     * @param <T> 泛型类型
     * @return 第一个元素，如果为空返回null
     */
    public static <T> T getFirst(List<T> list) {
        return get(list, 0);
    }

    /**
     * 安全获取List中最后一个元素
     *
     * @param list List对象
     * @param <T> 泛型类型
     * @return 最后一个元素，如果为空返回null
     */
    public static <T> T getLast(List<T> list) {
        return isEmpty(list) ? null : list.get(list.size() - 1);
    }

    // ==================== 创建方法 ====================

    /**
     * 创建ArrayList
     *
     * @param elements 元素数组
     * @param <T> 泛型类型
     * @return ArrayList实例
     */
    public static <T> List<T> newArrayList(T[] elements) {
        List<T> list = new ArrayList<>();
        if (elements != null) {
            Collections.addAll(list, elements);
        }
        return list;
    }

    /**
     * 创建ArrayList
     *
     * @param <T> 泛型类型
     * @return 空的ArrayList实例
     */
    public static <T> List<T> newArrayList() {
        return new ArrayList<>();
    }

    /**
     * 创建LinkedList
     *
     * @param elements 元素数组
     * @param <T> 泛型类型
     * @return LinkedList实例
     */
    public static <T> List<T> newLinkedList(T[] elements) {
        List<T> list = new LinkedList<>();
        if (elements != null) {
            Collections.addAll(list, elements);
        }
        return list;
    }

    /**
     * 创建LinkedList
     *
     * @param <T> 泛型类型
     * @return 空的LinkedList实例
     */
    public static <T> List<T> newLinkedList() {
        return new LinkedList<>();
    }

    /**
     * 创建HashSet
     *
     * @param elements 元素数组
     * @param <T> 泛型类型
     * @return HashSet实例
     */
    public static <T> Set<T> newHashSet(T[] elements) {
        Set<T> set = new HashSet<>();
        if (elements != null) {
            Collections.addAll(set, elements);
        }
        return set;
    }

    /**
     * 创建HashSet
     *
     * @param <T> 泛型类型
     * @return 空的HashSet实例
     */
    public static <T> Set<T> newHashSet() {
        return new HashSet<>();
    }

    /**
     * 创建LinkedHashSet
     *
     * @param elements 元素数组
     * @param <T> 泛型类型
     * @return LinkedHashSet实例
     */
    public static <T> Set<T> newLinkedHashSet(T[] elements) {
        Set<T> set = new LinkedHashSet<>();
        if (elements != null) {
            Collections.addAll(set, elements);
        }
        return set;
    }

    /**
     * 创建LinkedHashSet
     *
     * @param <T> 泛型类型
     * @return 空的LinkedHashSet实例
     */
    public static <T> Set<T> newLinkedHashSet() {
        return new LinkedHashSet<>();
    }

    /**
     * 创建HashMap
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @return HashMap实例
     */
    public static <K, V> Map<K, V> newHashMap() {
        return new HashMap<>();
    }

    /**
     * 创建LinkedHashMap
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @return LinkedHashMap实例
     */
    public static <K, V> Map<K, V> newLinkedHashMap() {
        return new LinkedHashMap<>();
    }

    // ==================== 转换方法 ====================

    /**
     * 数组转List
     *
     * @param array 数组
     * @param <T> 泛型类型
     * @return List对象
     */
    public static <T> List<T> toList(T[] array) {
        if (isEmpty(array)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(array));
    }

    /**
     * 集合转数组
     *
     * @param collection 集合
     * @param clazz 数组元素类型
     * @param <T> 泛型类型
     * @return 数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Collection<T> collection, Class<T> clazz) {
        if (isEmpty(collection)) {
            return (T[]) java.lang.reflect.Array.newInstance(clazz, 0);
        }
        return collection.toArray((T[]) java.lang.reflect.Array.newInstance(clazz, collection.size()));
    }

    /**
     * Set转List
     *
     * @param set Set对象
     * @param <T> 泛型类型
     * @return List对象
     */
    public static <T> List<T> toList(Set<T> set) {
        if (isEmpty(set)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(set);
    }

    /**
     * List转Set
     *
     * @param list List对象
     * @param <T> 泛型类型
     * @return Set对象
     */
    public static <T> Set<T> toSet(List<T> list) {
        if (isEmpty(list)) {
            return new HashSet<>();
        }
        return new HashSet<>(list);
    }

    // ==================== 过滤方法 ====================

    /**
     * 过滤集合
     *
     * @param collection 原集合
     * @param predicate 过滤条件
     * @param <T> 泛型类型
     * @return 过滤后的List
     */
    public static <T> List<T> filter(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) {
            return new ArrayList<>();
        }
        return collection.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * 过滤并去重
     *
     * @param collection 原集合
     * @param predicate 过滤条件
     * @param <T> 泛型类型
     * @return 过滤后的Set
     */
    public static <T> Set<T> filterToSet(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) {
            return new HashSet<>();
        }
        return collection.stream()
                .filter(predicate)
                .collect(Collectors.toSet());
    }

    /**
     * 过滤非空元素
     *
     * @param collection 原集合
     * @param <T> 泛型类型
     * @return 过滤后的List
     */
    public static <T> List<T> filterNotNull(Collection<T> collection) {
        return filter(collection, Objects::nonNull);
    }

    /**
     * 过滤非空白字符串
     *
     * @param collection 字符串集合
     * @return 过滤后的List
     */
    public static List<String> filterNotBlank(Collection<String> collection) {
        return filter(collection, StringUtil::isNotBlank);
    }

    // ==================== 映射方法 ====================

    /**
     * 映射集合
     *
     * @param collection 原集合
     * @param mapper 映射函数
     * @param <T> 原类型
     * @param <R> 目标类型
     * @return 映射后的List
     */
    public static <T, R> List<R> map(Collection<T> collection, Function<T, R> mapper) {
        if (isEmpty(collection) || mapper == null) {
            return new ArrayList<>();
        }
        return collection.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * 映射集合并去重
     *
     * @param collection 原集合
     * @param mapper 映射函数
     * @param <T> 原类型
     * @param <R> 目标类型
     * @return 映射后的Set
     */
    public static <T, R> Set<R> mapToSet(Collection<T> collection, Function<T, R> mapper) {
        if (isEmpty(collection) || mapper == null) {
            return new HashSet<>();
        }
        return collection.stream()
                .map(mapper)
                .collect(Collectors.toSet());
    }

    /**
     * 映射为Map
     *
     * @param collection 原集合
     * @param keyMapper 键映射函数
     * @param valueMapper 值映射函数
     * @param <T> 原类型
     * @param <K> 键类型
     * @param <V> 值类型
     * @return Map对象
     */
    public static <T, K, V> Map<K, V> toMap(Collection<T> collection,
                                            Function<T, K> keyMapper,
                                            Function<T, V> valueMapper) {
        if (isEmpty(collection) || keyMapper == null || valueMapper == null) {
            return new HashMap<>();
        }
        return collection.stream()
                .collect(Collectors.toMap(keyMapper, valueMapper, (v1, v2) -> v2));
    }

    /**
     * 按键分组
     *
     * @param collection 原集合
     * @param keyMapper 键映射函数
     * @param <T> 元素类型
     * @param <K> 键类型
     * @return 分组后的Map
     */
    public static <T, K> Map<K, List<T>> groupBy(Collection<T> collection, Function<T, K> keyMapper) {
        if (isEmpty(collection) || keyMapper == null) {
            return new HashMap<>();
        }
        return collection.stream()
                .collect(Collectors.groupingBy(keyMapper));
    }

    // ==================== 查找方法 ====================

    /**
     * 查找第一个匹配的元素
     *
     * @param collection 集合
     * @param predicate 匹配条件
     * @param <T> 泛型类型
     * @return 匹配的元素，如果没有返回null
     */
    public static <T> T findFirst(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) {
            return null;
        }
        return collection.stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    /**
     * 查找任意匹配的元素
     *
     * @param collection 集合
     * @param predicate 匹配条件
     * @param <T> 泛型类型
     * @return 匹配的元素，如果没有返回null
     */
    public static <T> T findAny(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) {
            return null;
        }
        return collection.stream()
                .filter(predicate)
                .findAny()
                .orElse(null);
    }

    /**
     * 判断是否包含匹配的元素
     *
     * @param collection 集合
     * @param predicate 匹配条件
     * @param <T> 泛型类型
     * @return 是否包含
     */
    public static <T> boolean anyMatch(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) {
            return false;
        }
        return collection.stream().anyMatch(predicate);
    }

    /**
     * 判断是否所有元素都匹配
     *
     * @param collection 集合
     * @param predicate 匹配条件
     * @param <T> 泛型类型
     * @return 是否所有都匹配
     */
    public static <T> boolean allMatch(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) {
            return false;
        }
        return collection.stream().allMatch(predicate);
    }

    /**
     * 判断是否没有元素匹配
     *
     * @param collection 集合
     * @param predicate 匹配条件
     * @param <T> 泛型类型
     * @return 是否没有匹配
     */
    public static <T> boolean noneMatch(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) {
            return true;
        }
        return collection.stream().noneMatch(predicate);
    }

    // ==================== 集合运算方法 ====================

    /**
     * 求两个集合的交集
     *
     * @param collection1 集合1
     * @param collection2 集合2
     * @param <T> 泛型类型
     * @return 交集
     */
    public static <T> Set<T> intersection(Collection<T> collection1, Collection<T> collection2) {
        if (isEmpty(collection1) || isEmpty(collection2)) {
            return new HashSet<>();
        }
        Set<T> result = new HashSet<>(collection1);
        result.retainAll(collection2);
        return result;
    }

    /**
     * 求两个集合的并集
     *
     * @param collection1 集合1
     * @param collection2 集合2
     * @param <T> 泛型类型
     * @return 并集
     */
    public static <T> Set<T> union(Collection<T> collection1, Collection<T> collection2) {
        Set<T> result = new HashSet<>();
        if (isNotEmpty(collection1)) {
            result.addAll(collection1);
        }
        if (isNotEmpty(collection2)) {
            result.addAll(collection2);
        }
        return result;
    }

    /**
     * 求两个集合的差集（collection1 - collection2）
     *
     * @param collection1 集合1
     * @param collection2 集合2
     * @param <T> 泛型类型
     * @return 差集
     */
    public static <T> Set<T> difference(Collection<T> collection1, Collection<T> collection2) {
        if (isEmpty(collection1)) {
            return new HashSet<>();
        }
        Set<T> result = new HashSet<>(collection1);
        if (isNotEmpty(collection2)) {
            result.removeAll(collection2);
        }
        return result;
    }

    /**
     * 求两个集合的对称差集
     *
     * @param collection1 集合1
     * @param collection2 集合2
     * @param <T> 泛型类型
     * @return 对称差集
     */
    public static <T> Set<T> symmetricDifference(Collection<T> collection1, Collection<T> collection2) {
        Set<T> diff1 = difference(collection1, collection2);
        Set<T> diff2 = difference(collection2, collection1);
        return union(diff1, diff2);
    }

    // ==================== 排序方法 ====================

    /**
     * 排序集合
     *
     * @param collection 集合
     * @param <T> 泛型类型（必须实现Comparable接口）
     * @return 排序后的List
     */
    public static <T extends Comparable<T>> List<T> sort(Collection<T> collection) {
        if (isEmpty(collection)) {
            return new ArrayList<>();
        }
        return collection.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 使用比较器排序集合
     *
     * @param collection 集合
     * @param comparator 比较器
     * @param <T> 泛型类型
     * @return 排序后的List
     */
    public static <T> List<T> sort(Collection<T> collection, Comparator<T> comparator) {
        if (isEmpty(collection) || comparator == null) {
            return new ArrayList<>(collection);
        }
        return collection.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    // ==================== 分页方法 ====================

    /**
     * 分页获取数据
     *
     * @param list 原List
     * @param page 页码（从0开始）
     * @param size 页大小
     * @param <T> 泛型类型
     * @return 分页后的List
     */
    public static <T> List<T> page(List<T> list, int page, int size) {
        if (isEmpty(list) || page < 0 || size <= 0) {
            return new ArrayList<>();
        }

        int fromIndex = page * size;
        if (fromIndex >= list.size()) {
            return new ArrayList<>();
        }

        int toIndex = Math.min(fromIndex + size, list.size());
        return new ArrayList<>(list.subList(fromIndex, toIndex));
    }

    /**
     * 分批处理
     *
     * @param list 原List
     * @param batchSize 批大小
     * @param <T> 泛型类型
     * @return 分批后的List
     */
    public static <T> List<List<T>> batch(List<T> list, int batchSize) {
        if (isEmpty(list) || batchSize <= 0) {
            return new ArrayList<>();
        }

        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            int toIndex = Math.min(i + batchSize, list.size());
            batches.add(new ArrayList<>(list.subList(i, toIndex)));
        }
        return batches;
    }

    // ==================== 统计方法 ====================

    /**
     * 统计匹配元素的数量
     *
     * @param collection 集合
     * @param predicate 匹配条件
     * @param <T> 泛型类型
     * @return 匹配数量
     */
    public static <T> long count(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) {
            return 0;
        }
        return collection.stream()
                .filter(predicate)
                .count();
    }

    /**
     * 去重
     *
     * @param collection 集合
     * @param <T> 泛型类型
     * @return 去重后的List
     */
    public static <T> List<T> distinct(Collection<T> collection) {
        if (isEmpty(collection)) {
            return new ArrayList<>();
        }
        return collection.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 根据指定字段去重
     *
     * @param collection 集合
     * @param keyExtractor 字段提取器
     * @param <T> 泛型类型
     * @param <K> 字段类型
     * @return 去重后的List
     */
    public static <T, K> List<T> distinctBy(Collection<T> collection, Function<T, K> keyExtractor) {
        if (isEmpty(collection) || keyExtractor == null) {
            return new ArrayList<>();
        }

        Set<K> seen = new HashSet<>();
        return collection.stream()
                .filter(item -> seen.add(keyExtractor.apply(item)))
                .collect(Collectors.toList());
    }
}


