package com.server.surveyanalystserver.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.server.surveyanalystserver.entity.Survey;
import com.server.surveyanalystserver.service.SurveyPublishService;
import com.server.surveyanalystserver.service.SurveyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 问卷发布与推广Service实现类
 */
@Service
public class SurveyPublishServiceImpl implements SurveyPublishService {

    @Autowired
    private SurveyService surveyService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public String generateSurveyLink(Long surveyId) {
        Survey survey = surveyService.getSurveyById(surveyId);
        if (survey == null) {
            throw new RuntimeException("问卷不存在");
        }
        return frontendUrl + "/user/survey/fill/" + surveyId;
    }

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

    @Override
    public String generateEmbedCode(Long surveyId) {
        String surveyLink = generateSurveyLink(surveyId);
        return String.format("<iframe src=\"%s\" width=\"100%%\" height=\"800px\" frameborder=\"0\"></iframe>", surveyLink);
    }

    @Override
    public Map<String, String> getShareLinks(Long surveyId) {
        String surveyLink = generateSurveyLink(surveyId);
        
        Map<String, String> shareLinks = new HashMap<>();
        // 只返回链接，供复制使用
        shareLinks.put("link", surveyLink);
        
        return shareLinks;
    }
}

