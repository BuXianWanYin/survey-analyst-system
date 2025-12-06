package com.server.surveyanalystserver.controller.user;

import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.service.AnalysisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 分析控制器
 */
@RestController
@RequestMapping("/api/analysis")
@Api(tags = "数据分析")
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    @ApiOperation(value = "交叉分析", notes = "对两个题目进行交叉分析")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/cross")
    public Result<Map<String, Object>> crossAnalysis(@RequestBody Map<String, Object> params) {
        Long surveyId = Long.valueOf(params.get("surveyId").toString());
        Long questionId1 = Long.valueOf(params.get("questionId1").toString());
        Long questionId2 = Long.valueOf(params.get("questionId2").toString());
        
        Map<String, Object> result = analysisService.crossAnalysis(surveyId, questionId1, questionId2);
        return Result.success("分析成功", result);
    }

    @ApiOperation(value = "趋势分析", notes = "对题目进行趋势分析")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/trend")
    public Result<Map<String, Object>> trendAnalysis(@RequestBody Map<String, Object> params) {
        Long surveyId = Long.valueOf(params.get("surveyId").toString());
        Long questionId = Long.valueOf(params.get("questionId").toString());
        String timeRange = params.get("timeRange") != null ? params.get("timeRange").toString() : "30d";
        
        Map<String, Object> result = analysisService.trendAnalysis(surveyId, questionId, timeRange);
        return Result.success("分析成功", result);
    }

    @ApiOperation(value = "样本画像分析", notes = "获取问卷填写者的样本画像")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/profile/{surveyId}")
    public Result<Map<String, Object>> profileAnalysis(@PathVariable Long surveyId) {
        Map<String, Object> result = analysisService.profileAnalysis(surveyId);
        return Result.success("分析成功", result);
    }

    @ApiOperation(value = "条件筛选分析", notes = "根据条件筛选数据后进行分析")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/filter/{surveyId}")
    public Result<Map<String, Object>> filterAnalysis(
            @PathVariable Long surveyId,
            @RequestBody Map<String, Object> filter) {
        Map<String, Object> result = analysisService.filterAnalysis(surveyId, filter);
        return Result.success("分析成功", result);
    }
}

