package com.server.surveyanalystserver.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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

    @ApiOperation(value = "分页查询用户列表", notes = "管理员分页查询所有用户")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public Result<Page<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getAccount, keyword)
                    .or().like(User::getEmail, keyword)
                    .or().like(User::getUsername, keyword));
        }
        
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userService.page(page, wrapper);
        return Result.success("查询成功", result);
    }

    @ApiOperation(value = "获取用户详情", notes = "根据ID获取用户详情")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success("获取成功", user);
    }

    @ApiOperation(value = "更新用户信息", notes = "管理员更新用户信息")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        userService.updateById(user);
        User updatedUser = userService.getById(id);
        return Result.success("更新成功", updatedUser);
    }

    @ApiOperation(value = "启用/禁用用户", notes = "管理员启用或禁用用户")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(status);
        userService.updateById(user);
        return Result.success(status == 1 ? "启用成功" : "禁用成功");
    }

    @ApiOperation(value = "删除用户", notes = "管理员删除用户（逻辑删除）")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success("删除成功");
    }

    @ApiOperation(value = "获取用户统计", notes = "获取用户统计数据")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/statistics")
    public Result<Object> getUserStatistics() {
        long totalUsers = userService.count();
        long activeUsers = userService.count(new LambdaQueryWrapper<User>().eq(User::getStatus, 1));
        long disabledUsers = userService.count(new LambdaQueryWrapper<User>().eq(User::getStatus, 0));
        
        java.util.Map<String, Object> statistics = new java.util.HashMap<>();
        statistics.put("totalUsers", totalUsers);
        statistics.put("activeUsers", activeUsers);
        statistics.put("disabledUsers", disabledUsers);
        
        return Result.success("获取成功", statistics);
    }
}

