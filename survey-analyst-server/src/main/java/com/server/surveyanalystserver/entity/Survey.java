package com.server.surveyanalystserver.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 问卷实体类
 */
@Data
@TableName("survey")
@ApiModel(description = "问卷实体")
public class Survey implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "问卷ID")
    private Long id;

    @TableField("user_id")
    @ApiModelProperty(value = "创建用户ID", required = true)
    private Long userId;

    @TableField("title")
    @ApiModelProperty(value = "问卷标题", required = true)
    private String title;

    @TableField("description")
    @ApiModelProperty(value = "问卷描述")
    private String description;

    @TableField("status")
    @ApiModelProperty(value = "状态：DRAFT-草稿，PUBLISHED-收集中，ENDED-已结束")
    private String status;

    @TableField("access_type")
    @ApiModelProperty(value = "访问类型：PUBLIC-公开，PASSWORD-密码访问")
    private String accessType;

    @TableField("password")
    @ApiModelProperty(value = "访问密码")
    private String password;

    @TableField("max_responses")
    @ApiModelProperty(value = "最大填写数")
    private Integer maxResponses;

    @TableField("start_time")
    @ApiModelProperty(value = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @TableField("end_time")
    @ApiModelProperty(value = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField("delete_flag")
    @TableLogic
    @ApiModelProperty(value = "删除标志：0-未删除，1-已删除")
    private Integer deleteFlag;

    @TableField(exist = false)
    @ApiModelProperty(value = "答卷数量（非数据库字段）")
    private Long responseCount;
}

