package com.example.campusbuddy.exception;

import com.example.campusbuddy.common.ResultCode;

/**
 * 未授权操作异常
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(ResultCode.UNAUTHORIZED, message);
    }

    public UnauthorizedException() {
        super(ResultCode.UNAUTHORIZED, "未登录或Token已过期，请重新登录");
    }
}
