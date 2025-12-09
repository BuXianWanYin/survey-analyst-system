package com.server.surveyanalystserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体类
 */
@Data
@TableName("operation_log")
@ApiModel(description = "操作日志实体")
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "日志ID")
    private Long id;

    @TableField("user_id")
    @ApiModelProperty(value = "操作用户ID")
    private Long userId;

    @TableField("operation_type")
    @ApiModelProperty(value = "操作类型", required = true)
    private String operationType;

    @TableField("operation_desc")
    @ApiModelProperty(value = "操作描述")
    private String operationDesc;

    @TableField("request_url")
    @ApiModelProperty(value = "请求URL")
    private String requestUrl;

    @TableField("request_method")
    @ApiModelProperty(value = "请求方法")
    private String requestMethod;

    @TableField("request_params")
    @ApiModelProperty(value = "请求参数")
    private String requestParams;

    @TableField("response_result")
    @ApiModelProperty(value = "响应结果")
    private String responseResult;

    @TableField("ip_address")
    @ApiModelProperty(value = "IP地址")
    private String ipAddress;

    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

