package com.server.surveyanalystserver.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.*;
import com.server.surveyanalystserver.mapper.*;
import com.server.surveyanalystserver.service.StatisticsService;
import com.server.surveyanalystserver.utils.DataCleaningUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计Service实现类
 */
@Service
public class StatisticsServiceImpl extends ServiceImpl<StatisticsCacheMapper, StatisticsCache> implements StatisticsService {

    @Autowired
    private SurveyMapper surveyMapper;

    @Autowired
    private ResponseMapper responseMapper;

    @Override
    public Map<String, Object> getSurveyStatistics(Long surveyId) {
        // 先从缓存获取
        StatisticsCache cache = getCache(surveyId, null, "SURVEY_OVERVIEW");
        if (cache != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(cache.getStatData(), Map.class);
            } catch (Exception e) {
                // 缓存数据解析失败，重新计算
            }
        }

        // 计算统计数据
        Map<String, Object> statistics = new HashMap<>();

        // 总填写数
        LambdaQueryWrapper<Response> responseWrapper = new LambdaQueryWrapper<>();
        responseWrapper.eq(Response::getSurveyId, surveyId);
        long totalResponses = responseMapper.selectCount(responseWrapper);
        statistics.put("totalResponses", totalResponses);

        // 已完成填写数
        responseWrapper.eq(Response::getStatus, "COMPLETED");
        long completedResponses = responseMapper.selectCount(responseWrapper);
        statistics.put("completedResponses", completedResponses);

        // 草稿数
        LambdaQueryWrapper<Response> draftWrapper = new LambdaQueryWrapper<>();
        draftWrapper.eq(Response::getSurveyId, surveyId).eq(Response::getStatus, "DRAFT");
        long draftResponses = responseMapper.selectCount(draftWrapper);
        statistics.put("draftResponses", draftResponses);

        // 有效率（已完成/总填写数）
        double validRate = totalResponses > 0 ? (double) completedResponses / totalResponses * 100 : 0;
        statistics.put("validRate", Math.round(validRate * 100.0) / 100.0);

        // 保存到缓存
        saveCache(surveyId, null, "SURVEY_OVERVIEW", statistics);

        return statistics;
    }

    @Override
    public Map<String, Object> getQuestionStatistics(Long questionId) {
        // 传统问卷系统已废弃，该方法不再支持
        throw new RuntimeException("该方法已废弃，系统已迁移到表单系统，请使用基于form_item的统计方法");
    }

    @Override
    public Map<String, Object> getOptionStatistics(Long questionId) {
        // 传统问卷系统已废弃，该方法不再支持
        throw new RuntimeException("该方法已废弃，系统已迁移到表单系统，请使用基于form_item的统计方法");
    }

    @Override
    public Map<String, Object> getResponseTrend(Long surveyId, String timeRange) {
        // 先从缓存获取
        StatisticsCache cache = getCache(surveyId, null, "RESPONSE_TREND_" + timeRange);
        if (cache != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(cache.getStatData(), Map.class);
            } catch (Exception e) {
                // 缓存数据解析失败，重新计算
            }
        }

        Map<String, Object> trend = new HashMap<>();
        List<Map<String, Object>> data = new ArrayList<>();

        // 获取填写记录
        LambdaQueryWrapper<Response> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Response::getSurveyId, surveyId)
               .eq(Response::getStatus, "COMPLETED")
               .orderByAsc(Response::getSubmitTime);

        LocalDateTime startTime = null;
        if ("7d".equals(timeRange)) {
            startTime = LocalDateTime.now().minusDays(7);
        } else if ("30d".equals(timeRange)) {
            startTime = LocalDateTime.now().minusDays(30);
        }

        if (startTime != null) {
            wrapper.ge(Response::getSubmitTime, startTime);
        }

        List<Response> responses = responseMapper.selectList(wrapper);

        // 按日期统计
        Map<String, Long> dateCount = responses.stream()
                .filter(r -> r.getSubmitTime() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getSubmitTime().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        Collectors.counting()
                ));

        for (Map.Entry<String, Long> entry : dateCount.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", entry.getKey());
            item.put("count", entry.getValue());
            data.add(item);
        }

        trend.put("data", data);
        trend.put("total", responses.size());

        // 保存到缓存
        saveCache(surveyId, null, "RESPONSE_TREND_" + timeRange, trend);

        return trend;
    }

    @Override
    public Map<String, Object> getResponseSource(Long surveyId) {
        // 先从缓存获取
        StatisticsCache cache = getCache(surveyId, null, "RESPONSE_SOURCE");
        if (cache != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(cache.getStatData(), Map.class);
            } catch (Exception e) {
                // 缓存数据解析失败，重新计算
            }
        }

        Map<String, Object> source = new HashMap<>();

        // 获取填写记录
        LambdaQueryWrapper<Response> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Response::getSurveyId, surveyId)
               .eq(Response::getStatus, "COMPLETED");

        List<Response> responses = responseMapper.selectList(wrapper);

        // 统计来源（这里简化处理，实际可以根据referer等字段）
        Map<String, Long> sourceCount = new HashMap<>();
        sourceCount.put("direct", (long) responses.size()); // 直接访问
        sourceCount.put("share", 0L); // 分享链接
        sourceCount.put("other", 0L); // 其他

        source.put("sourceCount", sourceCount);
        source.put("total", responses.size());

        // 保存到缓存
        saveCache(surveyId, null, "RESPONSE_SOURCE", source);

        return source;
    }

    @Override
    public Map<String, Object> getDeviceStatistics(Long surveyId) {
        // 先从缓存获取
        StatisticsCache cache = getCache(surveyId, null, "DEVICE_STAT");
        if (cache != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(cache.getStatData(), Map.class);
            } catch (Exception e) {
                // 缓存数据解析失败，重新计算
            }
        }

        Map<String, Object> device = new HashMap<>();

        // 获取填写记录
        LambdaQueryWrapper<Response> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Response::getSurveyId, surveyId)
               .eq(Response::getStatus, "COMPLETED");

        List<Response> responses = responseMapper.selectList(wrapper);

        // 统计设备类型
        Map<String, Long> deviceCount = responses.stream()
                .filter(r -> r.getDeviceType() != null)
                .collect(Collectors.groupingBy(
                        Response::getDeviceType,
                        Collectors.counting()
                ));

        // 如果没有设备类型，设置默认值
        if (deviceCount.isEmpty()) {
            deviceCount.put("PC", 0L);
            deviceCount.put("MOBILE", 0L);
        }

        device.put("deviceCount", deviceCount);
        device.put("total", responses.size());

        // 保存到缓存
        saveCache(surveyId, null, "DEVICE_STAT", device);

        return device;
    }

    @Override
    public boolean refreshStatistics(Long surveyId) {
        // 删除该问卷的所有缓存
        LambdaQueryWrapper<StatisticsCache> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StatisticsCache::getSurveyId, surveyId);
        this.remove(wrapper);
        return true;
    }

    /**
     * 获取缓存
     */
    private StatisticsCache getCache(Long surveyId, Long questionId, String statType) {
        LambdaQueryWrapper<StatisticsCache> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StatisticsCache::getSurveyId, surveyId)
               .eq(StatisticsCache::getStatType, statType);
        
        if (questionId != null) {
            wrapper.eq(StatisticsCache::getQuestionId, questionId);
        } else {
            wrapper.isNull(StatisticsCache::getQuestionId);
        }
        
        return this.getOne(wrapper);
    }

    /**
     * 保存缓存
     */
    private void saveCache(Long surveyId, Long questionId, String statType, Map<String, Object> data) {
        StatisticsCache cache = getCache(surveyId, questionId, statType);
        
        if (cache == null) {
            cache = new StatisticsCache();
            cache.setSurveyId(surveyId);
            cache.setQuestionId(questionId);
            cache.setStatType(statType);
        }
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            cache.setStatData(objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            throw new RuntimeException("保存统计数据失败", e);
        }
        this.saveOrUpdate(cache);
    }
}

