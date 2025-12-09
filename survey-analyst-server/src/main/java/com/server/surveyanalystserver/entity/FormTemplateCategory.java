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
 * 表单模板分类实体类
 */
@Data
@TableName("form_template_category")
@ApiModel(description = "表单模板分类实体")
public class FormTemplateCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @TableField("user_id")
    @ApiModelProperty(value = "用户ID：NULL-系统分类，有值-用户自定义分类")
    private Long userId;

    @TableField("name")
    @ApiModelProperty(value = "分类名称", required = true)
    private String name;

    @TableField("sort")
    @ApiModelProperty(value = "排序号")
    private Integer sort;

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

