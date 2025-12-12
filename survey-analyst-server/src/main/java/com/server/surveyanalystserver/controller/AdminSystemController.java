package com.server.surveyanalystserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.OperationLog;
import com.server.surveyanalystserver.entity.Response;
import com.server.surveyanalystserver.entity.Survey;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.service.OperationLogService;
import com.server.surveyanalystserver.service.ResponseService;
import com.server.surveyanalystserver.service.SurveyService;
import com.server.surveyanalystserver.service.UserService;
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

    @ApiOperation(value = "获取今日新增问卷数", notes = "获取今日新增的问卷数量")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/today-surveys")
    public Result<Object> getTodaySurveys() {
        java.time.LocalDateTime todayStart = java.time.LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        java.time.LocalDateTime todayEnd = todayStart.plusDays(1);
        
        long todaySurveys = surveyService.count(
            new LambdaQueryWrapper<Survey>()
                .ge(Survey::getCreateTime, todayStart)
                .lt(Survey::getCreateTime, todayEnd)
        );
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("todaySurveys", todaySurveys);
        return Result.success("获取成功", result);
    }

    @ApiOperation(value = "获取问卷创建趋势", notes = "获取按时间统计的问卷创建趋势数据")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/survey-create-trend")
    public Result<Object> getSurveyCreateTrend(
            @ApiParam(value = "时间范围：7d-最近7天，30d-最近30天，90d-最近90天，1y-最近1年", defaultValue = "30d")
            @RequestParam(defaultValue = "30d") String timeRange) {
        java.time.LocalDateTime startTime = null;
        java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        if ("7d".equals(timeRange)) {
            startTime = java.time.LocalDateTime.now().minusDays(7);
        } else if ("30d".equals(timeRange)) {
            startTime = java.time.LocalDateTime.now().minusDays(30);
        } else if ("90d".equals(timeRange)) {
            startTime = java.time.LocalDateTime.now().minusDays(90);
        } else if ("1y".equals(timeRange)) {
            startTime = java.time.LocalDateTime.now().minusYears(1);
        } else {
            startTime = java.time.LocalDateTime.now().minusDays(30);
        }
        
        java.util.List<Survey> surveys = surveyService.list(
            new LambdaQueryWrapper<Survey>()
                .ge(Survey::getCreateTime, startTime)
                .orderByAsc(Survey::getCreateTime)
        );
        
        // 按日期分组统计
        java.util.Map<String, Long> trendMap = new java.util.HashMap<>();
        for (Survey survey : surveys) {
            if (survey.getCreateTime() != null) {
                String date = survey.getCreateTime().toLocalDate().format(dateFormatter);
                trendMap.put(date, trendMap.getOrDefault(date, 0L) + 1);
            }
        }
        
        // 生成完整日期序列
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        java.time.LocalDate currentDate = startTime.toLocalDate();
        java.time.LocalDate endDate = java.time.LocalDate.now();
        
        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.format(dateFormatter);
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("date", dateStr);
            item.put("count", trendMap.getOrDefault(dateStr, 0L));
            result.add(item);
            currentDate = currentDate.plusDays(1);
        }
        
        return Result.success("获取成功", result);
    }

    @ApiOperation(value = "获取问卷填写趋势", notes = "获取按时间统计的问卷填写趋势数据（所有提交的都是已完成的）")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/response-trend")
    public Result<Object> getResponseTrend(
            @ApiParam(value = "时间范围：7d-最近7天，30d-最近30天，90d-最近90天，1y-最近1年", defaultValue = "30d")
            @RequestParam(defaultValue = "30d") String timeRange) {
        java.time.LocalDateTime startTime = null;
        java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        if ("7d".equals(timeRange)) {
            startTime = java.time.LocalDateTime.now().minusDays(7);
        } else if ("30d".equals(timeRange)) {
            startTime = java.time.LocalDateTime.now().minusDays(30);
        } else if ("90d".equals(timeRange)) {
            startTime = java.time.LocalDateTime.now().minusDays(90);
        } else if ("1y".equals(timeRange)) {
            startTime = java.time.LocalDateTime.now().minusYears(1);
        } else {
            startTime = java.time.LocalDateTime.now().minusDays(30);
        }
        
        // 查询所有提交的填写记录（提交的都是已完成的）
        java.util.List<Response> responses = responseService.list(
            new LambdaQueryWrapper<Response>()
                .ge(Response::getCreateTime, startTime)
                .orderByAsc(Response::getCreateTime)
        );
        
        // 按日期分组统计（所有提交的都是已完成的）
        java.util.Map<String, Long> totalMap = new java.util.HashMap<>();
        
        for (Response response : responses) {
            if (response.getCreateTime() != null) {
                String date = response.getCreateTime().toLocalDate().format(dateFormatter);
                totalMap.put(date, totalMap.getOrDefault(date, 0L) + 1);
            }
        }
        
        // 生成完整日期序列
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        java.time.LocalDate currentDate = startTime.toLocalDate();
        java.time.LocalDate endDate = java.time.LocalDate.now();
        
        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.format(dateFormatter);
            long total = totalMap.getOrDefault(dateStr, 0L);
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("date", dateStr);
            item.put("total", total);
            // 所有提交的都是已完成的
            item.put("completed", total);
            result.add(item);
            currentDate = currentDate.plusDays(1);
        }
        
        return Result.success("获取成功", result);
    }

    @ApiOperation(value = "获取登录趋势", notes = "获取按时间统计的用户登录趋势数据")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/login-trend")
    public Result<Object> getLoginTrend(
            @ApiParam(value = "时间范围：7d-最近7天，30d-最近30天，90d-最近90天，1y-最近1年", defaultValue = "30d")
            @RequestParam(defaultValue = "30d") String timeRange) {
        java.time.LocalDateTime startTime = null;
        java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        if ("7d".equals(timeRange)) {
            startTime = java.time.LocalDateTime.now().minusDays(7);
        } else if ("30d".equals(timeRange)) {
            startTime = java.time.LocalDateTime.now().minusDays(30);
        } else if ("90d".equals(timeRange)) {
            startTime = java.time.LocalDateTime.now().minusDays(90);
        } else if ("1y".equals(timeRange)) {
            startTime = java.time.LocalDateTime.now().minusYears(1);
        } else {
            startTime = java.time.LocalDateTime.now().minusDays(30);
        }
        
        // 查询登录日志（operationType为"登录"）
        java.util.List<OperationLog> logs = operationLogService.list(
            new LambdaQueryWrapper<OperationLog>()
                .eq(OperationLog::getOperationType, "登录")
                .ge(OperationLog::getCreateTime, startTime)
                .orderByAsc(OperationLog::getCreateTime)
        );
        
        // 按日期分组统计
        java.util.Map<String, Long> trendMap = new java.util.HashMap<>();
        for (OperationLog log : logs) {
            if (log.getCreateTime() != null) {
                String date = log.getCreateTime().toLocalDate().format(dateFormatter);
                trendMap.put(date, trendMap.getOrDefault(date, 0L) + 1);
            }
        }
        
        // 生成完整日期序列
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        java.time.LocalDate currentDate = startTime.toLocalDate();
        java.time.LocalDate endDate = java.time.LocalDate.now();
        
        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.format(dateFormatter);
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("date", dateStr);
            item.put("count", trendMap.getOrDefault(dateStr, 0L));
            result.add(item);
            currentDate = currentDate.plusDays(1);
        }
        
        return Result.success("获取成功", result);
    }

    @ApiOperation(value = "获取平均填写时长趋势", notes = "获取按时间统计的平均填写时长趋势数据")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/response-duration-trend")
    public Result<Object> getResponseDurationTrend(
            @ApiParam(value = "时间范围：7d-最近7天，30d-最近30天，90d-最近90天，1y-最近1年", defaultValue = "30d")
            @RequestParam(defaultValue = "30d") String timeRange) {
        java.time.LocalDateTime startTime = null;
        java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        if ("7d".equals(timeRange)) {
            startTime = java.time.LocalDateTime.now().minusDays(7);
        } else if ("30d".equals(timeRange)) {
            startTime = java.time.LocalDateTime.now().minusDays(30);
        } else if ("90d".equals(timeRange)) {
            startTime = java.time.LocalDateTime.now().minusDays(90);
        } else if ("1y".equals(timeRange)) {
            startTime = java.time.LocalDateTime.now().minusYears(1);
        } else {
            startTime = java.time.LocalDateTime.now().minusDays(30);
        }
        
        java.util.List<Response> responses = responseService.list(
            new LambdaQueryWrapper<Response>()
                .ge(Response::getCreateTime, startTime)
                .eq(Response::getStatus, "COMPLETED")
                .isNotNull(Response::getDuration)
                .orderByAsc(Response::getCreateTime)
        );
        
        // 按日期分组统计平均时长
        java.util.Map<String, java.util.List<Integer>> durationMap = new java.util.HashMap<>();
        
        for (Response response : responses) {
            if (response.getCreateTime() != null && response.getDuration() != null) {
                String date = response.getCreateTime().toLocalDate().format(dateFormatter);
                durationMap.computeIfAbsent(date, k -> new java.util.ArrayList<>()).add(response.getDuration());
            }
        }
        
        // 生成完整日期序列并计算平均值
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        java.time.LocalDate currentDate = startTime.toLocalDate();
        java.time.LocalDate endDate = java.time.LocalDate.now();
        
        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.format(dateFormatter);
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("date", dateStr);
            
            java.util.List<Integer> durations = durationMap.get(dateStr);
            if (durations != null && !durations.isEmpty()) {
                double avgDuration = durations.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                item.put("avgDuration", Math.round(avgDuration * 100.0) / 100.0);
                item.put("count", durations.size());
            } else {
                item.put("avgDuration", 0.0);
                item.put("count", 0);
            }
            result.add(item);
            currentDate = currentDate.plusDays(1);
        }
        
        return Result.success("获取成功", result);
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
