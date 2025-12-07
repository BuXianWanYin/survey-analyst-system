package com.server.surveyanalystserver.service;

import com.server.surveyanalystserver.entity.FormTheme;

/**
 * 表单主题服务接口
 */
public interface FormThemeService {
    /**
     * 保存或更新表单主题
     */
    FormTheme saveFormTheme(FormTheme theme);
    
    /**
     * 根据 surveyId 获取表单主题
     */
    FormTheme getBySurveyId(Long surveyId);
    
    /**
     * 删除表单主题（逻辑外键联动删除）
     */
    boolean deleteBySurveyId(Long surveyId);
}

