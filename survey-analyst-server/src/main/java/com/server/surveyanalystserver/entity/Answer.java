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
 * 填写答案实体类
 */
@Data
@TableName("answer")
@ApiModel(description = "填写答案实体")
public class Answer implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "答案ID")
    private Long id;

    @TableField("response_id")
    @ApiModelProperty(value = "填写记录ID", required = true)
    private Long responseId;

    @TableField("question_id")
    @ApiModelProperty(value = "题目ID", required = true)
    private Long questionId;

    @TableField("option_id")
    @ApiModelProperty(value = "选项ID（选择题）")
    private Long optionId;

    @TableField("content")
    @ApiModelProperty(value = "答案内容（填空题）")
    private String content;

    @TableField("file_url")
    @ApiModelProperty(value = "文件URL（文件上传题）")
    private String fileUrl;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

