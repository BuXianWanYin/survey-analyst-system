package com.server.surveyanalystserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.server.surveyanalystserver.entity.FormTemplateCategory;

import java.util.List;

/**
 * 表单模板分类服务接口
 */
public interface FormTemplateCategoryService extends IService<FormTemplateCategory> {
    /**
     * 获取所有模板分类列表（按排序）
     * 包括系统分类和当前用户的自定义分类
     */
    List<FormTemplateCategory> listAll();
    
    /**
     * 获取当前用户的分类列表（包括系统分类和用户自定义分类）
     * @param userId 用户ID
     * @return 分类列表
     */
    List<FormTemplateCategory> listByUserId(Long userId);
    
    /**
     * 获取系统分类列表
     * @return 系统分类列表
     */
    List<FormTemplateCategory> listSystemCategories();
    
    /**
     * 删除分类及其下的所有模板和相关数据
     * @param categoryId 分类ID
     */
    void deleteCategoryWithTemplates(Long categoryId);
    
    /**
     * 创建用户自定义分类
     * @param category 分类信息
     * @param userId 用户ID
     * @return 创建的分类
     */
    FormTemplateCategory createCategory(FormTemplateCategory category, Long userId);
    
    /**
     * 更新分类（包含权限检查）
     * @param category 分类信息
     * @param currentUserId 当前用户ID
     * @return 更新后的分类ID
     */
    Long updateCategory(FormTemplateCategory category, Long currentUserId);
    
    /**
     * 删除分类（包含权限检查）
     * @param categoryId 分类ID
     * @param currentUserId 当前用户ID
     * @return 删除的分类ID
     */
    Long deleteCategory(Long categoryId, Long currentUserId);
}

