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
        if (config.getId() != null) {
            this.updateById(config);
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
        
        // 从 survey 表获取 formKey（如果 survey 表有 formKey 字段）
        // 或者通过其他方式关联
        // 这里先简化处理，直接通过 surveyId 查询
        LambdaQueryWrapper<FormConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormConfig::getFormKey, "survey_" + surveyId);
        return this.getOne(wrapper);
    }
    
    @Override
    public FormConfig getByFormKey(String formKey) {
        LambdaQueryWrapper<FormConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormConfig::getFormKey, formKey);
        return this.getOne(wrapper);
    }
}

