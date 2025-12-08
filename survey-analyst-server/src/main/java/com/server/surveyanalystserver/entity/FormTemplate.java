package com.server.surveyanalystserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 表单模板实体类
 */
@Data
@TableName(value = "form_template", autoResultMap = true)
@ApiModel(description = "表单模板实体")
public class FormTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @TableField("form_key")
    @ApiModelProperty(value = "模板唯一标识", required = true)
    private String formKey;

    @TableField("name")
    @ApiModelProperty(value = "模板名称", required = true)
    private String name;

    @TableField("cover_img")
    @ApiModelProperty(value = "封面图")
    private String coverImg;

    @TableField("description")
    @ApiModelProperty(value = "模板描述")
    private String description;

    @TableField("category_id")
    @ApiModelProperty(value = "模板分类ID", required = true)
    private Long categoryId;

    @TableField("status")
    @ApiModelProperty(value = "状态：0-禁用，1-启用")
    private Long status;

    @TableField("user_id")
    @ApiModelProperty(value = "创建用户ID")
    private Long userId;

    /**
     * 模板内容定义（JSON格式存储）
     */
    @TableField(value = "scheme", typeHandler = JacksonTypeHandler.class)
    @ApiModelProperty(value = "模板内容定义")
    private Definition scheme;

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

    /**
     * 模板定义内容
     */
    @Data
    public static class Definition implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "表单类型")
        private Integer formType;

        @ApiModelProperty(value = "表单项列表")
        private List<FormItem> formItems;

        @ApiModelProperty(value = "表单主题")
        private FormTheme formTheme;

        @ApiModelProperty(value = "表单逻辑")
        private FormLogic formLogic;
    }
}

