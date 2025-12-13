package com.server.surveyanalystserver.controller;

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

    /**
     * 交叉分析接口
     * 对两个题目进行交叉分析，分析它们之间的关联关系
     * @param params 参数Map，包含surveyId（问卷ID）、formItemId1（第一个题目ID）、formItemId2（第二个题目ID）
     * @return 交叉分析结果，包含交叉统计表和关联度等数据
     */
    @ApiOperation(value = "交叉分析", notes = "对两个题目进行交叉分析")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/cross")
    public Result<Map<String, Object>> crossAnalysis(@RequestBody Map<String, Object> params) {
        Long surveyId = Long.valueOf(params.get("surveyId").toString());
        String formItemId1 = params.get("formItemId1").toString();
        String formItemId2 = params.get("formItemId2").toString();
        
        Map<String, Object> result = analysisService.crossAnalysis(surveyId, formItemId1, formItemId2);
        return Result.success("分析成功", result);
    }

    /**
     * 趋势分析接口
     * 对题目进行时间趋势分析，分析选项随时间的变化趋势
     * @param params 参数Map，包含surveyId（问卷ID）、formItemId（题目ID）、timeRange（时间范围，可选，默认为30d）
     * @return 趋势分析结果，包含各时间点的选项分布数据
     */
    @ApiOperation(value = "趋势分析", notes = "对题目进行趋势分析")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/trend")
    public Result<Map<String, Object>> trendAnalysis(@RequestBody Map<String, Object> params) {
        Long surveyId = Long.valueOf(params.get("surveyId").toString());
        String formItemId = params.get("formItemId").toString();
        String timeRange = params.get("timeRange") != null ? params.get("timeRange").toString() : "30d";
        
        Map<String, Object> result = analysisService.trendAnalysis(surveyId, formItemId, timeRange);
        return Result.success("分析成功", result);
    }

    /**
     * 样本画像分析接口
     * 获取问卷填写者的样本画像，包括年龄、性别、地区等分布特征
     * @param surveyId 问卷ID
     * @return 样本画像数据，包含各维度的分布统计
     */
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

    /**
     * 对比分析接口
     * 对比不同群体在各题目上的分布差异，用于群体差异分析
     * @param params 参数Map，包含surveyId（问卷ID）、compareVariable（对比变量，表单项ID）
     * @return 对比分析结果，包含各群体的分布差异数据
     */
    @ApiOperation(value = "对比分析", notes = "对比不同群体在各题目上的分布差异")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/compare")
    public Result<Map<String, Object>> compareAnalysis(@RequestBody Map<String, Object> params) {
        Long surveyId = Long.valueOf(params.get("surveyId").toString());
        String compareVariable = params.get("compareVariable").toString();
        
        Map<String, Object> result = analysisService.compareAnalysis(surveyId, compareVariable);
        return Result.success("分析成功", result);
    }
}

