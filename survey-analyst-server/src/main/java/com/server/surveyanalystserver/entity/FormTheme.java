package com.server.surveyanalystserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 表单主题实体类
 */
@Data
@TableName("form_theme")
@ApiModel(description = "表单主题实体")
public class FormTheme {
    
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;
    
    @TableField("survey_id")
    @ApiModelProperty(value = "问卷ID（逻辑外键）", required = true)
    private Long surveyId;
    
    @TableField("submit_btn_text")
    @ApiModelProperty(value = "提交按钮文字")
    private String submitBtnText;
    
    @TableField("logo_img")
    @ApiModelProperty(value = "logo图片")
    private String logoImg;
    
    @TableField("logo_position")
    @ApiModelProperty(value = "logo位置")
    private String logoPosition;
    
    @TableField("logo_size")
    @ApiModelProperty(value = "logo大小（像素）")
    private Integer logoSize;
    
    @TableField("background_color")
    @ApiModelProperty(value = "背景颜色")
    private String backgroundColor;
    
    @TableField("background_img")
    @ApiModelProperty(value = "背景图片")
    private String backgroundImg;
    
    @TableField("show_title")
    @ApiModelProperty(value = "是否显示标题")
    private Boolean showTitle;
    
    @TableField("show_describe")
    @ApiModelProperty(value = "是否显示描述语")
    private Boolean showDescribe;
    
    @TableField("theme_color")
    @ApiModelProperty(value = "主题颜色")
    private String themeColor;
    
    @TableField("show_number")
    @ApiModelProperty(value = "显示序号")
    private Boolean showNumber;
    
    @TableField("show_submit_btn")
    @ApiModelProperty(value = "显示提交按钮")
    private Boolean showSubmitBtn;
    
    @TableField("head_img_url")
    @ApiModelProperty(value = "头部图片")
    private String headImgUrl;
    
    @TableField("head_img_height")
    @ApiModelProperty(value = "头图高度（像素）")
    private Integer headImgHeight;
    
    @TableField("btn_font_size")
    @ApiModelProperty(value = "按钮字体大小（像素）")
    private Integer btnFontSize;
    
    @TableField("btn_width")
    @ApiModelProperty(value = "按钮宽度（像素）")
    private Integer btnWidth;
    
    @TableField("btn_height")
    @ApiModelProperty(value = "按钮高度（像素）")
    private Integer btnHeight;
    
    @TableField("update_time")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
    
    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}

