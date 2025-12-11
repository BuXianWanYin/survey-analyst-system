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
     * @param formItemId 表单项ID（字符串）
     * @param surveyId 问卷ID（可选，用于容错处理）
     * @return 题目统计数据
     */
    Map<String, Object> getQuestionStatistics(String formItemId, Long surveyId);

    /**
     * 获取选项统计（选择人数、比例）
     * @param formItemId 表单项ID（字符串）
     * @return 选项统计数据
     */
    Map<String, Object> getOptionStatistics(String formItemId);

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

    /**
     * 获取所有统计数据（统一接口）
     * @param surveyId 问卷ID
     * @param includeTrend 是否包含填写趋势（默认false）
     * @param includeSource 是否包含填写来源（默认false）
     * @param includeDevice 是否包含设备统计（默认false）
     * @return 所有统计数据
     */
    Map<String, Object> getAllStatistics(Long surveyId, boolean includeTrend, boolean includeSource, boolean includeDevice);
}

