package com.server.surveyanalystserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 表单项实体类（参考 tduck）
 */
@Data
@TableName("form_item")
public class FormItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String formKey;
    
    private String formItemId;
    
    private String type;
    
    private String label;
    
    private Integer isDisplayType;
    
    private Integer isHideType;
    
    private Integer isSpecialType;
    
    private Integer showLabel;
    
    private String defaultValue;
    
    private Integer required;
    
    private String placeholder;
    
    private Long sort;
    
    private Integer span;
    
    private String scheme; // JSON 字符串
    
    private String regList; // JSON 字符串
    
    private LocalDateTime updateTime;
    
    private LocalDateTime createTime;
}

