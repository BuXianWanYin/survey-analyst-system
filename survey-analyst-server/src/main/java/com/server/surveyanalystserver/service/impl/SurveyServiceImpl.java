package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.FormConfig;
import com.server.surveyanalystserver.entity.FormData;
import com.server.surveyanalystserver.entity.Survey;
import com.server.surveyanalystserver.mapper.SurveyMapper;
import com.server.surveyanalystserver.service.FormConfigService;
import com.server.surveyanalystserver.service.FormDataService;
import com.server.surveyanalystserver.service.FormItemService;
import com.server.surveyanalystserver.service.FormLogicService;
import com.server.surveyanalystserver.service.FormSettingService;
import com.server.surveyanalystserver.service.FormThemeService;
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
    
    @Autowired
    private FormSettingService formSettingService;
    
    @Autowired
    private FormLogicService formLogicService;
    
    @Autowired
    private FormThemeService formThemeService;
    
    @Autowired
    private FormDataService formDataService;

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
        
        // 检查是否有表单项
        FormConfig formConfig = formConfigService.getBySurveyId(id);
        if (formConfig == null) {
            throw new RuntimeException("表单配置不存在，无法发布");
        }
        
        long itemCount = formItemService.countByFormKey(formConfig.getFormKey());
        if (itemCount == 0) {
            throw new RuntimeException("无有效表单项，无法发布");
        }
        
        survey.setStatus("PUBLISHED");
        return this.updateById(survey);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean stopPublishSurvey(Long id) {
        Survey survey = this.getById(id);
        if (survey == null) {
            throw new RuntimeException("问卷不存在");
        }
        survey.setStatus("PAUSED");
        return this.updateById(survey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSurvey(Long id) {
        // 逻辑外键联动删除：删除问卷时，删除相关的所有数据
        // 1. 查找并删除 form_config
        FormConfig formConfig = formConfigService.getBySurveyId(id);
        if (formConfig != null) {
            String formKey = formConfig.getFormKey();
            
            // 2. 删除 form_item（通过 formKey）
            formItemService.deleteByFormKey(formKey);
            
            // 3. 删除 form_data（通过 formKey）
            LambdaQueryWrapper<FormData> dataWrapper = new LambdaQueryWrapper<>();
            dataWrapper.eq(FormData::getFormKey, formKey);
            formDataService.remove(dataWrapper);
            
            // 4. 删除 form_config
            formConfigService.deleteById(formConfig.getId());
        }
        
        // 5. 删除 form_setting（通过 surveyId）
        formSettingService.deleteBySurveyId(id);
        
        // 6. 删除 form_logic（通过 surveyId）
        formLogicService.deleteBySurveyId(id);
        
        // 7. 删除 form_theme（通过 surveyId）
        formThemeService.deleteBySurveyId(id);
        
        // 8. 删除问卷（逻辑删除）
        return this.removeById(id);
    }

    @Override
    public boolean verifyPassword(Long id, String password) {
        Survey survey = this.getById(id);
        if (survey == null) {
            return false;
        }
        
        // 如果问卷不是密码访问类型，返回false
        if (!"PASSWORD".equals(survey.getAccessType())) {
            return false;
        }
        
        // 验证密码
        if (survey.getPassword() == null || survey.getPassword().isEmpty()) {
            return false;
        }
        
        return survey.getPassword().equals(password);
    }
}

