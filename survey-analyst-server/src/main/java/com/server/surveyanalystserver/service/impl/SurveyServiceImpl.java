package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.Survey;
import com.server.surveyanalystserver.mapper.SurveyMapper;
import com.server.surveyanalystserver.service.SurveyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 问卷Service实现类
 */
@Service
public class SurveyServiceImpl extends ServiceImpl<SurveyMapper, Survey> implements SurveyService {

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
        return this.removeById(id);
    }
}

