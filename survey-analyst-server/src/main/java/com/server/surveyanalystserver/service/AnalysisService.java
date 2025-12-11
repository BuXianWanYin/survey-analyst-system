package com.server.surveyanalystserver.service;

import java.util.Map;

/**
 * 分析Service接口
 */
public interface AnalysisService {

    /**
     * 交叉分析
     * @param surveyId 问卷ID
     * @param formItemId1 表单项1 ID（字符串）
     * @param formItemId2 表单项2 ID（字符串）
     * @return 交叉分析结果
     */
    Map<String, Object> crossAnalysis(Long surveyId, String formItemId1, String formItemId2);

    /**
     * 趋势分析
     * @param surveyId 问卷ID
     * @param formItemId 表单项ID（字符串）
     * @param timeRange 时间范围
     * @return 趋势分析结果
     */
    Map<String, Object> trendAnalysis(Long surveyId, String formItemId, String timeRange);

    /**
     * 样本画像分析
     * @param surveyId 问卷ID
     * @return 样本画像数据
     */
    Map<String, Object> profileAnalysis(Long surveyId);

    /**
     * 条件筛选分析
     * @param surveyId 问卷ID
     * @param filter 筛选条件
     * @return 筛选后的分析结果
     */
    Map<String, Object> filterAnalysis(Long surveyId, Map<String, Object> filter);

    /**
     * 对比分析
     * @param surveyId 问卷ID
     * @param compareVariable 对比变量（表单项ID）
     * @return 对比分析结果
     */
    Map<String, Object> compareAnalysis(Long surveyId, String compareVariable);
}

