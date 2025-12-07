package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.FormLogic;
import com.server.surveyanalystserver.mapper.FormLogicMapper;
import com.server.surveyanalystserver.service.FormLogicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 表单逻辑服务实现类
 */
@Service
public class FormLogicServiceImpl extends ServiceImpl<FormLogicMapper, FormLogic> implements FormLogicService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FormLogic saveFormLogic(Long surveyId, List<Map<String, Object>> scheme) {
        // 查找是否已存在
        LambdaQueryWrapper<FormLogic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormLogic::getSurveyId, surveyId);
        FormLogic existing = this.getOne(wrapper);
        
        if (existing != null) {
            // 更新现有逻辑
            existing.setScheme(scheme);
            this.updateById(existing);
            return this.getById(existing.getId());
        } else {
            // 创建新逻辑
            FormLogic newLogic = new FormLogic();
            newLogic.setSurveyId(surveyId);
            newLogic.setScheme(scheme);
            this.save(newLogic);
            return newLogic;
        }
    }
    
    @Override
    public FormLogic getBySurveyId(Long surveyId) {
        LambdaQueryWrapper<FormLogic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormLogic::getSurveyId, surveyId);
        return this.getOne(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBySurveyId(Long surveyId) {
        LambdaQueryWrapper<FormLogic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormLogic::getSurveyId, surveyId);
        return this.remove(wrapper);
    }
}

