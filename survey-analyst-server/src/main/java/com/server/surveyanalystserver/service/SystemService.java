package com.server.surveyanalystserver.service;

import java.util.List;
import java.util.Map;

/**
 * 系统统计Service接口
 */
public interface SystemService {

    /**
     * 获取系统概览统计数据
     * @return 系统概览数据
     */
    Map<String, Object> getSystemOverview();

    /**
     * 获取今日新增问卷数
     * @return 今日新增问卷数
     */
    Map<String, Object> getTodaySurveys();

    /**
     * 获取问卷创建趋势
     * @param timeRange 时间范围（7d、30d、90d、1y）
     * @return 问卷创建趋势数据
     */
    List<Map<String, Object>> getSurveyCreateTrend(String timeRange);

    /**
     * 获取问卷填写趋势
     * @param timeRange 时间范围（7d、30d、90d、1y）
     * @return 问卷填写趋势数据
     */
    List<Map<String, Object>> getResponseTrend(String timeRange);

    /**
     * 获取登录趋势
     * @param timeRange 时间范围（7d、30d、90d、1y）
     * @return 登录趋势数据
     */
    List<Map<String, Object>> getLoginTrend(String timeRange);

    /**
     * 获取平均填写时长趋势
     * @param timeRange 时间范围（7d、30d、90d、1y）
     * @return 平均填写时长趋势数据
     */
    List<Map<String, Object>> getResponseDurationTrend(String timeRange);
}

