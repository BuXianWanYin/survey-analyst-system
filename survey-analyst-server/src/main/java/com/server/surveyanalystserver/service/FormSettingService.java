package com.server.surveyanalystserver.service;

import com.server.surveyanalystserver.entity.FormSetting;

import java.util.Map;

/**
 * 表单设置服务接口
 */
public interface FormSettingService {
    /**
     * 保存或更新表单设置
     */
    FormSetting saveFormSetting(Long surveyId, Map<String, Object> settings);
    
    /**
     * 根据 surveyId 获取表单设置
     */
    FormSetting getBySurveyId(Long surveyId);
    
    /**
     * 删除表单设置（逻辑外键联动删除）
     */
    boolean deleteBySurveyId(Long surveyId);
}

