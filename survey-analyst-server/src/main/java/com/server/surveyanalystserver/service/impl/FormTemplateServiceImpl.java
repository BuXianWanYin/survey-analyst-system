package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.*;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.mapper.*;
import com.server.surveyanalystserver.service.*;
import com.server.surveyanalystserver.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 表单模板服务实现类
 */
@Service
public class FormTemplateServiceImpl extends ServiceImpl<FormTemplateMapper, FormTemplate> implements FormTemplateService {

    @Autowired
    private FormConfigService formConfigService;

    @Autowired
    private FormItemService formItemService;

    @Autowired
    private FormThemeService formThemeService;

    @Autowired
    private FormLogicService formLogicService;

    @Autowired
    private SurveyService surveyService;
    
    @Autowired
    private UserService userService;

    @Override
    public FormTemplate getByFormKey(String formKey) {
        LambdaQueryWrapper<FormTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormTemplate::getFormKey, formKey);
        wrapper.eq(FormTemplate::getIsDeleted, 0);
        return this.getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FormTemplate createFormTemplate(FormTemplate template) {
        // 保存基础信息和问题
        String sourceFormKey = template.getFormKey();
        // 生成新的模板 formKey
        String newFormKey = generateFormKey();
        template.setFormKey(newFormKey);

        // 查询源表单配置
        FormConfig sourceConfig = formConfigService.getByFormKey(sourceFormKey);
        if (sourceConfig == null) {
            throw new RuntimeException("源表单不存在");
        }

        // 查询表单项列表
        List<FormItem> sourceFormItems = formItemService.getByFormKey(sourceFormKey);
        
        // 创建不包含时间字段的 FormItem 列表（避免 Jackson 序列化 LocalDateTime 的问题）
        List<FormItem> formItems = null;
        if (sourceFormItems != null && !sourceFormItems.isEmpty()) {
            formItems = sourceFormItems.stream().map(item -> {
                FormItem templateItem = new FormItem();
                BeanUtils.copyProperties(item, templateItem);
                // 清空不需要的字段（模板中不需要这些字段）
                templateItem.setId(null);
                templateItem.setFormKey(newFormKey); // 设置为模板的 formKey，这样可以从 form_item 表中查询
                templateItem.setCreateTime(null); // 设置为 null，避免 Jackson 序列化 LocalDateTime 的问题
                templateItem.setUpdateTime(null); // 设置为 null，避免 Jackson 序列化 LocalDateTime 的问题
                return templateItem;
            }).collect(Collectors.toList());
            
            // 将表单项保存到 form_item 表中，使用模板的 formKey
            formItemService.batchSave(newFormKey, formItems);
        }

        // 查询主题
        FormTheme sourceTheme = null;
        FormTheme formTheme = null;
        if (sourceConfig.getSurveyId() != null) {
            sourceTheme = formThemeService.getBySurveyId(sourceConfig.getSurveyId());
            if (sourceTheme != null) {
                // 创建不包含时间字段的 FormTheme（避免 Jackson 序列化 LocalDateTime 的问题）
                formTheme = new FormTheme();
                BeanUtils.copyProperties(sourceTheme, formTheme);
                formTheme.setId(null);
                formTheme.setSurveyId(null); // 模板中的 surveyId 会在创建问卷时重新设置
                formTheme.setCreateTime(null); // 设置为 null，避免 Jackson 序列化 LocalDateTime 的问题
                formTheme.setUpdateTime(null); // 设置为 null，避免 Jackson 序列化 LocalDateTime 的问题
            }
        }

        // 查询逻辑
        FormLogic sourceLogic = null;
        FormLogic formLogic = null;
        if (sourceConfig.getSurveyId() != null) {
            sourceLogic = formLogicService.getBySurveyId(sourceConfig.getSurveyId());
            if (sourceLogic != null) {
                // 创建不包含时间字段的 FormLogic（避免 Jackson 序列化 LocalDateTime 的问题）
                formLogic = new FormLogic();
                BeanUtils.copyProperties(sourceLogic, formLogic);
                formLogic.setId(null);
                formLogic.setSurveyId(null); // 模板中的 surveyId 会在创建问卷时重新设置
                formLogic.setCreateTime(null); // 设置为 null，避免 Jackson 序列化 LocalDateTime 的问题
                formLogic.setUpdateTime(null); // 设置为 null，避免 Jackson 序列化 LocalDateTime 的问题
            }
        }

        // 构建模板定义
        FormTemplate.Definition definition = new FormTemplate.Definition();
        definition.setFormType(sourceConfig.getType() != null ? Integer.parseInt(sourceConfig.getType()) : null);
        definition.setFormItems(formItems);
        definition.setFormTheme(formTheme);
        definition.setFormLogic(formLogic);

        template.setScheme(definition);
        template.setIsDeleted(0);
        template.setStatus(1L);
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());

        this.save(template);
        return template;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Survey createSurveyByTemplate(String templateFormKey, Long userId) {
        // 获取模板
        FormTemplate template = this.getByFormKey(templateFormKey);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }

        FormTemplate.Definition definition = template.getScheme();
        if (definition == null) {
            throw new RuntimeException("模板定义不存在");
        }

        // 生成新的 formKey
        String newFormKey = generateFormKey();

        // 创建问卷
        Survey survey = new Survey();
        survey.setTitle(template.getName());
        survey.setDescription(template.getDescription());
        survey.setUserId(userId);
        survey.setStatus("DRAFT"); // 草稿状态
        survey = surveyService.createSurvey(survey);

        // 创建表单配置
        FormConfig formConfig = new FormConfig();
        formConfig.setFormKey(newFormKey);
        formConfig.setSurveyId(survey.getId());
        formConfig.setName(template.getName());
        formConfig.setDescription(template.getDescription());
        formConfig.setUserId(userId);
        formConfig.setType(definition.getFormType() != null ? String.valueOf(definition.getFormType()) : null);
        formConfig.setStatus(1);
        formConfig.setIsDeleted(0);
        formConfig.setCreateTime(LocalDateTime.now());
        formConfig.setUpdateTime(LocalDateTime.now());
        formConfigService.saveFormConfig(formConfig);

        // 获取表单项列表：优先从 form_item 表中查询，确保 regList 等字段完整
        // 因为 regList 存储在数据库的 reg_list 列中，不在 scheme 的 JSON 中
        List<FormItem> sourceFormItems = formItemService.getByFormKey(templateFormKey);
        if (sourceFormItems == null || sourceFormItems.isEmpty()) {
            // 如果 form_item 表中没有数据，则从 scheme 中获取（兼容旧数据）
            sourceFormItems = definition.getFormItems();
        }

        // 复制表单项
        if (sourceFormItems != null && !sourceFormItems.isEmpty()) {
            List<FormItem> newFormItems = sourceFormItems.stream().map(item -> {
                FormItem newItem = new FormItem();
                // 使用 BeanUtils 复制所有字段
                BeanUtils.copyProperties(item, newItem);
                // 覆盖需要特殊处理的字段
                newItem.setId(null); // 清空ID，让数据库自动生成
                newItem.setFormKey(newFormKey); // 设置新的 formKey
                // 确保 span 有默认值
                if (newItem.getSpan() == null) {
                    newItem.setSpan(24);
                }
                newItem.setCreateTime(LocalDateTime.now());
                newItem.setUpdateTime(LocalDateTime.now());
                return newItem;
            }).collect(Collectors.toList());
            formItemService.batchSave(newFormKey, newFormItems);
        }

        // 复制主题
        if (definition.getFormTheme() != null) {
            FormTheme newTheme = new FormTheme();
            BeanUtils.copyProperties(definition.getFormTheme(), newTheme);
            newTheme.setId(null);
            newTheme.setSurveyId(survey.getId());
            newTheme.setCreateTime(LocalDateTime.now());
            newTheme.setUpdateTime(LocalDateTime.now());
            formThemeService.saveFormTheme(newTheme);
        }

        // 复制逻辑
        if (definition.getFormLogic() != null && definition.getFormLogic().getScheme() != null) {
            formLogicService.saveFormLogic(survey.getId(), definition.getFormLogic().getScheme());
        }

        return survey;
    }

    @Override
    public Page<FormTemplate> getTemplatePage(Page<FormTemplate> page, String name, Long type, Integer isPublic, Long userId) {
        LambdaQueryWrapper<FormTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormTemplate::getIsDeleted, 0);
        
        // 如果指定了模板类型，则按类型筛选
        if (isPublic != null) {
            wrapper.eq(FormTemplate::getIsPublic, isPublic);
        }
        
        // 如果是查询我的模板，需要过滤当前用户的模板
        if (isPublic != null && isPublic == 0 && userId != null) {
            wrapper.eq(FormTemplate::getUserId, userId);
        }
        
        if (StringUtils.hasText(name)) {
            wrapper.like(FormTemplate::getName, name);
        }
        
        if (type != null) {
            wrapper.eq(FormTemplate::getCategoryId, type);
        }
        
        wrapper.orderByDesc(FormTemplate::getCreateTime);
        return this.page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateTemplate(FormTemplate template, Long currentUserId) {
        FormTemplate existing = this.getByFormKey(template.getFormKey());
        if (existing == null) {
            throw new RuntimeException("模板不存在");
        }
        
        // 公共模板不允许更新
        if (existing.getIsPublic() != null && existing.getIsPublic() == 1) {
            throw new RuntimeException("公共模板不允许更新");
        }
        
        // 检查权限：只能更新自己创建的模板
        if (!existing.getUserId().equals(currentUserId)) {
            // 检查是否为管理员
            User currentUser = userService.getById(currentUserId);
            if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
                throw new RuntimeException("无权更新此模板");
            }
        }
        
        // 更新模板信息
        if (StringUtils.hasText(template.getName())) {
            existing.setName(template.getName());
        }
        if (StringUtils.hasText(template.getDescription())) {
            existing.setDescription(template.getDescription());
        }
        if (template.getCategoryId() != null) {
            existing.setCategoryId(template.getCategoryId());
        }
        if (StringUtils.hasText(template.getCoverImg())) {
            existing.setCoverImg(template.getCoverImg());
        }
        if (template.getStatus() != null) {
            existing.setStatus(template.getStatus());
        }
        
        this.updateById(existing);
        return template.getFormKey();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteTemplate(String formKey, Long currentUserId) {
        FormTemplate existing = this.getByFormKey(formKey);
        if (existing == null) {
            throw new RuntimeException("模板不存在");
        }
        
        // 公共模板不允许删除
        if (existing.getIsPublic() != null && existing.getIsPublic() == 1) {
            throw new RuntimeException("公共模板不允许删除");
        }
        
        // 检查权限：只能删除自己创建的模板
        if (!existing.getUserId().equals(currentUserId)) {
            // 检查是否为管理员
            User currentUser = userService.getById(currentUserId);
            if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
                throw new RuntimeException("无权删除此模板");
            }
        }
        
        // 逻辑删除
        existing.setIsDeleted(1);
        this.updateById(existing);
        
        return formKey;
    }

    /**
     * 生成唯一的 formKey
     */
    private String generateFormKey() {
        return "form_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}

