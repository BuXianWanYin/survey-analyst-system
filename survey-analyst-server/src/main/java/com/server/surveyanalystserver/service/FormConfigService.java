package com.server.surveyanalystserver.service;

import com.server.surveyanalystserver.entity.FormConfig;

/**
 * 表单配置服务接口
 */
public interface FormConfigService {
    /**
     * 保存或更新表单配置
     * 保存表单的基本配置信息，如果已存在则更新
     * @param config 表单配置对象
     * @return 保存成功后的表单配置对象
     */
    FormConfig saveFormConfig(FormConfig config);
    
    /**
     * 根据问卷ID获取表单配置
     * 查询指定问卷的表单配置信息
     * @param surveyId 问卷ID
     * @return 表单配置对象，如果不存在则返回null
     */
    FormConfig getBySurveyId(Long surveyId);
    
    /**
     * 根据表单Key获取表单配置
     * 查询指定表单Key的配置信息
     * @param formKey 表单Key
     * @return 表单配置对象，如果不存在则返回null
     */
    FormConfig getByFormKey(String formKey);
    
    /**
     * 删除表单配置
     * 根据ID删除表单配置（逻辑删除），同时会删除关联的表单项等数据
     * @param id 表单配置ID
     * @return true表示删除成功，false表示删除失败
     */
    boolean deleteById(Long id);
}

