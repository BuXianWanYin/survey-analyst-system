package com.server.surveyanalystserver.controller.user;

import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.service.StatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 统计控制器
 */
@RestController
@RequestMapping("/api/statistics")
@Api(tags = "数据统计")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @ApiOperation(value = "获取问卷统计概览", notes = "获取问卷的整体统计数据")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/survey/{surveyId}")
    public Result<Map<String, Object>> getSurveyStatistics(@PathVariable Long surveyId) {
        Map<String, Object> statistics = statisticsService.getSurveyStatistics(surveyId);
        return Result.success("获取成功", statistics);
    }

    @ApiOperation(value = "获取题目统计", notes = "获取单个题目的统计数据")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/question/{questionId}")
    public Result<Map<String, Object>> getQuestionStatistics(@PathVariable Long questionId) {
        Map<String, Object> statistics = statisticsService.getQuestionStatistics(questionId);
        return Result.success("获取成功", statistics);
    }

    @ApiOperation(value = "获取选项统计", notes = "获取题目的选项统计数据")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/option/{questionId}")
    public Result<Map<String, Object>> getOptionStatistics(@PathVariable Long questionId) {
        Map<String, Object> statistics = statisticsService.getOptionStatistics(questionId);
        return Result.success("获取成功", statistics);
    }

    @ApiOperation(value = "获取填写趋势", notes = "获取问卷的填写趋势统计")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/trend/{surveyId}")
    public Result<Map<String, Object>> getResponseTrend(
            @PathVariable Long surveyId,
            @RequestParam(defaultValue = "30d") String timeRange) {
        Map<String, Object> trend = statisticsService.getResponseTrend(surveyId, timeRange);
        return Result.success("获取成功", trend);
    }

    @ApiOperation(value = "获取填写来源", notes = "获取问卷的填写来源统计")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/source/{surveyId}")
    public Result<Map<String, Object>> getResponseSource(@PathVariable Long surveyId) {
        Map<String, Object> source = statisticsService.getResponseSource(surveyId);
        return Result.success("获取成功", source);
    }

    @ApiOperation(value = "获取设备统计", notes = "获取问卷的设备类型统计")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/device/{surveyId}")
    public Result<Map<String, Object>> getDeviceStatistics(@PathVariable Long surveyId) {
        Map<String, Object> device = statisticsService.getDeviceStatistics(surveyId);
        return Result.success("获取成功", device);
    }

    @ApiOperation(value = "刷新统计数据", notes = "清除缓存，重新计算统计数据")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/refresh/{surveyId}")
    public Result<Void> refreshStatistics(@PathVariable Long surveyId) {
        statisticsService.refreshStatistics(surveyId);
        return Result.success("刷新成功");
    }
}

