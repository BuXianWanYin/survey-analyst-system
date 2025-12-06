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
 * 题目实体类
 */
@Data
@TableName("question")
@ApiModel(description = "题目实体")
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "题目ID")
    private Long id;

    @TableField("survey_id")
    @ApiModelProperty(value = "问卷ID", required = true)
    private Long surveyId;

    @TableField("type")
    @ApiModelProperty(value = "题型：SINGLE_CHOICE, MULTIPLE_CHOICE, TEXT, TEXTAREA, RATING, SORT, MATRIX, FILE, DATE", required = true)
    private String type;

    @TableField("title")
    @ApiModelProperty(value = "题目标题", required = true)
    private String title;

    @TableField("description")
    @ApiModelProperty(value = "题目描述")
    private String description;

    @TableField("required")
    @ApiModelProperty(value = "是否必填：0-否，1-是")
    private Integer required;

    @TableField("order_num")
    @ApiModelProperty(value = "排序号")
    private Integer orderNum;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

