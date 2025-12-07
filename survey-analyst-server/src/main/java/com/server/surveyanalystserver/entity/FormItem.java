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
 * 表单项实体类（参考 tduck）
 */
@Data
@TableName("form_item")
@ApiModel(description = "表单项实体")
public class FormItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @TableField("form_key")
    @ApiModelProperty(value = "表单唯一标识", required = true)
    private String formKey;

    @TableField("form_item_id")
    @ApiModelProperty(value = "表单项唯一标识", required = true)
    private String formItemId;

    @TableField("question_id")
    @ApiModelProperty(value = "题目ID（逻辑外键，关联question表）")
    private Long questionId;

    @TableField("type")
    @ApiModelProperty(value = "表单项类型", required = true)
    private String type;

    @TableField("label")
    @ApiModelProperty(value = "表单项标签（标题）", required = true)
    private String label;

    @TableField("is_display_type")
    @ApiModelProperty(value = "显示类型")
    private Integer isDisplayType;

    @TableField("is_hide_type")
    @ApiModelProperty(value = "隐藏类型")
    private Integer isHideType;

    @TableField("is_special_type")
    @ApiModelProperty(value = "特殊类型")
    private Integer isSpecialType;

    @TableField("show_label")
    @ApiModelProperty(value = "是否显示标签")
    private Integer showLabel;

    @TableField("default_value")
    @ApiModelProperty(value = "默认值")
    private String defaultValue;

    @TableField("required")
    @ApiModelProperty(value = "是否必填：0-否，1-是")
    private Integer required;

    @TableField("placeholder")
    @ApiModelProperty(value = "占位符")
    private String placeholder;

    @TableField("sort")
    @ApiModelProperty(value = "排序号")
    private Long sort;

    @TableField("span")
    @ApiModelProperty(value = "栅格占位")
    private Integer span;

    @TableField("scheme")
    @ApiModelProperty(value = "配置方案（JSON字符串）")
    private String scheme;

    @TableField("reg_list")
    @ApiModelProperty(value = "校验规则列表（JSON字符串）")
    private String regList;

    @TableField("update_time")
    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

