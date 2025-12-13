package com.server.surveyanalystserver.service;

import com.server.surveyanalystserver.entity.FormLogic;

import java.util.List;
import java.util.Map;

/**
 * 表单逻辑服务接口
 */
public interface FormLogicService {
    /**
     * 保存或更新表单逻辑规则
     * 保存表单的逻辑规则配置，用于控制表单项的显示、隐藏等逻辑
     * @param surveyId 问卷ID
     * @param scheme 逻辑规则列表，包含多个逻辑规则配置
     * @return 保存成功后的表单逻辑对象
     */
    FormLogic saveFormLogic(Long surveyId, List<Map<String, Object>> scheme);
    
    /**
     * 根据问卷ID获取表单逻辑规则
     * 查询指定问卷的表单逻辑规则配置
     * @param surveyId 问卷ID
     * @return 表单逻辑对象，如果不存在则返回null
     */
    FormLogic getBySurveyId(Long surveyId);
    
    /**
     * 删除表单逻辑规则
     * 根据问卷ID删除表单逻辑规则（逻辑删除）
     * @param surveyId 问卷ID
     * @return true表示删除成功，false表示删除失败
     */
    boolean deleteBySurveyId(Long surveyId);
}

