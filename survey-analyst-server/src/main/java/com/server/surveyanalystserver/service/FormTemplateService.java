package com.server.surveyanalystserver.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
    
    /**
     * 分页查询模板列表（支持多条件查询）
     * @param page 分页参数
     * @param name 模板名称（模糊查询）
     * @param type 分类ID
     * @param isPublic 是否公共模板（0-我的模板，1-公共模板）
     * @param userId 当前用户ID（用于查询我的模板时过滤）
     * @return 模板分页列表
     */
    Page<FormTemplate> getTemplatePage(Page<FormTemplate> page, String name, Long type, Integer isPublic, Long userId);
    
    /**
     * 更新模板（包含权限检查）
     * @param template 模板信息
     * @param currentUserId 当前用户ID
     * @return 更新后的模板formKey
     */
    String updateTemplate(FormTemplate template, Long currentUserId);
    
    /**
     * 删除模板（包含权限检查）
     * @param formKey 模板formKey
     * @param currentUserId 当前用户ID
     * @return 删除的模板formKey
     */
    String deleteTemplate(String formKey, Long currentUserId);
}

