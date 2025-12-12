package com.server.surveyanalystserver.service;

import javax.servlet.http.HttpServletResponse;

/**
 * 数据导出Service接口
 */
public interface ExportService {

    /**
     * 导出问卷数据（Excel格式）
     * @param surveyId 问卷ID
     * @param response HTTP响应
     */
    void exportSurveyData(Long surveyId, HttpServletResponse response);

    /**
     * 导出分析报告（PDF格式）
     * @param surveyId 问卷ID
     * @param response HTTP响应
     */
    void exportAnalysisReport(Long surveyId, HttpServletResponse response);
}

