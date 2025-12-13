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

    /**
     * 导出问卷填写数据
     * 将问卷的所有填写数据导出为Excel文件，包含图片等附件
     * @param surveyId 问卷ID
     * @param response HTTP响应对象，用于输出Excel文件
     */
    @ApiOperation(value = "导出问卷数据", notes = "导出问卷的填写数据（Excel格式）")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/survey/{surveyId}/data")
    public void exportSurveyData(@PathVariable Long surveyId, HttpServletResponse response) {
        exportService.exportSurveyData(surveyId, response);
    }
}

