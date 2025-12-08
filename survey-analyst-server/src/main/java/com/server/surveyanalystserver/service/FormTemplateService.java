package com.server.surveyanalystserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.server.surveyanalystserver.entity.FormTemplate;
import com.server.surveyanalystserver.entity.Survey;

/**
 * 表单模板服务接口
 */
public interface FormTemplateService extends IService<FormTemplate> {
    /**
     * 根据 formKey 获取模板
     */
    FormTemplate getByFormKey(String formKey);

    /**
     * 创建模板（从表单保存为模板）
     */
    FormTemplate createFormTemplate(FormTemplate template);

    /**
     * 根据模板创建问卷
     */
    Survey createSurveyByTemplate(String templateFormKey, Long userId);
}

