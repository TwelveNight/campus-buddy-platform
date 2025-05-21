package com.example.campusbuddy.exception;

import com.example.campusbuddy.common.ResultCode;

/**
 * 禁止操作异常
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(ResultCode.FORBIDDEN, message);
    }

    public ForbiddenException() {
        super(ResultCode.FORBIDDEN, "没有权限执行此操作");
    }
}
