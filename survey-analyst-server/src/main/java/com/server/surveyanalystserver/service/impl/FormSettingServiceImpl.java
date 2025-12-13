package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.FormSetting;
import com.server.surveyanalystserver.mapper.FormSettingMapper;
import com.server.surveyanalystserver.service.FormSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 表单设置服务实现类
 */
@Slf4j
@Service
public class FormSettingServiceImpl extends ServiceImpl<FormSettingMapper, FormSetting> implements FormSettingService {
    
    /**
     * 保存表单设置
     * 保存或更新表单设置，如果已存在则合并设置（保留原有设置，只更新传入的字段），否则创建新设置
     * @param surveyId 问卷ID
     * @param settings 设置Map
     * @return 保存成功后的表单设置对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FormSetting saveFormSetting(Long surveyId, Map<String, Object> settings) {
        log.info("保存表单设置 - surveyId: {}, settings: {}", surveyId, settings);
        
        // 查找是否已存在
        LambdaQueryWrapper<FormSetting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormSetting::getSurveyId, surveyId);
        FormSetting existing = this.getOne(wrapper);
        
        if (existing != null) {
            // 合并现有设置（保留原有设置，只更新传入的字段）
            Map<String, Object> existingSettings = existing.getSettings();
            if (existingSettings != null) {
                // 创建新的Map，避免直接修改原Map导致的问题
                Map<String, Object> mergedSettings = new HashMap<>(existingSettings);
                mergedSettings.putAll(settings); // 合并设置
                existing.setSettings(mergedSettings);
                log.info("合并设置 - 原有设置: {}, 新设置: {}, 合并后: {}", existingSettings, settings, mergedSettings);
            } else {
                existing.setSettings(settings);
                log.info("设置新配置（原有为空）: {}", settings);
            }
            this.updateById(existing);
            FormSetting updated = this.getById(existing.getId());
            log.info("保存后的设置: {}", updated.getSettings());
            return updated;
        } else {
            // 创建新设置
            FormSetting newSetting = new FormSetting();
            newSetting.setSurveyId(surveyId);
            newSetting.setSettings(settings);
            this.save(newSetting);
            log.info("创建新设置: {}", settings);
            return newSetting;
        }
    }
    
    /**
     * 根据问卷ID获取表单设置
     * @param surveyId 问卷ID
     * @return 表单设置对象，如果不存在则返回null
     */
    @Override
    public FormSetting getBySurveyId(Long surveyId) {
        LambdaQueryWrapper<FormSetting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormSetting::getSurveyId, surveyId);
        return this.getOne(wrapper);
    }
    
    /**
     * 根据问卷ID删除表单设置
     * @param surveyId 问卷ID
     * @return true表示删除成功，false表示删除失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBySurveyId(Long surveyId) {
        LambdaQueryWrapper<FormSetting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormSetting::getSurveyId, surveyId);
        return this.remove(wrapper);
    }
}

