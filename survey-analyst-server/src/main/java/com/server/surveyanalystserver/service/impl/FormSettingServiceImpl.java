package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.FormSetting;
import com.server.surveyanalystserver.mapper.FormSettingMapper;
import com.server.surveyanalystserver.service.FormSettingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 表单设置服务实现类
 */
@Service
public class FormSettingServiceImpl extends ServiceImpl<FormSettingMapper, FormSetting> implements FormSettingService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FormSetting saveFormSetting(Long surveyId, Map<String, Object> settings) {
        // 查找是否已存在
        LambdaQueryWrapper<FormSetting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormSetting::getSurveyId, surveyId);
        FormSetting existing = this.getOne(wrapper);
        
        if (existing != null) {
            // 合并现有设置（保留原有设置，只更新传入的字段）
            Map<String, Object> existingSettings = existing.getSettings();
            if (existingSettings != null) {
                existingSettings.putAll(settings); // 合并设置
                existing.setSettings(existingSettings);
            } else {
                existing.setSettings(settings);
            }
            this.updateById(existing);
            return this.getById(existing.getId());
        } else {
            // 创建新设置
            FormSetting newSetting = new FormSetting();
            newSetting.setSurveyId(surveyId);
            newSetting.setSettings(settings);
            this.save(newSetting);
            return newSetting;
        }
    }
    
    @Override
    public FormSetting getBySurveyId(Long surveyId) {
        LambdaQueryWrapper<FormSetting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormSetting::getSurveyId, surveyId);
        return this.getOne(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBySurveyId(Long surveyId) {
        LambdaQueryWrapper<FormSetting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormSetting::getSurveyId, surveyId);
        return this.remove(wrapper);
    }
}

