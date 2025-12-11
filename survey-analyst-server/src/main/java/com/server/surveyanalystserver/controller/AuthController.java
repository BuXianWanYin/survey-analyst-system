package com.server.surveyanalystserver.controller;

import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.entity.dto.UserLoginDTO;
import com.server.surveyanalystserver.entity.dto.UserRegisterDTO;
import com.server.surveyanalystserver.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
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

    @ApiOperation(value = "用户注册", notes = "用户注册接口")
    @PostMapping("/register")
    public Result<User> register(@RequestBody UserRegisterDTO dto) {
        User user = userService.register(dto);
        return Result.success("注册成功", user);
    }

    @Autowired
    private com.server.surveyanalystserver.service.OperationLogService operationLogService;

    @ApiOperation(value = "用户登录", notes = "用户登录接口，返回JWT Token")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody UserLoginDTO dto, HttpServletRequest request) {
        String token = userService.login(dto.getLoginName(), dto.getPassword());
        
        // 根据登录名查询用户信息（不包含密码）
        // 注意：这里直接使用userService.getOne，返回的用户信息中手机号是加密的
        // 如果需要解密，应该使用userService.getCurrentUser()，但登录时还没有Token
        // 所以这里先获取用户，后续如果需要显示手机号，可以在前端调用getCurrentUser接口
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(User::getAccount, dto.getLoginName()).or().eq(User::getEmail, dto.getLoginName()));
        User user = userService.getOne(wrapper);
        // 清除密码信息
        user.setPassword(null);
        // 注意：手机号在数据库中可能是加密的，这里不返回手机号，前端需要时调用getCurrentUser接口
        user.setPhone(null);
        
        // 记录登录日志
        try {
            com.server.surveyanalystserver.entity.OperationLog log = new com.server.surveyanalystserver.entity.OperationLog();
            log.setUserId(user.getId());
            log.setOperationType("登录");
            log.setOperationDesc("用户登录：" + user.getUsername());
            log.setRequestUrl("/api/auth/login");
            log.setRequestMethod("POST");
            log.setIpAddress(getIpAddress(request));
            log.setCreateTime(java.time.LocalDateTime.now());
            operationLogService.saveLog(log);
        } catch (Exception e) {
            // 日志记录失败不影响登录
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        
        return Result.success("登录成功", data);
    }

    @ApiOperation(value = "发送密码重置邮件", notes = "发送密码重置邮件到指定邮箱")
    @PostMapping("/forgot-password")
    public Result<Void> forgotPassword(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        if (email == null || email.trim().isEmpty()) {
            return Result.error("邮箱不能为空");
        }
        
        try {
            userService.sendPasswordResetEmail(email.trim());
            // 为了安全，无论用户是否存在都返回成功
            return Result.success("如果该邮箱已注册，密码重置链接已发送到您的邮箱，请查收");
        } catch (Exception e) {
            return Result.error("发送密码重置邮件失败：" + e.getMessage());
        }
    }

    @ApiOperation(value = "重置密码", notes = "使用重置令牌重置密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, String> params) {
        String token = params.get("token");
        String newPassword = params.get("newPassword");
        
        if (token == null || token.trim().isEmpty()) {
            return Result.error("重置令牌不能为空");
        }
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return Result.error("新密码不能为空");
        }
        
        if (newPassword.length() < 6) {
            return Result.error("密码长度不能少于6位");
        }
        
        try {
            boolean success = userService.resetPassword(token.trim(), newPassword.trim());
            if (success) {
                return Result.success("密码重置成功，请使用新密码登录");
            } else {
                return Result.error("密码重置失败");
            }
        } catch (Exception e) {
            return Result.error("密码重置失败：" + e.getMessage());
        }
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

