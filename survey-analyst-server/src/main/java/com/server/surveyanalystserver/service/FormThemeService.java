package com.server.surveyanalystserver.service;

import com.server.surveyanalystserver.entity.FormTheme;

/**
 * 表单主题服务接口
 */
public interface FormThemeService {
    /**
     * 保存或更新表单主题
     * 保存表单的主题样式配置，包括颜色、字体等
     * @param theme 表单主题对象，包含主题样式配置
     * @return 保存成功后的表单主题对象
     */
    FormTheme saveFormTheme(FormTheme theme);
    
    /**
     * 根据问卷ID获取表单主题
     * 查询指定问卷的表单主题样式配置
     * @param surveyId 问卷ID
     * @return 表单主题对象，如果不存在则返回null
     */
    FormTheme getBySurveyId(Long surveyId);
    
    /**
     * 删除表单主题
     * 根据问卷ID删除表单主题（逻辑删除）
     * @param surveyId 问卷ID
     * @return true表示删除成功，false表示删除失败
     */
    boolean deleteBySurveyId(Long surveyId);
}

