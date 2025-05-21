package com.example.campusbuddy.exception;

import com.example.campusbuddy.common.ResultCode;

/**
 * 资源不存在异常
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(ResultCode.NOT_FOUND, message);
    }

    public ResourceNotFoundException(String resourceName, Long resourceId) {
        super(ResultCode.NOT_FOUND, String.format("%s资源不存在，ID: %d", resourceName, resourceId));
    }
}
