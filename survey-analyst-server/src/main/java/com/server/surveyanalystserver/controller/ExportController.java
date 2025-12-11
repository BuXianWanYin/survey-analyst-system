package com.server.surveyanalystserver.controller;

import com.server.surveyanalystserver.service.ExportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * 数据导出控制器
 */
@RestController
@RequestMapping("/api/export")
@Api(tags = "数据导出")
public class ExportController {

    @Autowired
    private ExportService exportService;

    @ApiOperation(value = "导出问卷数据", notes = "导出问卷的填写数据（Excel格式）")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/survey/{surveyId}/data")
    public void exportSurveyData(@PathVariable Long surveyId, HttpServletResponse response) {
        exportService.exportSurveyData(surveyId, response);
    }

    @ApiOperation(value = "导出统计数据", notes = "导出问卷的统计数据（Excel格式）")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/survey/{surveyId}/statistics")
    public void exportStatistics(@PathVariable Long surveyId, HttpServletResponse response) {
        exportService.exportStatistics(surveyId, response);
    }

    @ApiOperation(value = "导出分析报告", notes = "导出问卷的分析报告（PDF格式）")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/survey/{surveyId}/report")
    public void exportAnalysisReport(@PathVariable Long surveyId, HttpServletResponse response) {
        exportService.exportAnalysisReport(surveyId, response);
    }
}

