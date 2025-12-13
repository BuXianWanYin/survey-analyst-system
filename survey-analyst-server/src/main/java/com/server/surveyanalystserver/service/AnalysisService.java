package com.server.surveyanalystserver.service;

import java.util.Map;

/**
 * 分析Service接口
 */
public interface AnalysisService {

    /**
     * 交叉分析
     * 对两个题目进行交叉分析，分析它们之间的关联关系
     * @param surveyId 问卷ID
     * @param formItemId1 第一个表单项ID（字符串格式）
     * @param formItemId2 第二个表单项ID（字符串格式）
     * @return 交叉分析结果Map，包含交叉统计表和关联度等数据
     */
    Map<String, Object> crossAnalysis(Long surveyId, String formItemId1, String formItemId2);

    /**
     * 趋势分析
     * 对题目进行时间趋势分析，分析选项随时间的变化趋势
     * @param surveyId 问卷ID
     * @param formItemId 表单项ID（字符串格式）
     * @param timeRange 时间范围（7d-最近7天，30d-最近30天，90d-最近90天，1y-最近1年）
     * @return 趋势分析结果Map，包含各时间点的选项分布数据
     */
    Map<String, Object> trendAnalysis(Long surveyId, String formItemId, String timeRange);

    /**
     * 样本画像分析
     * 获取问卷填写者的样本画像，包括年龄、性别、地区等分布特征
     * @param surveyId 问卷ID
     * @return 样本画像数据Map，包含各维度的分布统计
     */
    Map<String, Object> profileAnalysis(Long surveyId);

    /**
     * 条件筛选分析
     * 根据筛选条件过滤数据后进行分析，支持多条件组合筛选
     * @param surveyId 问卷ID
     * @param filter 筛选条件Map，包含多个筛选条件
     * @return 筛选后的分析结果Map
     */
    Map<String, Object> filterAnalysis(Long surveyId, Map<String, Object> filter);

    /**
     * 对比分析
     * 对比不同群体在各题目上的分布差异，用于群体差异分析
     * @param surveyId 问卷ID
     * @param compareVariable 对比变量（表单项ID），用于划分不同群体
     * @return 对比分析结果Map，包含各群体的分布差异数据
     */
    Map<String, Object> compareAnalysis(Long surveyId, String compareVariable);
}

