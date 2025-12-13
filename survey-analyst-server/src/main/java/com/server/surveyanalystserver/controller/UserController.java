package com.server.surveyanalystserver.controller;

import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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

    /**
     * 获取当前登录用户信息
     * 从Spring Security上下文中获取当前认证用户的信息
     * @return 当前用户信息
     */
    @ApiOperation(value = "获取当前用户信息", notes = "获取当前登录用户信息")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/current")
    public Result<User> getCurrentUser() {
        User user = userService.getCurrentUser();
        return Result.success("获取成功", user);
    }

    /**
     * 更新当前用户信息
     * 更新当前登录用户的个人信息（不包括密码）
     * @param user 包含更新信息的用户对象
     * @return 更新后的用户信息
     */
    @ApiOperation(value = "更新用户信息", notes = "更新当前用户信息")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PutMapping("/info")
    public Result<User> updateUserInfo(@RequestBody User user) {
        User updatedUser = userService.updateUserInfo(user);
        return Result.success("更新成功", updatedUser);
    }

    /**
     * 修改当前用户密码
     * 验证旧密码后更新为新密码
     * @param params 参数Map，包含oldPassword（旧密码）和newPassword（新密码）
     * @return 修改结果
     */
    @ApiOperation(value = "修改密码", notes = "修改当前用户密码")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        userService.changePassword(oldPassword, newPassword);
        return Result.success("密码修改成功");
    }

    /**
     * 用户登出接口
     * 清除用户认证信息，记录登出日志
     * @param request HTTP请求对象，用于获取IP地址等信息
     * @return 登出结果
     */
    @ApiOperation(value = "用户登出", notes = "用户登出接口，记录登出日志")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        userService.logout(request);
        return Result.success("登出成功");
    }
}

