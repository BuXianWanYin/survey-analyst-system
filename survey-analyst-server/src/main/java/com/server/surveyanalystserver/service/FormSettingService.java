package com.server.surveyanalystserver.service;

import com.server.surveyanalystserver.entity.FormSetting;

import java.util.Map;

/**
 * 表单设置服务接口
 */
public interface FormSettingService {
    /**
     * 保存或更新表单设置
     * 保存表单的设置信息，包括提交设置、填写限制等
     * @param surveyId 问卷ID
     * @param settings 设置信息Map，包含各种表单设置项
     * @return 保存成功后的表单设置对象
     */
    FormSetting saveFormSetting(Long surveyId, Map<String, Object> settings);
    
    /**
     * 根据问卷ID获取表单设置
     * 查询指定问卷的表单设置信息
     * @param surveyId 问卷ID
     * @return 表单设置对象，如果不存在则返回null
     */
    FormSetting getBySurveyId(Long surveyId);
    
    /**
     * 删除表单设置
     * 根据问卷ID删除表单设置（逻辑删除）
     * @param surveyId 问卷ID
     * @return true表示删除成功，false表示删除失败
     */
    boolean deleteBySurveyId(Long surveyId);
}

