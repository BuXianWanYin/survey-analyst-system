package com.server.surveyanalystserver.service;

import java.util.Map;

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

    /**
     * 获取社交媒体分享链接
     * @param surveyId 问卷ID
     * @return 分享链接Map（微信、微博、QQ等）
     */
    Map<String, String> getShareLinks(Long surveyId);
}

