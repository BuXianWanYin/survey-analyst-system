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
     */
    List<FormTemplateCategory> listAll();
}

