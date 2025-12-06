package com.server.surveyanalystserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("user")
@ApiModel(description = "用户实体")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "用户ID")
    private Long id;

    @TableField("account")
    @ApiModelProperty(value = "账号（用于登录，唯一）", required = true)
    private String account;

    @TableField("email")
    @ApiModelProperty(value = "邮箱（用于登录，唯一）", required = true)
    private String email;

    @TableField("username")
    @ApiModelProperty(value = "用户名（昵称，不用于登录，可重复）", required = true)
    private String username;

    @TableField("phone")
    @ApiModelProperty(value = "手机号")
    private String phone;

    @TableField("password")
    @ApiModelProperty(value = "密码（BCrypt加密）", required = true, hidden = true)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @TableField("avatar")
    @ApiModelProperty(value = "头像URL")
    private String avatar;

    @TableField("role")
    @ApiModelProperty(value = "角色：USER, ADMIN")
    private String role;

    @TableField("status")
    @ApiModelProperty(value = "状态：0-禁用，1-启用")
    private Integer status;

    @TableField(value = "create_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField("delete_flag")
    @TableLogic
    @ApiModelProperty(value = "删除标志：0-未删除，1-已删除")
    private Integer deleteFlag;
}

