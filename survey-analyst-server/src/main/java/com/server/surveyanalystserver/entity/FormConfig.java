package com.server.surveyanalystserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 表单配置实体类（参考 tduck）
 */
@Data
@TableName("form_config")
public class FormConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String formKey;
    
    private Long surveyId;
    
    private String name;
    
    private String description;
    
    private Long userId;
    
    private String type;
    
    private Integer status;
    
    private Integer isDeleted;
    
    private LocalDateTime updateTime;
    
    private LocalDateTime createTime;
}

