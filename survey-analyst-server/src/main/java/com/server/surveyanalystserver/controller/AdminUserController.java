package com.server.surveyanalystserver.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员用户管理控制器
 */
@RestController
@RequestMapping("/api/admin/user")
@Api(tags = "管理员-用户管理")
public class AdminUserController {

    @Autowired
    private UserService userService;

    /**
     * 管理员分页查询用户列表
     * 支持按关键词和状态筛选用户
     * @param pageNum 页码，默认为1
     * @param pageSize 每页数量，默认为10
     * @param keyword 关键词，用于搜索账号、邮箱、用户名，可选
     * @param status 用户状态（1-启用，0-禁用），可选
     * @return 用户分页列表
     */
    @ApiOperation(value = "分页查询用户列表", notes = "管理员分页查询所有用户")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public Result<Page<User>> getUserList(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页数量", defaultValue = "10") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam(value = "关键词") @RequestParam(required = false) String keyword,
            @ApiParam(value = "状态") @RequestParam(required = false) Integer status) {
        Page<User> page = new Page<>(pageNum, pageSize);
        Page<User> result = userService.getAdminUserList(page, keyword, status);
        return Result.success("查询成功", result);
    }

    @ApiOperation(value = "获取用户详情", notes = "根据ID获取用户详情")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success("获取成功", user);
    }

    /**
     * 管理员更新用户信息
     * 更新指定用户的信息，包括基本信息（不包括密码）
     * @param id 用户ID
     * @param user 包含更新信息的用户对象
     * @return 更新后的用户信息
     */
    @ApiOperation(value = "更新用户信息", notes = "管理员更新用户信息")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Result<User> updateUser(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long id,
            @ApiParam(value = "用户信息", required = true) @RequestBody User user) {
        User updatedUser = userService.updateAdminUser(id, user);
        return Result.success("更新成功", updatedUser);
    }

    @ApiOperation(value = "启用/禁用用户", notes = "管理员启用或禁用用户")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long id,
            @ApiParam(value = "状态", required = true) @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return Result.success(status == 1 ? "启用成功" : "禁用成功");
    }

    @ApiOperation(value = "删除用户", notes = "管理员删除用户（逻辑删除）")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success("删除成功");
    }

    @ApiOperation(value = "创建用户", notes = "管理员创建新用户")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Result<User> createUser(@ApiParam(value = "用户信息", required = true) @RequestBody User user) {
        User createdUser = userService.createAdminUser(user);
        return Result.success("创建成功", createdUser);
    }

    @ApiOperation(value = "获取用户统计", notes = "获取用户统计数据")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/statistics")
    public Result<Object> getUserStatistics() {
        Object statistics = userService.getUserStatistics();
        return Result.success("获取成功", statistics);
    }
}

