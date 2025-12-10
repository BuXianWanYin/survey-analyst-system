package com.server.surveyanalystserver.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 填写记录VO（包含问卷名称、发布用户名称、填写用户名称）
 */
@Data
@ApiModel(description = "填写记录VO")
public class ResponseVO {

    @ApiModelProperty(value = "填写记录ID")
    private Long id;

    @ApiModelProperty(value = "问卷ID")
    private Long surveyId;

    @ApiModelProperty(value = "问卷名称")
    private String surveyTitle;

    @ApiModelProperty(value = "发布用户ID")
    private Long publisherId;

    @ApiModelProperty(value = "发布用户名称")
    private String publisherName;

    @ApiModelProperty(value = "填写用户ID（匿名填写为NULL）")
    private Long userId;

    @ApiModelProperty(value = "填写用户名称")
    private String userName;

    @ApiModelProperty(value = "IP地址")
    private String ipAddress;

    @ApiModelProperty(value = "设备类型：PC, MOBILE")
    private String deviceType;

    @ApiModelProperty(value = "开始填写时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "提交时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    @ApiModelProperty(value = "填写时长（秒）")
    private Integer duration;

    @ApiModelProperty(value = "状态：COMPLETED-已完成，DRAFT-草稿")
    private String status;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

