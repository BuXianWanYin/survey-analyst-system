package com.server.surveyanalystserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 表单设置实体类
 */
@Data
@TableName(value = "form_setting", autoResultMap = true)
@ApiModel(description = "表单设置实体")
public class FormSetting implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;
    
    @TableField("survey_id")
    @ApiModelProperty(value = "问卷ID（逻辑外键）", required = true)
    private Long surveyId;
    
    @TableField(value = "settings", typeHandler = JacksonTypeHandler.class)
    @ApiModelProperty(value = "设置内容（JSON格式）")
    private Map<String, Object> settings;
    
    @TableField("update_time")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
    
    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}

