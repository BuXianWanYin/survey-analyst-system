package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.*;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.mapper.FormTemplateCategoryMapper;
import com.server.surveyanalystserver.service.*;
import com.server.surveyanalystserver.service.UserService;
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
    
    @Autowired
    private UserService userService;

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
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FormTemplateCategory createCategory(FormTemplateCategory category, Long userId) {
        category.setUserId(userId);
        
        // 设置默认排序号
        if (category.getSort() == null) {
            category.setSort(0);
        }
        
        this.save(category);
        return category;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long updateCategory(FormTemplateCategory category, Long currentUserId) {
        FormTemplateCategory existing = this.getById(category.getId());
        if (existing == null) {
            throw new RuntimeException("分类不存在");
        }
        
        User currentUser = userService.getById(currentUserId);
        if (currentUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 如果是系统分类，只有管理员可以更新
        if (existing.getUserId() == null && !"ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("系统分类只能由管理员修改");
        }
        
        // 如果是用户分类，只能更新自己的分类（管理员可以更新所有）
        if (existing.getUserId() != null && !existing.getUserId().equals(currentUserId) 
            && !"ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("无权修改此分类");
        }
        
        existing.setName(category.getName());
        if (category.getSort() != null) {
            existing.setSort(category.getSort());
        }
        this.updateById(existing);
        
        return category.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long deleteCategory(Long categoryId, Long currentUserId) {
        FormTemplateCategory existing = this.getById(categoryId);
        if (existing == null) {
            throw new RuntimeException("分类不存在");
        }
        
        User currentUser = userService.getById(currentUserId);
        if (currentUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 权限检查：普通用户只能删除自己的分类，管理员可以删除所有分类
        if (existing.getUserId() != null && !existing.getUserId().equals(currentUserId) 
            && !"ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("无权删除此分类");
        }
        
        // 如果是系统分类，只有管理员可以删除
        if (existing.getUserId() == null && !"ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("系统分类只能由管理员删除");
        }
        
        // 删除该分类下的所有模板及其相关数据
        this.deleteCategoryWithTemplates(existing.getId());
        
        return categoryId;
    }
}

