package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.FormTheme;
import com.server.surveyanalystserver.mapper.FormThemeMapper;
import com.server.surveyanalystserver.service.FormThemeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 表单主题服务实现类
 */
@Service
public class FormThemeServiceImpl extends ServiceImpl<FormThemeMapper, FormTheme> implements FormThemeService {
    
    /**
     * 保存表单主题
     * 保存或更新表单主题，如果已存在则更新，否则创建新主题
     * @param theme 表单主题对象
     * @return 保存成功后的表单主题对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FormTheme saveFormTheme(FormTheme theme) {
        // 查找是否已存在
        LambdaQueryWrapper<FormTheme> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormTheme::getSurveyId, theme.getSurveyId());
        FormTheme existing = this.getOne(wrapper);
        
        if (existing != null) {
            // 更新现有主题
            theme.setId(existing.getId());
            this.updateById(theme);
            return this.getById(theme.getId());
        } else {
            // 创建新主题
            this.save(theme);
            return theme;
        }
    }
    
    /**
     * 根据问卷ID获取表单主题
     * @param surveyId 问卷ID
     * @return 表单主题对象，如果不存在则返回null
     */
    @Override
    public FormTheme getBySurveyId(Long surveyId) {
        LambdaQueryWrapper<FormTheme> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormTheme::getSurveyId, surveyId);
        return this.getOne(wrapper);
    }
    
    /**
     * 根据问卷ID删除表单主题
     * @param surveyId 问卷ID
     * @return true表示删除成功，false表示删除失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBySurveyId(Long surveyId) {
        LambdaQueryWrapper<FormTheme> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormTheme::getSurveyId, surveyId);
        return this.remove(wrapper);
    }
}

