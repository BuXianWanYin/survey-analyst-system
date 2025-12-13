package com.server.surveyanalystserver.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.server.surveyanalystserver.entity.Survey;
import com.server.surveyanalystserver.service.SurveyPublishService;
import com.server.surveyanalystserver.service.SurveyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 问卷发布与推广Service实现类
 */
@Service
public class SurveyPublishServiceImpl implements SurveyPublishService {

    @Autowired
    private SurveyService surveyService;

    @Value("${survey.access.url}")
    private String frontendUrl;

    /**
     * 生成问卷访问链接
     * 生成问卷的前端访问URL
     * @param surveyId 问卷ID
     * @return 问卷访问链接
     * @throws RuntimeException 如果问卷不存在则抛出异常
     */
    @Override
    public String generateSurveyLink(Long surveyId) {
        Survey survey = surveyService.getSurveyById(surveyId);
        if (survey == null) {
            throw new RuntimeException("问卷不存在");
        }
        return frontendUrl + "/user/survey/fill/" + surveyId;
    }

    /**
     * 生成问卷二维码
     * 生成问卷访问链接的二维码图片，返回Base64编码的图片数据
     * @param surveyId 问卷ID
     * @return Base64编码的二维码图片数据（data:image/png;base64,前缀）
     * @throws RuntimeException 如果生成失败则抛出异常
     */
    @Override
    public String generateQRCode(Long surveyId) {
        try {
            String surveyLink = generateSurveyLink(surveyId);
            
            // 使用Hutool生成二维码并转换为Base64
            byte[] qrCodeBytes = QrCodeUtil.generatePng(surveyLink, 300, 300);
            String base64 = Base64.encode(qrCodeBytes);
            
            return "data:image/png;base64," + base64;
        } catch (Exception e) {
            throw new RuntimeException("生成二维码失败", e);
        }
    }
}

