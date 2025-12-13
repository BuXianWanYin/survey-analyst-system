package com.server.surveyanalystserver.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.dto.OperationLogVO;
import com.server.surveyanalystserver.service.OperationLogService;
import com.server.surveyanalystserver.service.SystemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 管理员系统管理控制器
 */
@RestController
@RequestMapping("/api/admin/system")
@Api(tags = "管理员-系统管理")
public class AdminSystemController {

    @Autowired
    private SystemService systemService;

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 获取系统概览数据
     * 获取系统整体统计数据，包括用户数、问卷数、填写记录数等
     * @return 系统概览数据对象
     */
    @ApiOperation(value = "获取系统概览", notes = "获取系统统计数据概览")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/overview")
    public Result<Object> getSystemOverview() {
        Object overview = systemService.getSystemOverview();
        return Result.success("获取成功", overview);
    }

    @ApiOperation(value = "获取今日新增问卷数", notes = "获取今日新增的问卷数量")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/today-surveys")
    public Result<Object> getTodaySurveys() {
        Object result = systemService.getTodaySurveys();
        return Result.success("获取成功", result);
    }

    /**
     * 获取问卷创建趋势数据
     * 按时间范围统计问卷创建数量，用于趋势分析
     * @param timeRange 时间范围（7d-最近7天，30d-最近30天，90d-最近90天，1y-最近1年），默认为30d
     * @return 问卷创建趋势数据列表
     */
    @ApiOperation(value = "获取问卷创建趋势", notes = "获取按时间统计的问卷创建趋势数据")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/survey-create-trend")
    public Result<Object> getSurveyCreateTrend(
            @ApiParam(value = "时间范围：7d-最近7天，30d-最近30天，90d-最近90天，1y-最近1年", defaultValue = "30d")
            @RequestParam(defaultValue = "30d") String timeRange) {
        Object result = systemService.getSurveyCreateTrend(timeRange);
        return Result.success("获取成功", result);
    }

    @ApiOperation(value = "获取问卷填写趋势", notes = "获取按时间统计的问卷填写趋势数据（所有提交的都是已完成的）")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/response-trend")
    public Result<Object> getResponseTrend(
            @ApiParam(value = "时间范围：7d-最近7天，30d-最近30天，90d-最近90天，1y-最近1年", defaultValue = "30d")
            @RequestParam(defaultValue = "30d") String timeRange) {
        Object result = systemService.getResponseTrend(timeRange);
        return Result.success("获取成功", result);
    }

    @ApiOperation(value = "获取登录趋势", notes = "获取按时间统计的用户登录趋势数据")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/login-trend")
    public Result<Object> getLoginTrend(
            @ApiParam(value = "时间范围：7d-最近7天，30d-最近30天，90d-最近90天，1y-最近1年", defaultValue = "30d")
            @RequestParam(defaultValue = "30d") String timeRange) {
        Object result = systemService.getLoginTrend(timeRange);
        return Result.success("获取成功", result);
    }

    @ApiOperation(value = "获取平均填写时长趋势", notes = "获取按时间统计的平均填写时长趋势数据")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/response-duration-trend")
    public Result<Object> getResponseDurationTrend(
            @ApiParam(value = "时间范围：7d-最近7天，30d-最近30天，90d-最近90天，1y-最近1年", defaultValue = "30d")
            @RequestParam(defaultValue = "30d") String timeRange) {
        Object result = systemService.getResponseDurationTrend(timeRange);
        return Result.success("获取成功", result);
    }

    @ApiOperation(value = "分页查询系统日志", notes = "管理员分页查询系统操作日志（包含用户名）")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/logs")
    public Result<Page<OperationLogVO>> getSystemLogs(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页数量", defaultValue = "10") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam(value = "用户ID（可选）") @RequestParam(required = false) Long userId,
            @ApiParam(value = "操作类型（可选）") @RequestParam(required = false) String operationType,
            @ApiParam(value = "开始时间（可选，格式：yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss）") @RequestParam(required = false) String startTime,
            @ApiParam(value = "结束时间（可选，格式：yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss）") @RequestParam(required = false) String endTime) {
        Page<OperationLogVO> page = new Page<>(pageNum, pageSize);
        LocalDateTime startDateTime = operationLogService.parseDateTime(startTime);
        LocalDateTime endDateTime = operationLogService.parseEndDateTime(endTime);
        Page<OperationLogVO> result = operationLogService.getLogPageWithUsername(page, userId, operationType, startDateTime, endDateTime);
        return Result.success("查询成功", result);
    }
}
