package com.server.surveyanalystserver.service;

/**
 * 问卷发布与推广Service接口
 */
public interface SurveyPublishService {

    /**
     * 生成问卷访问链接
     * 生成问卷的公开访问链接，用于分享问卷给他人填写
     * @param surveyId 问卷ID
     * @return 问卷访问链接URL
     */
    String generateSurveyLink(Long surveyId);

    /**
     * 生成问卷二维码
     * 生成问卷的二维码图片，返回Base64编码的图片数据
     * @param surveyId 问卷ID
     * @return 二维码Base64编码字符串
     */
    String generateQRCode(Long surveyId);
}

