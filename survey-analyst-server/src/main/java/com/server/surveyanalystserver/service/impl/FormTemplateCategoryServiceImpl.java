package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.*;
import com.server.surveyanalystserver.mapper.FormTemplateCategoryMapper;
import com.server.surveyanalystserver.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 表单模板分类服务实现类
 */
@Service
public class FormTemplateCategoryServiceImpl extends ServiceImpl<FormTemplateCategoryMapper, FormTemplateCategory> implements FormTemplateCategoryService {

    @Autowired
    private FormTemplateService formTemplateService;

    @Autowired
    private FormItemService formItemService;

    @Autowired
    private FormDataService formDataService;

    @Override
    public List<FormTemplateCategory> listAll() {
        LambdaQueryWrapper<FormTemplateCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormTemplateCategory::getIsDeleted, 0);
        wrapper.orderByDesc(FormTemplateCategory::getSort);
        return this.list(wrapper);
    }

    @Override
    public List<FormTemplateCategory> listByUserId(Long userId) {
        LambdaQueryWrapper<FormTemplateCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormTemplateCategory::getIsDeleted, 0);
        // 查询系统分类（user_id为NULL）或当前用户的分类
        wrapper.and(w -> w.isNull(FormTemplateCategory::getUserId).or().eq(FormTemplateCategory::getUserId, userId));
        wrapper.orderByDesc(FormTemplateCategory::getSort);
        return this.list(wrapper);
    }

    @Override
    public List<FormTemplateCategory> listSystemCategories() {
        LambdaQueryWrapper<FormTemplateCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormTemplateCategory::getIsDeleted, 0);
        wrapper.isNull(FormTemplateCategory::getUserId);
        wrapper.orderByDesc(FormTemplateCategory::getSort);
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategoryWithTemplates(Long categoryId) {
        // 1. 查询该分类下的所有模板
        LambdaQueryWrapper<FormTemplate> templateWrapper = new LambdaQueryWrapper<>();
        templateWrapper.eq(FormTemplate::getCategoryId, categoryId);
        templateWrapper.eq(FormTemplate::getIsDeleted, 0);
        List<FormTemplate> templates = formTemplateService.list(templateWrapper);

        // 2. 删除每个模板及其相关数据
        for (FormTemplate template : templates) {
            String formKey = template.getFormKey();
            
            // 2.1 删除表单项
            formItemService.deleteByFormKey(formKey);
            
            // 2.2 删除表单数据
            LambdaQueryWrapper<FormData> dataWrapper = new LambdaQueryWrapper<>();
            dataWrapper.eq(FormData::getFormKey, formKey);
            formDataService.remove(dataWrapper);
            
            // 2.3 逻辑删除模板
            template.setIsDeleted(1);
            formTemplateService.updateById(template);
        }

        // 3. 逻辑删除分类
        FormTemplateCategory category = this.getById(categoryId);
        if (category != null) {
            category.setIsDeleted(1);
            this.updateById(category);
        }
    }
}

