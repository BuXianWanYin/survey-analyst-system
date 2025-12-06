package com.server.surveyanalystserver.service;

import java.util.Map;

/**
 * 分析Service接口
 */
public interface AnalysisService {

    /**
     * 交叉分析
     * @param surveyId 问卷ID
     * @param questionId1 题目1 ID
     * @param questionId2 题目2 ID
     * @return 交叉分析结果
     */
    Map<String, Object> crossAnalysis(Long surveyId, Long questionId1, Long questionId2);

    /**
     * 趋势分析
     * @param surveyId 问卷ID
     * @param questionId 题目ID
     * @param timeRange 时间范围
     * @return 趋势分析结果
     */
    Map<String, Object> trendAnalysis(Long surveyId, Long questionId, String timeRange);

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
}

