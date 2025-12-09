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
}

