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
     * 根据表单Key获取模板
     * 根据formKey查询模板的完整信息
     * @param formKey 表单Key
     * @return 模板对象，如果不存在则返回null
     */
    FormTemplate getByFormKey(String formKey);

    /**
     * 创建模板
     * 将表单保存为模板，用于后续复用
     * @param template 模板信息对象，必须包含scheme（表单定义）
     * @return 创建成功后的模板对象（包含生成的formKey）
     */
    FormTemplate createFormTemplate(FormTemplate template);

    /**
     * 根据模板创建问卷
     * 使用模板的表单定义创建新的问卷
     * @param templateFormKey 模板的formKey
     * @param userId 创建问卷的用户ID
     * @return 创建成功后的问卷对象
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
     * 更新模板信息，只有模板创建者或管理员可以更新
     * @param template 包含更新信息的模板对象
     * @param currentUserId 当前用户ID，用于权限验证
     * @return 更新后的模板formKey
     */
    String updateTemplate(FormTemplate template, Long currentUserId);
    
    /**
     * 删除模板（包含权限检查）
     * 删除指定模板，只有模板创建者或管理员可以删除
     * @param formKey 模板formKey
     * @param currentUserId 当前用户ID，用于权限验证
     * @return 删除的模板formKey
     */
    String deleteTemplate(String formKey, Long currentUserId);

    /**
     * 管理员分页查询公共模板列表
     * @param page 分页参数
     * @param name 模板名称（模糊查询）
     * @param categoryId 分类ID
     * @return 模板分页列表
     */
    Page<FormTemplate> getAdminPublicTemplatePage(Page<FormTemplate> page, String name, Long categoryId);

    /**
     * 管理员更新公共模板
     * @param template 模板信息
     * @return 更新后的模板formKey
     */
    String updateAdminPublicTemplate(FormTemplate template);

    /**
     * 管理员删除公共模板（逻辑删除）
     * @param formKey 模板formKey
     * @return 删除的模板formKey
     */
    String deleteAdminPublicTemplate(String formKey);
}

