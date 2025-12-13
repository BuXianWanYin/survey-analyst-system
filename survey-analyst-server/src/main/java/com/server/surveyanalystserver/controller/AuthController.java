package com.server.surveyanalystserver.controller;

import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.entity.dto.UserLoginDTO;
import com.server.surveyanalystserver.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@Api(tags = "认证管理")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册接口
     * 接收注册参数，验证验证码后创建新用户账号
     * @param params 注册参数Map，包含account、email、username、password、code等字段
     * @return 注册成功后的用户信息
     */
    @ApiOperation(value = "用户注册", notes = "用户注册接口（需要验证码）")
    @PostMapping("/register")
    public Result<User> register(@RequestBody Map<String, String> params) {
        User user = userService.registerWithMap(params);
        return Result.success("注册成功", user);
    }

    /**
     * 用户登录接口
     * 验证用户身份后返回JWT Token和用户信息，用于后续请求认证
     * @param dto 登录信息DTO，包含登录名和密码
     * @param request HTTP请求对象，用于获取IP地址等信息
     * @return 登录结果，包含token和user信息
     */
    @ApiOperation(value = "用户登录", notes = "用户登录接口，返回JWT Token")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody UserLoginDTO dto, HttpServletRequest request) {
        Map<String, Object> data = userService.loginWithResult(dto.getLoginName(), dto.getPassword(), request);
        return Result.success("登录成功", data);
    }

    /**
     * 发送验证码接口
     * 向指定邮箱发送验证码，用于注册或重置密码
     * @param params 参数Map，包含email（邮箱）和type（类型：REGISTER或RESET_PASSWORD）
     * @return 发送结果
     */
    @ApiOperation(value = "发送验证码", notes = "发送验证码到指定邮箱（注册或重置密码）")
    @PostMapping("/send-verification-code")
    public Result<Void> sendVerificationCode(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String type = params.get("type");
        boolean success = userService.sendVerificationCode(email, type);
        if (success) {
            return Result.success("验证码已发送到您的邮箱，请查收");
        } else {
            return Result.error("发送验证码失败");
        }
    }

    /**
     * 重置密码接口
     * 使用验证码验证身份后重置用户密码
     * @param params 参数Map，包含email（邮箱）、code（验证码）和newPassword（新密码）
     * @return 重置结果
     */
    @ApiOperation(value = "重置密码", notes = "使用验证码重置密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String code = params.get("code");
        String newPassword = params.get("newPassword");
        boolean success = userService.resetPasswordByCode(email, code, newPassword);
        if (success) {
            return Result.success("密码重置成功，请使用新密码登录");
        } else {
            return Result.error("密码重置失败");
        }
    }
}

