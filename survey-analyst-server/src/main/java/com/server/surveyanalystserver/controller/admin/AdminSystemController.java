package com.server.surveyanalystserver.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.Response;
import com.server.surveyanalystserver.entity.Survey;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.service.OperationLogService;
import com.server.surveyanalystserver.service.ResponseService;
import com.server.surveyanalystserver.service.SurveyService;
import com.server.surveyanalystserver.service.user.UserService;
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

    @Autowired
    private UserService userService;

    @Autowired
    private SurveyService surveyService;

    @Autowired
    private ResponseService responseService;

    @ApiOperation(value = "获取系统概览", notes = "获取系统统计数据概览")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/overview")
    public Result<Object> getSystemOverview() {
        java.util.Map<String, Object> overview = new java.util.HashMap<>();
        
        // 用户统计
        long totalUsers = userService.count();
        long activeUsers = userService.count(new LambdaQueryWrapper<User>().eq(User::getStatus, 1));
        overview.put("totalUsers", totalUsers);
        overview.put("activeUsers", activeUsers);
        
        // 问卷统计
        long totalSurveys = surveyService.count();
        long publishedSurveys = surveyService.count(new LambdaQueryWrapper<Survey>().eq(Survey::getStatus, "PUBLISHED"));
        overview.put("totalSurveys", totalSurveys);
        overview.put("publishedSurveys", publishedSurveys);
        
        // 填写统计
        long totalResponses = responseService.count();
        long completedResponses = responseService.count(new LambdaQueryWrapper<Response>().eq(Response::getStatus, "COMPLETED"));
        overview.put("totalResponses", totalResponses);
        overview.put("completedResponses", completedResponses);
        
        return Result.success("获取成功", overview);
    }

    @ApiOperation(value = "分页查询系统日志", notes = "管理员分页查询系统操作日志（包含用户名）")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/logs")
    public Result<Page<com.server.surveyanalystserver.entity.dto.OperationLogVO>> getSystemLogs(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页数量", defaultValue = "10") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam(value = "用户ID（可选）") @RequestParam(required = false) Long userId,
            @ApiParam(value = "操作类型（可选）") @RequestParam(required = false) String operationType,
            @ApiParam(value = "开始时间（可选，格式：yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss）") @RequestParam(required = false) String startTime,
            @ApiParam(value = "结束时间（可选，格式：yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss）") @RequestParam(required = false) String endTime) {
        Page<com.server.surveyanalystserver.entity.dto.OperationLogVO> page = 
            new Page<>(pageNum, pageSize);
        
        // 解析时间参数
        java.time.LocalDateTime startDateTime = null;
        java.time.LocalDateTime endDateTime = null;
        try {
            if (startTime != null && !startTime.isEmpty()) {
                if (startTime.length() == 10) {
                    // 只有日期，设置为当天的00:00:00
                    startDateTime = java.time.LocalDate.parse(startTime, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        .atStartOfDay();
                } else {
                    startDateTime = java.time.LocalDateTime.parse(startTime, 
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
            }
            if (endTime != null && !endTime.isEmpty()) {
                if (endTime.length() == 10) {
                    // 只有日期，设置为当天的23:59:59
                    endDateTime = java.time.LocalDate.parse(endTime, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        .atTime(23, 59, 59);
                } else {
                    endDateTime = java.time.LocalDateTime.parse(endTime, 
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
            }
        } catch (Exception e) {
            return Result.error("时间格式错误，请使用 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss 格式");
        }
        
        Page<com.server.surveyanalystserver.entity.dto.OperationLogVO> result = 
            operationLogService.getLogPageWithUsername(page, userId, operationType, startDateTime, endDateTime);
        return Result.success("查询成功", result);
    }
}
