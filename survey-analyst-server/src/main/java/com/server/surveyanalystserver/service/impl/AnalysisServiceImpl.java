package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.surveyanalystserver.entity.*;
import com.server.surveyanalystserver.mapper.*;
import com.server.surveyanalystserver.service.AnalysisService;
import com.server.surveyanalystserver.utils.DataCleaningUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 分析Service实现类
 */
@Service
public class AnalysisServiceImpl implements AnalysisService {

    @Autowired
    private ResponseMapper responseMapper;

    @Override
    public Map<String, Object> crossAnalysis(Long surveyId, Long questionId1, Long questionId2) {
        // 传统问卷系统已废弃，该方法不再支持
        throw new RuntimeException("该方法已废弃，系统已迁移到表单系统，请使用基于form_item的分析方法");
    }

    @Override
    public Map<String, Object> trendAnalysis(Long surveyId, Long questionId, String timeRange) {
        // 传统问卷系统已废弃，该方法不再支持
        throw new RuntimeException("该方法已废弃，系统已迁移到表单系统，请使用基于form_item的分析方法");
    }

    @Override
    public Map<String, Object> profileAnalysis(Long surveyId) {
        // 传统问卷系统已废弃，该方法不再支持
        throw new RuntimeException("该方法已废弃，系统已迁移到表单系统，请使用基于form_item的分析方法");
    }

    @Override
    public Map<String, Object> filterAnalysis(Long surveyId, Map<String, Object> filter) {
        Map<String, Object> result = new HashMap<>();

        // 获取填写记录
        LambdaQueryWrapper<Response> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Response::getSurveyId, surveyId)
               .eq(Response::getStatus, "COMPLETED");

        // 应用筛选条件
        if (filter.containsKey("deviceType")) {
            wrapper.eq(Response::getDeviceType, filter.get("deviceType"));
        }

        if (filter.containsKey("startTime")) {
            wrapper.ge(Response::getSubmitTime, filter.get("startTime"));
        }

        if (filter.containsKey("endTime")) {
            wrapper.le(Response::getSubmitTime, filter.get("endTime"));
        }

        List<Response> filteredResponses = responseMapper.selectList(wrapper);

        // 数据清洗
        List<Response> cleanedResponses = DataCleaningUtils.filterAbnormalData(
                DataCleaningUtils.removeDuplicates(filteredResponses)
        );

        result.put("filteredCount", cleanedResponses.size());
        result.put("originalCount", filteredResponses.size());
        result.put("filter", filter);

        return result;
    }

}

