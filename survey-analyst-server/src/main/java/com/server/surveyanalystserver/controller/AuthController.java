package com.server.surveyanalystserver.controller;

import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.entity.dto.UserLoginDTO;
import com.server.surveyanalystserver.entity.dto.UserRegisterDTO;
import com.server.surveyanalystserver.service.UserService;
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

    @ApiOperation(value = "用户注册", notes = "用户注册接口（需要验证码）")
    @PostMapping("/register")
    public Result<User> register(@RequestBody Map<String, String> params) {
        String account = params.get("account");
        String email = params.get("email");
        String username = params.get("username");
        String password = params.get("password");
        String code = params.get("code");
        
        if (account == null || account.trim().isEmpty()) {
            return Result.error("账号不能为空");
        }
        if (email == null || email.trim().isEmpty()) {
            return Result.error("邮箱不能为空");
        }
        if (username == null || username.trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return Result.error("密码不能为空");
        }
        if (code == null || code.trim().isEmpty()) {
            return Result.error("验证码不能为空");
        }
        
        // 验证验证码
        try {
            boolean valid = userService.verifyCode(email.trim(), code.trim(), "REGISTER");
            if (!valid) {
                return Result.error("验证码错误或已过期");
            }
        } catch (Exception e) {
            return Result.error("验证码验证失败：" + e.getMessage());
        }
        
        // 注册用户
        try {
            UserRegisterDTO dto = new UserRegisterDTO();
            dto.setAccount(account.trim());
            dto.setEmail(email.trim());
            dto.setUsername(username.trim());
            dto.setPassword(password.trim());
            
            User user = userService.register(dto);
            
            return Result.success("注册成功", user);
        } catch (Exception e) {
            return Result.error("注册失败：" + e.getMessage());
        }
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

    @ApiOperation(value = "发送验证码", notes = "发送验证码到指定邮箱（注册或重置密码）")
    @PostMapping("/send-verification-code")
    public Result<Void> sendVerificationCode(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String type = params.get("type"); // REGISTER 或 RESET_PASSWORD
        
        if (email == null || email.trim().isEmpty()) {
            return Result.error("邮箱不能为空");
        }
        
        if (type == null || (!type.equals("REGISTER") && !type.equals("RESET_PASSWORD"))) {
            return Result.error("验证码类型无效");
        }
        
        try {
            boolean success = userService.sendVerificationCode(email.trim(), type);
            if (success) {
                return Result.success("验证码已发送到您的邮箱，请查收");
            } else {
                return Result.error("发送验证码失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation(value = "重置密码", notes = "使用验证码重置密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String code = params.get("code");
        String newPassword = params.get("newPassword");
        
        if (email == null || email.trim().isEmpty()) {
            return Result.error("邮箱不能为空");
        }
        
        if (code == null || code.trim().isEmpty()) {
            return Result.error("验证码不能为空");
        }
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return Result.error("新密码不能为空");
        }
        
        if (newPassword.length() < 6) {
            return Result.error("密码长度不能少于6位");
        }
        
        try {
            boolean success = userService.resetPasswordByCode(email.trim(), code.trim(), newPassword.trim());
            if (success) {
                return Result.success("密码重置成功，请使用新密码登录");
            } else {
                return Result.error("密码重置失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
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

