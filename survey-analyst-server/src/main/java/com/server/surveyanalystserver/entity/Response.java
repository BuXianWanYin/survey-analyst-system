package com.server.surveyanalystserver.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
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
 * 填写记录实体类
 */
@Data
@TableName("response")
@ApiModel(description = "填写记录实体")
public class Response implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "填写记录ID")
    private Long id;

    @TableField("survey_id")
    @ApiModelProperty(value = "问卷ID", required = true)
    private Long surveyId;

    @TableField("user_id")
    @ApiModelProperty(value = "填写用户ID（匿名填写为NULL）")
    private Long userId;

    @TableField("ip_address")
    @ApiModelProperty(value = "IP地址")
    private String ipAddress;

    @TableField("device_type")
    @ApiModelProperty(value = "设备类型：PC, MOBILE")
    private String deviceType;

    @TableField("start_time")
    @ApiModelProperty(value = "开始填写时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @TableField("submit_time")
    @ApiModelProperty(value = "提交时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    @TableField("duration")
    @ApiModelProperty(value = "填写时长（秒）")
    private Integer duration;

    @TableField("status")
    @ApiModelProperty(value = "状态：COMPLETED-已完成，DRAFT-草稿")
    private String status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

