package com.server.surveyanalystserver.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应码枚举
 * 定义系统中所有API接口的响应码和对应消息
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /** 操作成功 */
    SUCCESS(200, "操作成功"),
    
    /** 操作失败（服务器内部错误） */
    ERROR(500, "操作失败"),
    
    /** 未授权（未登录或token无效） */
    UNAUTHORIZED(401, "未授权"),
    
    /** 无权限（已登录但无访问权限） */
    FORBIDDEN(403, "无权限"),
    
    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),
    
    /** 请求参数错误 */
    BAD_REQUEST(400, "请求参数错误"),
    
    /** 数据验证失败 */
    VALIDATION_ERROR(422, "数据验证失败");

    /** 响应码 */
    private final Integer code;
    
    /** 响应消息 */
    private final String message;
}

