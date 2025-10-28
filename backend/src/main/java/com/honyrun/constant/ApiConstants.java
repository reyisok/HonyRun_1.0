package com.honyrun.constant;

/**
 * API常量类
 * 提供API版本、端点路径、请求头常量
 *
 * @author Mr.Rey
 * @version 2.0.0
 * @created 2025-07-01  16:05:00
 * @modified 2025-07-01 16:05:00
 * Copyright © 2025 HonyRun. All rights reserved.
 */
public final class ApiConstants {

    private ApiConstants() {
        // 常量类，禁止实例化
    }

    // ==================== API路径前缀常量 ====================

    /**
     * API根路径
     */
    public static final String API_ROOT = "/api";

    /**
     * API v1版本前缀
     */
    public static final String API_V1_PREFIX = API_ROOT + "/v1";

    /**
     * API v1版本路径（简化版）
     */
    public static final String API_V1 = API_V1_PREFIX;

    /**
     * API v2版本前缀
     */
    public static final String API_V2_PREFIX = API_ROOT + "/v2";

    /**
     * 系统管理API前缀
     */
    public static final String SYSTEM_API_PREFIX = API_V1_PREFIX + "/system";

    /**
     * 公共API前缀
     */
    public static final String PUBLIC_API_PREFIX = API_V1_PREFIX + "/public";

    // ==================== API版本信息 ====================

    /**
     * API主版本号
     */
    public static final String API_MAJOR_VERSION = "1";

    /**
     * API次版本号
     */
    public static final String API_MINOR_VERSION = "0";

    /**
     * API修订版本号
     */
    public static final String API_PATCH_VERSION = "0";

    /**
     * API完整版本号
     */
    public static final String API_VERSION = API_MAJOR_VERSION + "." + API_MINOR_VERSION + "." + API_PATCH_VERSION;

    /**
     * API版本头名称
     */
    public static final String API_VERSION_HEADER = "X-API-Version";

    /**
     * API兼容版本
     */
    public static final String[] COMPATIBLE_VERSIONS = {"1.0.0"};

    // ==================== 请求头常量 ====================

    /**
     * 内容类型头
     */
    public static final String CONTENT_TYPE = "Content-Type";

    /**
     * 接受头
     */
    public static final String ACCEPT = "Accept";

    /**
     * 授权头
     */
    public static final String AUTHORIZATION = "Authorization";

    /**
     * 用户代理头
     */
    public static final String USER_AGENT = "User-Agent";

    /**
     * 请求ID头
     */
    public static final String REQUEST_ID = "X-Request-ID";

    /**
     * 客户端IP头
     */
    public static final String CLIENT_IP = "X-Client-IP";

    /**
     * 真实IP头
     */
    public static final String REAL_IP = "X-Real-IP";

    /**
     * 转发IP头
     */
    public static final String FORWARDED_FOR = "X-Forwarded-For";

    /**
     * 客户端版本头
     */
    public static final String CLIENT_VERSION = "X-Client-Version";

    /**
     * 设备类型头
     */
    public static final String DEVICE_TYPE = "X-Device-Type";

    /**
     * 平台类型头
     */
    public static final String PLATFORM_TYPE = "X-Platform-Type";

    /**
     * 时间戳头
     */
    public static final String TIMESTAMP = "X-Timestamp";

    /**
     * 签名头
     */
    public static final String SIGNATURE = "X-Signature";

    /**
     * 随机数头
     */
    public static final String NONCE = "X-Nonce";

    // ==================== 响应头常量 ====================

    /**
     * 服务器头
     */
    public static final String SERVER = "Server";

    /**
     * 响应时间头
     */
    public static final String RESPONSE_TIME = "X-Response-Time";

    /**
     * 请求跟踪ID头
     */
    public static final String TRACE_ID = "X-Trace-ID";

    /**
     * 限流剩余次数头
     */
    public static final String RATE_LIMIT_REMAINING = "X-RateLimit-Remaining";

    /**
     * 限流重置时间头
     */
    public static final String RATE_LIMIT_RESET = "X-RateLimit-Reset";

    /**
     * 限流总次数头
     */
    public static final String RATE_LIMIT_LIMIT = "X-RateLimit-Limit";

    /**
     * 分页总数头
     */
    public static final String TOTAL_COUNT = "X-Total-Count";

    /**
     * 分页总页数头
     */
    public static final String TOTAL_PAGES = "X-Total-Pages";

    /**
     * 当前页码头
     */
    public static final String CURRENT_PAGE = "X-Current-Page";

    /**
     * 页大小头
     */
    public static final String PAGE_SIZE = "X-Page-Size";

    // ==================== 媒体类型常量 ====================

    /**
     * JSON媒体类型
     */
    public static final String MEDIA_TYPE_JSON = "application/json";

    /**
     * XML媒体类型
     */
    public static final String MEDIA_TYPE_XML = "application/xml";

    /**
     * 表单媒体类型
     */
    public static final String MEDIA_TYPE_FORM = "application/x-www-form-urlencoded";

    /**
     * 多部分表单媒体类型
     */
    public static final String MEDIA_TYPE_MULTIPART = "multipart/form-data";

    /**
     * 文本媒体类型
     */
    public static final String MEDIA_TYPE_TEXT = "text/plain";

    /**
     * HTML媒体类型
     */
    public static final String MEDIA_TYPE_HTML = "text/html";

    /**
     * 流媒体类型
     */
    public static final String MEDIA_TYPE_STREAM = "application/octet-stream";

    /**
     * PDF媒体类型
     */
    public static final String MEDIA_TYPE_PDF = "application/pdf";

    /**
     * Excel媒体类型
     */
    public static final String MEDIA_TYPE_EXCEL = "application/vnd.ms-excel";

    /**
     * Word媒体类型
     */
    public static final String MEDIA_TYPE_WORD = "application/msword";

    // ==================== 字符编码常量 ====================

    /**
     * UTF-8编码
     */
    public static final String CHARSET_UTF8 = "UTF-8";

    /**
     * GBK编码
     */
    public static final String CHARSET_GBK = "GBK";

    /**
     * ISO-8859-1编码
     */
    public static final String CHARSET_ISO = "ISO-8859-1";

    /**
     * JSON内容类型（含编码）
     */
    public static final String JSON_UTF8 = MEDIA_TYPE_JSON + ";charset=" + CHARSET_UTF8;

    /**
     * 文本内容类型（含编码）
     */
    public static final String TEXT_UTF8 = MEDIA_TYPE_TEXT + ";charset=" + CHARSET_UTF8;

    /**
     * HTML内容类型（含编码）
     */
    public static final String HTML_UTF8 = MEDIA_TYPE_HTML + ";charset=" + CHARSET_UTF8;

    // ==================== HTTP方法常量 ====================

    /**
     * GET方法
     */
    public static final String METHOD_GET = "GET";

    /**
     * POST方法
     */
    public static final String METHOD_POST = "POST";

    /**
     * PUT方法
     */
    public static final String METHOD_PUT = "PUT";

    /**
     * DELETE方法
     */
    public static final String METHOD_DELETE = "DELETE";

    /**
     * PATCH方法
     */
    public static final String METHOD_PATCH = "PATCH";

    /**
     * HEAD方法
     */
    public static final String METHOD_HEAD = "HEAD";

    /**
     * OPTIONS方法
     */
    public static final String METHOD_OPTIONS = "OPTIONS";

    // ==================== 参数名称常量 ====================

    /**
     * 页码参数
     */
    public static final String PARAM_PAGE = "page";

    /**
     * 页大小参数
     */
    public static final String PARAM_SIZE = "size";

    /**
     * 排序参数
     */
    public static final String PARAM_SORT = "sort";

    /**
     * 搜索参数
     */
    public static final String PARAM_SEARCH = "search";

    /**
     * 过滤参数
     */
    public static final String PARAM_FILTER = "filter";

    /**
     * 字段参数
     */
    public static final String PARAM_FIELDS = "fields";

    /**
     * 包含参数
     */
    public static final String PARAM_INCLUDE = "include";

    /**
     * 排除参数
     */
    public static final String PARAM_EXCLUDE = "exclude";

    /**
     * 格式参数
     */
    public static final String PARAM_FORMAT = "format";

    /**
     * 回调参数
     */
    public static final String PARAM_CALLBACK = "callback";

    // ==================== 默认值常量 ====================

    /**
     * 默认页码
     */
    public static final int DEFAULT_PAGE = 0;

    /**
     * 默认页大小
     */
    public static final int DEFAULT_SIZE = 20;

    /**
     * 最大页大小
     */
    public static final int MAX_SIZE = 1000;

    /**
     * 默认排序字段
     */
    public static final String DEFAULT_SORT = "id";

    /**
     * 默认排序方向
     */
    public static final String DEFAULT_DIRECTION = "ASC";

    /**
     * 默认格式
     */
    public static final String DEFAULT_FORMAT = "json";

    // ==================== 错误码常量 ====================

    /**
     * API版本不支持错误码
     */
    public static final String ERROR_UNSUPPORTED_VERSION = "UNSUPPORTED_API_VERSION";

    /**
     * 参数无效错误码
     */
    public static final String ERROR_INVALID_PARAMETER = "INVALID_PARAMETER";

    /**
     * 缺少必需参数错误码
     */
    public static final String ERROR_MISSING_PARAMETER = "MISSING_PARAMETER";

    /**
     * 请求格式错误码
     */
    public static final String ERROR_INVALID_FORMAT = "INVALID_FORMAT";

    /**
     * 请求过大错误码
     */
    public static final String ERROR_REQUEST_TOO_LARGE = "REQUEST_TOO_LARGE";

    /**
     * 方法不允许错误码
     */
    public static final String ERROR_METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";

    /**
     * 媒体类型不支持错误码
     */
    public static final String ERROR_UNSUPPORTED_MEDIA_TYPE = "UNSUPPORTED_MEDIA_TYPE";

    // ==================== 缓存控制常量 ====================

    /**
     * 缓存控制头
     */
    public static final String CACHE_CONTROL = "Cache-Control";

    /**
     * 不缓存
     */
    public static final String NO_CACHE = "no-cache";

    /**
     * 不存储
     */
    public static final String NO_STORE = "no-store";

    /**
     * 必须重新验证
     */
    public static final String MUST_REVALIDATE = "must-revalidate";

    /**
     * 公共缓存
     */
    public static final String PUBLIC = "public";

    /**
     * 私有缓存
     */
    public static final String PRIVATE = "private";

    /**
     * 最大缓存时间（秒）
     */
    public static final String MAX_AGE = "max-age=";

    /**
     * ETag头
     */
    public static final String ETAG = "ETag";

    /**
     * 最后修改时间头
     */
    public static final String LAST_MODIFIED = "Last-Modified";

    /**
     * 如果匹配头
     */
    public static final String IF_MATCH = "If-Match";

    /**
     * 如果不匹配头
     */
    public static final String IF_NONE_MATCH = "If-None-Match";

    /**
     * 如果修改后头
     */
    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";

    /**
     * 如果未修改后头
     */
    public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";

    // ==================== CORS常量 ====================

    /**
     * 访问控制允许源头
     */
    public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

    /**
     * 访问控制允许方法头
     */
    public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";

    /**
     * 访问控制允许头部头
     */
    public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";

    /**
     * 访问控制暴露头部头
     */
    public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";

    /**
     * 访问控制允许凭证头
     */
    public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

    /**
     * 访问控制最大年龄头
     */
    public static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";

    /**
     * 默认允许的方法
     */
    public static final String DEFAULT_ALLOWED_METHODS = "GET,POST,PUT,DELETE,OPTIONS,HEAD,PATCH";

    /**
     * 默认允许的头部
     */
    public static final String DEFAULT_ALLOWED_HEADERS = "Content-Type,Authorization,X-Requested-With,X-API-Version";

    /**
     * 默认暴露的头部
     */
    public static final String DEFAULT_EXPOSED_HEADERS = "X-Total-Count,X-Total-Pages,X-Current-Page,X-Page-Size";
}


