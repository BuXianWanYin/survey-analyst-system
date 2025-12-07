package com.server.surveyanalystserver.service;

import com.server.surveyanalystserver.entity.FormLogic;

import java.util.List;
import java.util.Map;

/**
 * 表单逻辑服务接口
 */
public interface FormLogicService {
    /**
     * 保存或更新表单逻辑
     */
    FormLogic saveFormLogic(Long surveyId, List<Map<String, Object>> scheme);
    
    /**
     * 根据 surveyId 获取表单逻辑
     */
    FormLogic getBySurveyId(Long surveyId);
    
    /**
     * 删除表单逻辑（逻辑外键联动删除）
     */
    boolean deleteBySurveyId(Long surveyId);
}

