package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.*;
import com.server.surveyanalystserver.mapper.*;
import com.server.surveyanalystserver.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        List<FormItem> formItems = formItemService.getByFormKey(sourceFormKey);

        // 查询主题
        FormTheme formTheme = null;
        if (sourceConfig.getSurveyId() != null) {
            formTheme = formThemeService.getBySurveyId(sourceConfig.getSurveyId());
        }

        // 查询逻辑
        FormLogic formLogic = null;
        if (sourceConfig.getSurveyId() != null) {
            formLogic = formLogicService.getBySurveyId(sourceConfig.getSurveyId());
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

        // 复制表单项
        if (definition.getFormItems() != null && !definition.getFormItems().isEmpty()) {
            List<FormItem> newFormItems = definition.getFormItems().stream().map(item -> {
                FormItem newItem = new FormItem();
                BeanUtils.copyProperties(item, newItem);
                newItem.setId(null);
                newItem.setFormKey(newFormKey);
                newItem.setQuestionId(null); // 清空 questionId，因为这是新创建的
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

    /**
     * 生成唯一的 formKey
     */
    private String generateFormKey() {
        return "form_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}

