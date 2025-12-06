package com.server.surveyanalystserver.service;

import java.util.Map;

/**
 * 统计Service接口
 */
public interface StatisticsService {

    /**
     * 获取问卷统计概览
     * @param surveyId 问卷ID
     * @return 统计概览数据
     */
    Map<String, Object> getSurveyStatistics(Long surveyId);

    /**
     * 获取题目统计
     * @param questionId 题目ID
     * @return 题目统计数据
     */
    Map<String, Object> getQuestionStatistics(Long questionId);

    /**
     * 获取选项统计（选择人数、比例）
     * @param questionId 题目ID
     * @return 选项统计数据
     */
    Map<String, Object> getOptionStatistics(Long questionId);

    /**
     * 获取填写趋势统计
     * @param surveyId 问卷ID
     * @param timeRange 时间范围（如：7d, 30d, all）
     * @return 填写趋势数据
     */
    Map<String, Object> getResponseTrend(Long surveyId, String timeRange);

    /**
     * 获取填写来源统计
     * @param surveyId 问卷ID
     * @return 填写来源数据
     */
    Map<String, Object> getResponseSource(Long surveyId);

    /**
     * 获取设备类型统计
     * @param surveyId 问卷ID
     * @return 设备类型数据
     */
    Map<String, Object> getDeviceStatistics(Long surveyId);

    /**
     * 刷新统计数据（清除缓存，重新计算）
     * @param surveyId 问卷ID
     * @return 是否成功
     */
    boolean refreshStatistics(Long surveyId);
}

