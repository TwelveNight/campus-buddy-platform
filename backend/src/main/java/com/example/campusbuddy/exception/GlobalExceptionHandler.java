package com.example.campusbuddy.exception;

import com.example.campusbuddy.common.R;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 处理所有运行时异常
    @ExceptionHandler(RuntimeException.class)
    public R<?> handleRuntimeException(RuntimeException ex) {
        return R.fail(500, ex.getMessage());
    }

    // 处理参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<?> handleValidationException(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        return R.fail(400, msg);
    }

    // 处理参数非法异常（如注册、登录等业务）
    @ExceptionHandler(IllegalArgumentException.class)
    public R<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        return R.fail(400, ex.getMessage());
    }

    // 可扩展：处理自定义业务异常
    // @ExceptionHandler(UserAlreadyExistsException.class)
    // public R<?> handleUserExists(UserAlreadyExistsException ex) {
    // return R.fail(409, ex.getMessage());
    // }
}
