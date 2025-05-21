package com.example.campusbuddy.common;

/**
 * 统一响应状态码
 */
public class ResultCode {
    // 成功
    public static final int SUCCESS = 200;

    // 客户端错误 4xx
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int CONFLICT = 409;

    // 服务器错误 5xx
    public static final int INTERNAL_ERROR = 500;
    public static final int SERVICE_UNAVAILABLE = 503;

    // 业务相关错误码 (1000+)
    public static final int USER_NOT_FOUND = 1001;
    public static final int USER_ALREADY_EXISTS = 1002;
    public static final int WRONG_PASSWORD = 1003;
    public static final int HELP_INFO_NOT_FOUND = 1004;
    public static final int OPERATION_NOT_ALLOWED = 1005;

    // 参数校验错误 (2000+)
    public static final int VALIDATION_ERROR = 2000;
}