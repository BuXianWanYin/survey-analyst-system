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
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(User::getAccount, dto.getLoginName()).or().eq(User::getEmail, dto.getLoginName()));
        User user = userService.getOne(wrapper);
        // 清除密码信息
        user.setPassword(null);
        
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

