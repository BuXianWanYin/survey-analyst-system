package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.FormConfig;
import com.server.surveyanalystserver.entity.Survey;
import com.server.surveyanalystserver.mapper.SurveyMapper;
import com.server.surveyanalystserver.service.FormConfigService;
import com.server.surveyanalystserver.service.FormItemService;
import com.server.surveyanalystserver.service.SurveyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 问卷Service实现类
 */
@Service
public class SurveyServiceImpl extends ServiceImpl<SurveyMapper, Survey> implements SurveyService {
    
    @Autowired
    private FormConfigService formConfigService;
    
    @Autowired
    private FormItemService formItemService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Survey createSurvey(Survey survey) {
        survey.setStatus("DRAFT");
        this.save(survey);
        return survey;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Survey updateSurvey(Survey survey) {
        this.updateById(survey);
        return this.getById(survey.getId());
    }

    @Override
    public Survey getSurveyById(Long id) {
        return this.getById(id);
    }

    @Override
    public Page<Survey> getSurveyList(Page<Survey> page, Long userId) {
        LambdaQueryWrapper<Survey> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Survey::getUserId, userId);
        wrapper.orderByDesc(Survey::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishSurvey(Long id) {
        Survey survey = this.getById(id);
        if (survey == null) {
            throw new RuntimeException("问卷不存在");
        }
        survey.setStatus("PUBLISHED");
        return this.updateById(survey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSurvey(Long id) {
        // 逻辑外键联动删除：删除问卷时，删除相关的 form_config 和 form_item
        // 1. 查找并删除 form_config
        FormConfig formConfig = formConfigService.getBySurveyId(id);
        if (formConfig != null) {
            String formKey = formConfig.getFormKey();
            
            // 2. 删除 form_item（通过 formKey）
            formItemService.deleteByFormKey(formKey);
            
            // 3. 删除 form_config
            formConfigService.deleteById(formConfig.getId());
        }
        
        // 4. 删除问卷（逻辑删除）
        return this.removeById(id);
    }
}

