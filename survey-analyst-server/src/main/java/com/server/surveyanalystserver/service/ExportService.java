package com.server.surveyanalystserver.service;

import javax.servlet.http.HttpServletResponse;

/**
 * 数据导出Service接口
 */
public interface ExportService {

    /**
     * 导出问卷填写数据
     * 将问卷的所有填写数据导出为Excel文件，包含图片等附件
     * @param surveyId 问卷ID
     * @param response HTTP响应对象，用于输出Excel文件流
     */
    void exportSurveyData(Long surveyId, HttpServletResponse response);
}

