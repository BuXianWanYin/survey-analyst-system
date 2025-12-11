package com.server.surveyanalystserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统计缓存表实体类
 */
@Data
@TableName("statistics_cache")
@ApiModel(description = "统计缓存实体")
public class StatisticsCache implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "ID")
    private Long id;

    @TableField("survey_id")
    @ApiModelProperty(value = "问卷ID", required = true)
    private Long surveyId;

    @TableField("form_item_id")
    @ApiModelProperty(value = "表单项ID（题目统计，字符串）")
    private String formItemId;

    @TableField("stat_type")
    @ApiModelProperty(value = "统计类型", required = true)
    private String statType;

    @TableField("stat_data")
    @ApiModelProperty(value = "统计数据（JSON格式）", required = true)
    private String statData;

    @TableField("update_time")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}

