package com.honyrun.util.common;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.honyrun.util.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * JSON工具类
 * 提供JSON序列化、反序列化、格式化
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  16:35:00
 * @modified 2025-07-01 16:35:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public final class JsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    /**
     * ObjectMapper实例
     */
    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private JsonUtil() {
        // 工具类，禁止实例化
    }

    /**
     * 创建ObjectMapper实例
     *
     * @return ObjectMapper实例
     */
    private static ObjectMapper createObjectMapper() {
        // 禁用Unicode转义，确保中文字符正确显示
        JsonFactory factory = JsonFactory.builder()
                .disable(JsonWriteFeature.ESCAPE_NON_ASCII)
                .build();
        ObjectMapper mapper = new ObjectMapper(factory);

        // 注册Java时间模块
        mapper.registerModule(new JavaTimeModule());

        // 配置序列化特性
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false);

        // 配置反序列化特性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

        return mapper;
    }

    /**
     * 获取ObjectMapper实例
     *
     * @return ObjectMapper实例
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    // ==================== 序列化方法 ====================

    /**
     * 对象转JSON字符串
     *
     * @param obj 对象
     * @return JSON字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            LoggingUtil.error(logger, "对象转JSON失败", e);
            throw new RuntimeException("JSON序列化失败", e);
        }
    }

    /**
     * 对象转格式化的JSON字符串
     *
     * @param obj 对象
     * @return 格式化的JSON字符串
     */
    public static String toPrettyJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            LoggingUtil.error(logger, "对象转格式化JSON失败", e);
            return null;
        }
    }

    /**
     * 对象转JSON字节数组
     *
     * @param obj 对象
     * @return JSON字节数组
     */
    public static byte[] toJsonBytes(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            LoggingUtil.error(logger, "对象转JSON字节数组失败", e);
            return null;
        }
    }

    // ==================== 反序列化方法 ====================

    /**
     * JSON字符串转对象
     *
     * @param json JSON字符串
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 对象实例
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (StringUtil.isEmpty(json) || clazz == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            LoggingUtil.error(logger, "JSON转对象失败", e);
            throw new RuntimeException("JSON反序列化失败", e);
        }
    }

    /**
     * JSON字符串转对象（使用TypeReference）
     *
     * @param json JSON字符串
     * @param typeReference 类型引用
     * @param <T> 泛型类型
     * @return 对象实例
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (StringUtil.isEmpty(json) || typeReference == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            LoggingUtil.error(logger, "JSON转对象失败", e);
            return null;
        }
    }

    /**
     * JSON字节数组转对象
     *
     * @param jsonBytes JSON字节数组
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 对象实例
     */
    public static <T> T fromJsonBytes(byte[] jsonBytes, Class<T> clazz) {
        if (jsonBytes == null || jsonBytes.length == 0 || clazz == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(jsonBytes, clazz);
        } catch (IOException e) {
            LoggingUtil.error(logger, "JSON字节数组转对象失败", e);
            return null;
        }
    }

    // ==================== 集合转换方法 ====================

    /**
     * JSON字符串转List
     *
     * @param json JSON字符串
     * @param elementClass 元素类型
     * @param <T> 泛型类型
     * @return List对象
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> elementClass) {
        if (StringUtil.isEmpty(json) || elementClass == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json,
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, elementClass));
        } catch (JsonProcessingException e) {
            LoggingUtil.error(logger, "JSON转List失败", e);
            return null;
        }
    }

    /**
     * JSON字符串转Map
     *
     * @param json JSON字符串
     * @return Map对象
     */
    public static Map<String, Object> fromJsonToMap(String json) {
        if (StringUtil.isEmpty(json)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            LoggingUtil.error(logger, "JSON转Map失败", e);
            return null;
        }
    }

    /**
     * JSON字符串转指定类型的Map
     *
     * @param json JSON字符串
     * @param keyClass 键类型
     * @param valueClass 值类型
     * @param <K> 键泛型类型
     * @param <V> 值泛型类型
     * @return Map对象
     */
    public static <K, V> Map<K, V> fromJsonToMap(String json, Class<K> keyClass, Class<V> valueClass) {
        if (StringUtil.isEmpty(json) || keyClass == null || valueClass == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json,
                    OBJECT_MAPPER.getTypeFactory().constructMapType(Map.class, keyClass, valueClass));
        } catch (JsonProcessingException e) {
            LoggingUtil.error(logger, "JSON转Map失败", e);
            return null;
        }
    }

    // ==================== JsonNode操作方法 ====================

    /**
     * 字符串转JsonNode
     *
     * @param json JSON字符串
     * @return JsonNode对象
     */
    public static JsonNode parseJson(String json) {
        if (StringUtil.isEmpty(json)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            LoggingUtil.error(logger, "解析JSON失败", e);
            return null;
        }
    }

    /**
     * 对象转JsonNode
     *
     * @param obj 对象
     * @return JsonNode对象
     */
    public static JsonNode toJsonNode(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            if (obj instanceof String) {
                return OBJECT_MAPPER.readTree((String) obj);
            } else {
                return OBJECT_MAPPER.valueToTree(obj);
            }
        } catch (JsonProcessingException e) {
            LoggingUtil.error(logger, "对象转JsonNode失败", e);
            throw new RuntimeException("JSON解析失败", e);
        }
    }

    /**
     * JsonNode转对象
     *
     * @param jsonNode JsonNode对象
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 对象实例
     */
    public static <T> T fromJsonNode(JsonNode jsonNode, Class<T> clazz) {
        if (jsonNode == null || clazz == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.treeToValue(jsonNode, clazz);
        } catch (JsonProcessingException e) {
            LoggingUtil.error(logger, "JsonNode转对象失败", e);
            return null;
        }
    }

    // ==================== 对象转换方法 ====================

    /**
     * 对象转换（通过JSON中转）
     *
     * @param fromObj 源对象
     * @param toClass 目标类型
     * @param <T> 泛型类型
     * @return 目标对象
     */
    public static <T> T convertValue(Object fromObj, Class<T> toClass) {
        if (fromObj == null || toClass == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.convertValue(fromObj, toClass);
        } catch (IllegalArgumentException e) {
            LoggingUtil.error(logger, "对象转换失败", e);
            return null;
        }
    }

    /**
     * 对象转换（使用TypeReference）
     *
     * @param fromObj 源对象
     * @param typeReference 类型引用
     * @param <T> 泛型类型
     * @return 目标对象
     */
    public static <T> T convertValue(Object fromObj, TypeReference<T> typeReference) {
        if (fromObj == null || typeReference == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.convertValue(fromObj, typeReference);
        } catch (IllegalArgumentException e) {
            LoggingUtil.error(logger, "对象转换失败", e);
            return null;
        }
    }

    // ==================== 验证方法 ====================

    /**
     * 验证字符串是否为有效的JSON
     *
     * @param json JSON字符串
     * @return 是否为有效JSON
     */
    public static boolean isValidJson(String json) {
        if (StringUtil.isEmpty(json)) {
            return false;
        }
        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * 验证字符串是否为有效的JSON对象
     *
     * @param json JSON字符串
     * @return 是否为有效JSON对象
     */
    public static boolean isValidJsonObject(String json) {
        JsonNode jsonNode = parseJson(json);
        return jsonNode != null && jsonNode.isObject();
    }

    /**
     * 验证字符串是否为有效的JSON数组
     *
     * @param json JSON字符串
     * @return 是否为有效JSON数组
     */
    public static boolean isValidJsonArray(String json) {
        JsonNode jsonNode = parseJson(json);
        return jsonNode != null && jsonNode.isArray();
    }

    // ==================== 格式化方法 ====================

    /**
     * 格式化JSON字符串
     *
     * @param json JSON字符串
     * @return 格式化后的JSON字符串
     */
    public static String formatJson(String json) {
        JsonNode jsonNode = parseJson(json);
        if (jsonNode == null) {
            return json;
        }
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            LoggingUtil.error(logger, "格式化JSON失败", e);
            return json;
        }
    }

    /**
     * 压缩JSON字符串（移除空白字符）
     *
     * @param json JSON字符串
     * @return 压缩后的JSON字符串
     */
    public static String compactJson(String json) {
        JsonNode jsonNode = parseJson(json);
        if (jsonNode == null) {
            return json;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            LoggingUtil.error(logger, "压缩JSON失败", e);
            return json;
        }
    }

    // ==================== 合并方法 ====================

    /**
     * 合并两个JSON对象
     *
     * @param json1 第一个JSON字符串
     * @param json2 第二个JSON字符串
     * @return 合并后的JSON字符串
     */
    public static String mergeJson(String json1, String json2) {
        JsonNode node1 = parseJson(json1);
        JsonNode node2 = parseJson(json2);

        if (node1 == null) {
            return json2;
        }
        if (node2 == null) {
            return json1;
        }

        if (!node1.isObject() || !node2.isObject()) {
            return json2; // 如果不是对象，返回第二个
        }

        try {
            JsonNode merged = mergeJsonNodes(node1, node2);
            return OBJECT_MAPPER.writeValueAsString(merged);
        } catch (JsonProcessingException e) {
            LoggingUtil.error(logger, "合并JSON失败", e);
            return json2;
        }
    }

    /**
     * 合并JsonNode
     *
     * @param mainNode 主节点
     * @param updateNode 更新节点
     * @return 合并后的节点
     */
    private static JsonNode mergeJsonNodes(JsonNode mainNode, JsonNode updateNode) {
        updateNode.properties().forEach(entry -> {
            String fieldName = entry.getKey();
            JsonNode updateValue = entry.getValue();

            if (mainNode.has(fieldName)) {
                JsonNode mainValue = mainNode.get(fieldName);
                if (mainValue.isObject() && updateValue.isObject()) {
                    // 递归合并对象
                    mergeJsonNodes(mainValue, updateValue);
                } else {
                    // 替换值
                    ((com.fasterxml.jackson.databind.node.ObjectNode) mainNode).set(fieldName, updateValue);
                }
            } else {
                // 添加新字段
                ((com.fasterxml.jackson.databind.node.ObjectNode) mainNode).set(fieldName, updateValue);
            }
        });

        return mainNode;
    }

    // ==================== 路径操作方法 ====================

    /**
     * 根据路径获取JSON值
     *
     * @param json JSON字符串
     * @param path 路径（用.分隔）
     * @return 值的字符串表示
     */
    public static String getValueByPath(String json, String path) {
        JsonNode jsonNode = parseJson(json);
        if (jsonNode == null || StringUtil.isEmpty(path)) {
            return null;
        }

        String[] pathParts = path.split("\\.");
        JsonNode currentNode = jsonNode;

        for (String part : pathParts) {
            if (currentNode.isObject() && currentNode.has(part)) {
                currentNode = currentNode.get(part);
            } else {
                return null;
            }
        }

        return currentNode.isTextual() ? currentNode.textValue() : currentNode.toString();
    }

    /**
     * 根据路径设置JSON值
     *
     * @param json JSON字符串
     * @param path 路径（用.分隔）
     * @param value 值
     * @return 更新后的JSON字符串
     */
    public static String setValueByPath(String json, String path, Object value) {
        JsonNode jsonNode = parseJson(json);
        if (jsonNode == null || StringUtil.isEmpty(path)) {
            return json;
        }

        if (!jsonNode.isObject()) {
            return json;
        }

        String[] pathParts = path.split("\\.");
        com.fasterxml.jackson.databind.node.ObjectNode currentNode =
                (com.fasterxml.jackson.databind.node.ObjectNode) jsonNode;

        // 导航到父节点
        for (int i = 0; i < pathParts.length - 1; i++) {
            String part = pathParts[i];
            if (!currentNode.has(part)) {
                currentNode.set(part, OBJECT_MAPPER.createObjectNode());
            }
            JsonNode nextNode = currentNode.get(part);
            if (nextNode.isObject()) {
                currentNode = (com.fasterxml.jackson.databind.node.ObjectNode) nextNode;
            } else {
                return json; // 路径冲突
            }
        }

        // 设置值
        String lastPart = pathParts[pathParts.length - 1];
        currentNode.set(lastPart, toJsonNode(value));

        try {
            return OBJECT_MAPPER.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            LoggingUtil.error(logger, "设置JSON值失败", e);
            return json;
        }
    }
}


