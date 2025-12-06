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
 * 选项实体类
 */
@Data
@TableName("`option`")
@ApiModel(description = "选项实体")
public class Option implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "选项ID")
    private Long id;

    @TableField("question_id")
    @ApiModelProperty(value = "题目ID", required = true)
    private Long questionId;

    @TableField("content")
    @ApiModelProperty(value = "选项内容", required = true)
    private String content;

    @TableField("image_url")
    @ApiModelProperty(value = "选项图片URL")
    private String imageUrl;

    @TableField("order_num")
    @ApiModelProperty(value = "排序号")
    private Integer orderNum;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

