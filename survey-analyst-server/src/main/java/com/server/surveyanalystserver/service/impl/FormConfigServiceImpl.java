package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.FormConfig;
import com.server.surveyanalystserver.entity.Survey;
import com.server.surveyanalystserver.mapper.FormConfigMapper;
import com.server.surveyanalystserver.mapper.SurveyMapper;
import com.server.surveyanalystserver.service.FormConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 表单配置服务实现类
 */
@Service
public class FormConfigServiceImpl extends ServiceImpl<FormConfigMapper, FormConfig> implements FormConfigService {
    
    @Autowired
    private SurveyMapper surveyMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FormConfig saveFormConfig(FormConfig config) {
        // 如果传递了 surveyId，先查找是否已存在
        if (config.getSurveyId() != null) {
            LambdaQueryWrapper<FormConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FormConfig::getSurveyId, config.getSurveyId());
            FormConfig existing = this.getOne(wrapper);
            
            if (existing != null) {
                // 更新现有配置
                config.setId(existing.getId());
                this.updateById(config);
                
                // 逻辑外键联动：同步更新 survey 表的 title
                Survey survey = surveyMapper.selectById(config.getSurveyId());
                if (survey != null && config.getName() != null) {
                    survey.setTitle(config.getName());
                    surveyMapper.updateById(survey);
                }
                
                return this.getById(config.getId());
            }
        }
        
        // 如果 formKey 已存在，查找并更新
        if (config.getFormKey() != null) {
            LambdaQueryWrapper<FormConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FormConfig::getFormKey, config.getFormKey());
            FormConfig existing = this.getOne(wrapper);
            
            if (existing != null) {
                // 更新现有配置
                config.setId(existing.getId());
                // 如果现有配置有 surveyId，保持它
                if (existing.getSurveyId() != null) {
                    config.setSurveyId(existing.getSurveyId());
                    
                    // 逻辑外键联动：同步更新 survey 表的 title
                    Survey survey = surveyMapper.selectById(existing.getSurveyId());
                    if (survey != null && config.getName() != null) {
                        survey.setTitle(config.getName());
                        surveyMapper.updateById(survey);
                    }
                }
                this.updateById(config);
                return this.getById(config.getId());
            }
        }
        
        // 如果有 ID，更新；否则新建
        if (config.getId() != null) {
            this.updateById(config);
            
            // 逻辑外键联动：如果有 surveyId，同步更新 survey 表的 title
            if (config.getSurveyId() != null && config.getName() != null) {
                Survey survey = surveyMapper.selectById(config.getSurveyId());
                if (survey != null) {
                    survey.setTitle(config.getName());
                    surveyMapper.updateById(survey);
                }
            }
            
            return this.getById(config.getId());
        } else {
            this.save(config);
            return config;
        }
    }
    
    @Override
    public FormConfig getBySurveyId(Long surveyId) {
        Survey survey = surveyMapper.selectById(surveyId);
        if (survey == null) {
            return null;
        }
        
        // 通过 surveyId 查询 form_config
        LambdaQueryWrapper<FormConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormConfig::getSurveyId, surveyId);
        return this.getOne(wrapper);
    }
    
    @Override
    public FormConfig getByFormKey(String formKey) {
        LambdaQueryWrapper<FormConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormConfig::getFormKey, formKey);
        return this.getOne(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        return this.removeById(id);
    }
}

