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
 * 表单配置实体类（参考 tduck）
 */
@Data
@TableName("form_config")
@ApiModel(description = "表单配置实体")
public class FormConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @TableField("form_key")
    @ApiModelProperty(value = "表单唯一标识", required = true)
    private String formKey;

    @TableField("survey_id")
    @ApiModelProperty(value = "问卷ID（逻辑外键）", required = true)
    private Long surveyId;

    @TableField("name")
    @ApiModelProperty(value = "表单名称")
    private String name;

    @TableField("description")
    @ApiModelProperty(value = "表单描述")
    private String description;

    @TableField("user_id")
    @ApiModelProperty(value = "创建用户ID", required = true)
    private Long userId;

    @TableField("type")
    @ApiModelProperty(value = "表单类型")
    private String type;

    @TableField("status")
    @ApiModelProperty(value = "状态：0-禁用，1-启用")
    private Integer status;

    @TableField("is_deleted")
    @ApiModelProperty(value = "删除标志：0-未删除，1-已删除")
    private Integer isDeleted;

    @TableField("update_time")
    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

