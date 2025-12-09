package com.server.surveyanalystserver.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.OperationLog;
import com.server.surveyanalystserver.service.OperationLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员系统管理控制器
 */
@RestController
@RequestMapping("/api/admin/system")
@Api(tags = "管理员-系统管理")
public class AdminSystemController {

    @Autowired
    private OperationLogService operationLogService;

    @ApiOperation(value = "分页查询系统日志", notes = "管理员分页查询系统操作日志")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/logs")
    public Result<Page<OperationLog>> getSystemLogs(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页数量", defaultValue = "10") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam(value = "用户ID（可选）") @RequestParam(required = false) Long userId,
            @ApiParam(value = "操作类型（可选）") @RequestParam(required = false) String operationType) {
        Page<OperationLog> page = new Page<>(pageNum, pageSize);
        Page<OperationLog> result = operationLogService.getLogPage(page, userId, operationType);
        return Result.success("查询成功", result);
    }
}
