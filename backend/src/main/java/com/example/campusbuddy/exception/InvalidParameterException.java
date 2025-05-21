package com.example.campusbuddy.exception;

import com.example.campusbuddy.common.ResultCode;

/**
 * 无效参数异常
 */
public class InvalidParameterException extends BusinessException {

    public InvalidParameterException(String message) {
        super(ResultCode.VALIDATION_ERROR, message);
    }
}
