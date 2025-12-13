package com.server.surveyanalystserver.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数验证异常
     * 当使用@Valid注解验证请求参数时，如果验证失败会抛出此异常
     * @param e 方法参数验证异常
     * @return 包含验证错误信息的响应结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数验证失败";
        log.error("参数验证失败: {}", message);
        return Result.error(ResultCode.VALIDATION_ERROR.getCode(), message);
    }

    /**
     * 处理参数绑定异常
     * 当请求参数无法正确绑定到方法参数时抛出此异常
     * @param e 参数绑定异常
     * @return 包含绑定错误信息的响应结果
     */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        log.error("参数绑定失败: {}", message);
        return Result.error(ResultCode.VALIDATION_ERROR.getCode(), message);
    }

    /**
     * 处理约束违反异常
     * 当使用@Validated注解进行方法参数验证时，如果验证失败会抛出此异常
     * @param e 约束违反异常
     * @return 包含约束违反信息的响应结果
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String message = violations.iterator().next().getMessage();
        log.error("约束违反: {}", message);
        return Result.error(ResultCode.VALIDATION_ERROR.getCode(), message);
    }

    /**
     * 处理运行时异常
     * 捕获所有RuntimeException及其子类异常，返回错误响应
     * @param e 运行时异常
     * @return 包含错误信息的响应结果
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: ", e);
        return Result.error(ResultCode.ERROR.getCode(), e.getMessage());
    }

    /**
     * 处理所有未捕获的异常
     * 作为兜底异常处理器，捕获所有未被其他处理器处理的异常
     * @param e 异常对象
     * @return 包含系统错误信息的响应结果
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error(ResultCode.ERROR);
    }
}

