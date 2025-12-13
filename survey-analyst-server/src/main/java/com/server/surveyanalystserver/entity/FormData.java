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
 * 表单数据实体类
 */
@Data
@TableName(value = "form_data", autoResultMap = true)
@ApiModel(description = "表单数据实体")
public class FormData implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;
    
    @TableField("form_key")
    @ApiModelProperty(value = "表单key", required = true)
    private String formKey;
    
    @TableField("serial_number")
    @ApiModelProperty(value = "序号")
    private Integer serialNumber;
    
    @TableField(value = "original_data", typeHandler = JacksonTypeHandler.class)
    @ApiModelProperty(value = "填写结果原始数据（JSON格式）")
    private Map<String, Object> originalData;
    
    @TableField("submit_browser")
    @ApiModelProperty(value = "提交浏览器")
    private String submitBrowser;
    
    @TableField("submit_request_ip")
    @ApiModelProperty(value = "请求IP")
    private String submitRequestIp;
    
    @TableField("start_time")
    @ApiModelProperty(value = "开始填写时间")
    private LocalDateTime startTime;
    
    @TableField("complete_time")
    @ApiModelProperty(value = "完成时间（秒）")
    private Integer completeTime;
    
    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}

