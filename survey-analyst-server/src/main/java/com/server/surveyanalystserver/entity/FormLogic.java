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
import java.util.List;
import java.util.Map;

/**
 * 表单逻辑实体类
 */
@Data
@TableName(value = "form_logic", autoResultMap = true)
@ApiModel(description = "表单逻辑实体")
public class FormLogic implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;
    
    @TableField("survey_id")
    @ApiModelProperty(value = "问卷ID（逻辑外键）", required = true)
    private Long surveyId;
    
    @TableField(value = "scheme", typeHandler = JacksonTypeHandler.class)
    @ApiModelProperty(value = "逻辑定义（JSON格式）")
    private List<Map<String, Object>> scheme;
    
    @TableField("update_time")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
    
    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}

