package com.server.surveyanalystserver.controller.user;

import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.OperationLog;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.service.OperationLogService;
import com.server.surveyanalystserver.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/user")
@Api(tags = "用户管理")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private OperationLogService operationLogService;

    @ApiOperation(value = "获取当前用户信息", notes = "获取当前登录用户信息")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/current")
    public Result<User> getCurrentUser() {
        User user = userService.getCurrentUser();
        return Result.success("获取成功", user);
    }

    @ApiOperation(value = "更新用户信息", notes = "更新当前用户信息")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PutMapping("/info")
    public Result<User> updateUserInfo(@RequestBody User user) {
        User updatedUser = userService.updateUserInfo(user);
        return Result.success("更新成功", updatedUser);
    }

    @ApiOperation(value = "修改密码", notes = "修改当前用户密码")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        userService.changePassword(oldPassword, newPassword);
        return Result.success("密码修改成功");
    }

    @ApiOperation(value = "用户登出", notes = "用户登出接口，记录登出日志")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        User currentUser = userService.getCurrentUser();
        
        // 记录登出日志
        try {
            OperationLog log = new OperationLog();
            log.setUserId(currentUser.getId());
            log.setOperationType("登出");
            log.setOperationDesc("用户登出：" + currentUser.getUsername());
            log.setRequestUrl("/api/user/logout");
            log.setRequestMethod("POST");
            log.setIpAddress(getIpAddress(request));
            log.setCreateTime(LocalDateTime.now());
            operationLogService.saveLog(log);
        } catch (Exception e) {
            // 日志记录失败不影响登出
        }
        
        return Result.success("登出成功");
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

