package com.server.surveyanalystserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.server.surveyanalystserver.entity.FormTemplateCategory;

import java.util.List;

/**
 * 表单模板分类服务接口
 */
public interface FormTemplateCategoryService extends IService<FormTemplateCategory> {
    /**
     * 获取所有模板分类列表
     * 获取所有模板分类，包括系统分类和用户自定义分类，按排序号降序排列
     * @return 模板分类列表
     */
    List<FormTemplateCategory> listAll();
    
    /**
     * 获取当前用户的分类列表
     * 获取系统分类和指定用户的自定义分类，按排序号降序排列
     * @param userId 用户ID
     * @return 分类列表
     */
    List<FormTemplateCategory> listByUserId(Long userId);
    
    /**
     * 获取系统分类列表
     * 获取所有系统预设的分类列表
     * @return 系统分类列表
     */
    List<FormTemplateCategory> listSystemCategories();
    
    /**
     * 删除分类及其下的所有模板和相关数据
     * 删除指定分类，同时删除该分类下的所有模板及其相关数据
     * @param categoryId 分类ID
     */
    void deleteCategoryWithTemplates(Long categoryId);
    
    /**
     * 创建用户自定义分类
     * 创建用户自己的模板分类，用于组织和管理模板
     * @param category 分类信息对象，必须包含name（分类名称）
     * @param userId 用户ID，用于设置分类的创建者
     * @return 创建成功后的分类对象
     */
    FormTemplateCategory createCategory(FormTemplateCategory category, Long userId);
    
    /**
     * 更新分类（包含权限检查）
     * 更新分类信息，普通用户只能更新自己的分类，管理员可以更新所有分类
     * @param category 包含更新信息的分类对象
     * @param currentUserId 当前用户ID，用于权限验证
     * @return 更新后的分类ID
     */
    Long updateCategory(FormTemplateCategory category, Long currentUserId);
    
    /**
     * 删除分类（包含权限检查）
     * 删除指定分类，普通用户只能删除自己的分类，管理员可以删除所有分类
     * @param categoryId 分类ID
     * @param currentUserId 当前用户ID，用于权限验证
     * @return 删除的分类ID
     */
    Long deleteCategory(Long categoryId, Long currentUserId);
}

