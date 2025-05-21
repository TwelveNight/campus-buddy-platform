package com.example.campusbuddy.common;

import lombok.Data;

/**
 * 统一响应体
 * 
 * @param <T> 响应数据类型
 */
@Data
public class R<T> {
    private int code;
    private String message;
    private T data;
    private long timestamp;

    public R() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 操作成功返回结果
     * 
     * @param data 返回数据
     * @param <T>  数据类型
     * @return 响应结果
     */
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(ResultCode.SUCCESS);
        r.setMessage("操作成功");
        r.setData(data);
        return r;
    }

    /**
     * 操作成功返回结果
     * 
     * @param message 成功消息
     * @param data    返回数据
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> R<T> ok(String message, T data) {
        R<T> r = new R<>();
        r.setCode(ResultCode.SUCCESS);
        r.setMessage(message);
        r.setData(data);
        return r;
    }

    /**
     * 操作失败返回结果
     * 
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> R<T> fail() {
        return fail(ResultCode.INTERNAL_ERROR, "操作失败");
    }

    /**
     * 操作失败返回结果
     * 
     * @param message 失败消息
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> R<T> fail(String message) {
        return fail(ResultCode.INTERNAL_ERROR, message);
    }

    /**
     * 操作失败返回结果
     * 
     * @param code    状态码
     * @param message 失败消息
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> R<T> fail(int code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        r.setData(null);
        return r;
    }

    /**
     * 未授权返回结果
     * 
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> R<T> unauthorized() {
        return fail(ResultCode.UNAUTHORIZED, "未登录或Token已过期，请重新登录");
    }

    /**
     * 未授权返回结果
     * 
     * @param message 未授权消息
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> R<T> unauthorized(String message) {
        return fail(ResultCode.UNAUTHORIZED, message);
    }

    /**
     * 禁止访问返回结果
     * 
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> R<T> forbidden() {
        return fail(ResultCode.FORBIDDEN, "没有权限执行此操作");
    }

    /**
     * 资源不存在返回结果
     * 
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> R<T> notFound() {
        return fail(ResultCode.NOT_FOUND, "请求的资源不存在");
    }

    /**
     * 参数验证失败返回结果
     * 
     * @param message 验证失败消息
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> R<T> validateFailed(String message) {
        return fail(ResultCode.VALIDATION_ERROR, message);
    }
}
