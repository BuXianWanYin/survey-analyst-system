package com.server.surveyanalystserver.service;

import com.server.surveyanalystserver.entity.FormConfig;

/**
 * 表单配置服务接口
 */
public interface FormConfigService {
    /**
     * 保存或更新表单配置
     */
    FormConfig saveFormConfig(FormConfig config);
    
    /**
     * 根据 surveyId 获取表单配置
     */
    FormConfig getBySurveyId(Long surveyId);
    
    /**
     * 根据 formKey 获取表单配置
     */
    FormConfig getByFormKey(String formKey);
    
    /**
     * 删除表单配置（逻辑外键联动删除）
     */
    boolean deleteById(Long id);
}

