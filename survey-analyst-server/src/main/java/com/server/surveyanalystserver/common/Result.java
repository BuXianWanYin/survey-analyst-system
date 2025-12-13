package com.server.surveyanalystserver.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装
 */
@Data
@ApiModel(description = "统一响应结果")
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "响应码", required = true)
    private Integer code;

    @ApiModelProperty(value = "响应消息")
    private String message;

    @ApiModelProperty(value = "响应数据")
    private T data;

    public Result() {
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 创建成功响应结果（无消息无数据）
     * @param <T> 响应数据类型
     * @return 成功响应结果对象
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 创建成功响应结果（带消息，无数据）
     * @param <T> 响应数据类型
     * @param message 响应消息
     * @return 成功响应结果对象
     */
    public static <T> Result<T> success(String message) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, null);
    }

    /**
     * 创建成功响应结果（带数据，使用默认成功消息）
     * @param <T> 响应数据类型
     * @param data 响应数据
     * @return 成功响应结果对象
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 创建成功响应结果（带消息和数据）
     * @param <T> 响应数据类型
     * @param message 响应消息
     * @param data 响应数据
     * @return 成功响应结果对象
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 创建失败响应结果（使用默认错误码和消息）
     * @param <T> 响应数据类型
     * @return 失败响应结果对象
     */
    public static <T> Result<T> error() {
        return new Result<>(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMessage(), null);
    }

    /**
     * 创建失败响应结果（使用默认错误码，自定义消息）
     * @param <T> 响应数据类型
     * @param message 错误消息
     * @return 失败响应结果对象
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ResultCode.ERROR.getCode(), message, null);
    }

    /**
     * 创建失败响应结果（自定义错误码和消息）
     * @param <T> 响应数据类型
     * @param code 错误码
     * @param message 错误消息
     * @return 失败响应结果对象
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 创建失败响应结果（使用ResultCode枚举）
     * @param <T> 响应数据类型
     * @param resultCode 响应码枚举
     * @return 失败响应结果对象
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }
}

