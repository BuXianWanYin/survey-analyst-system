package com.server.surveyanalystserver.service;

/**
 * 问卷发布与推广Service接口
 */
public interface SurveyPublishService {

    /**
     * 生成问卷链接
     * @param surveyId 问卷ID
     * @return 问卷链接
     */
    String generateSurveyLink(Long surveyId);

    /**
     * 生成二维码（Base64格式）
     * @param surveyId 问卷ID
     * @return 二维码Base64字符串
     */
    String generateQRCode(Long surveyId);

    /**
     * 生成嵌入代码（iframe）
     * @param surveyId 问卷ID
     * @return 嵌入代码
     */
    String generateEmbedCode(Long surveyId);
}

