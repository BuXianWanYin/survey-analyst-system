package com.server.surveyanalystserver.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.*;
import com.server.surveyanalystserver.mapper.*;
import com.server.surveyanalystserver.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired
    private com.server.surveyanalystserver.service.FormConfigService formConfigService;

    @Autowired
    private com.server.surveyanalystserver.service.FormItemService formItemService;

    @Autowired
    private com.server.surveyanalystserver.service.FormDataService formDataService;

    @Autowired
    private com.server.surveyanalystserver.mapper.FormItemMapper formItemMapper;

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
    public Map<String, Object> getQuestionStatistics(String formItemId) {
        // 通过formItemId查找formItem
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.server.surveyanalystserver.entity.FormItem> itemWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        itemWrapper.eq(com.server.surveyanalystserver.entity.FormItem::getFormItemId, formItemId)
                   .last("LIMIT 1");
        com.server.surveyanalystserver.entity.FormItem formItem = formItemMapper.selectOne(itemWrapper);
        
        if (formItem == null) {
            throw new RuntimeException("表单项不存在");
        }
        
        String formKey = formItem.getFormKey();
        
        // 通过formKey获取surveyId
        com.server.surveyanalystserver.entity.FormConfig formConfig = formConfigService.getByFormKey(formKey);
        if (formConfig == null) {
            throw new RuntimeException("表单配置不存在");
        }
        Long surveyId = formConfig.getSurveyId();

        // 先从缓存获取
        StatisticsCache cache = getCache(surveyId, formItemId, "QUESTION_STAT");
        if (cache != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> cachedData = objectMapper.readValue(cache.getStatData(), Map.class);
                return cachedData;
            } catch (Exception e) {
                // 缓存数据解析失败，重新计算
            }
        }

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("formItemId", formItemId);
        statistics.put("questionTitle", formItem.getLabel());
        statistics.put("questionType", formItem.getType());

        // 获取该表单的所有数据
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.server.surveyanalystserver.entity.FormData> page = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10000);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.server.surveyanalystserver.entity.FormData> formDataPage = 
            formDataService.getFormDataList(page, formKey);
        List<com.server.surveyanalystserver.entity.FormData> formDataList = formDataPage.getRecords();

        // 解析scheme获取选项信息
        ObjectMapper objectMapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> scheme = null;
        try {
            if (formItem.getScheme() != null) {
                scheme = objectMapper.readValue(formItem.getScheme(), Map.class);
            }
        } catch (Exception e) {
            // 解析失败
        }

        // 判断题型并统计
        String type = formItem.getType();
        if (isChoiceType(type)) {
            // 选择题：统计选项分布
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> options = new ArrayList<>();
            if (scheme != null && scheme.containsKey("config")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> config = (Map<String, Object>) scheme.get("config");
                if (config != null && config.containsKey("options")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> opts = (List<Map<String, Object>>) config.get("options");
                    if (opts != null) {
                        options = opts;
                    }
                }
            }

            // 统计每个选项的选择次数
            Map<String, Integer> optionCount = new HashMap<>();
            int totalCount = 0;
            
            for (com.server.surveyanalystserver.entity.FormData data : formDataList) {
                Map<String, Object> originalData = data.getOriginalData();
                if (originalData != null && originalData.containsKey(formItemId)) {
                    Object value = originalData.get(formItemId);
                    if (value != null) {
                        totalCount++;
                        if (value instanceof List) {
                            // 多选题
                            List<?> values = (List<?>) value;
                            for (Object v : values) {
                                String optionValue = String.valueOf(v);
                                optionCount.put(optionValue, optionCount.getOrDefault(optionValue, 0) + 1);
                            }
                        } else {
                            // 单选题
                            String optionValue = String.valueOf(value);
                            optionCount.put(optionValue, optionCount.getOrDefault(optionValue, 0) + 1);
                        }
                    }
                }
            }

            // 构建选项统计
            List<Map<String, Object>> optionStats = new ArrayList<>();
            for (Map<String, Object> option : options) {
                String optionValue = String.valueOf(option.get("value"));
                String optionLabel = String.valueOf(option.get("label"));
                int count = optionCount.getOrDefault(optionValue, 0);
                double percentage = totalCount > 0 ? (double) count / totalCount * 100 : 0;
                
                Map<String, Object> optionStat = new HashMap<>();
                optionStat.put("optionValue", optionValue);
                optionStat.put("optionLabel", optionLabel);
                optionStat.put("count", count);
                optionStat.put("percentage", Math.round(percentage * 100.0) / 100.0);
                optionStats.add(optionStat);
            }
            
            statistics.put("optionStats", optionStats);
            statistics.put("totalCount", totalCount);
        } else if (isTextType(type)) {
            // 文本题：统计有效答案数
            int validAnswers = 0;
            for (com.server.surveyanalystserver.entity.FormData data : formDataList) {
                Map<String, Object> originalData = data.getOriginalData();
                if (originalData != null && originalData.containsKey(formItemId)) {
                    Object value = originalData.get(formItemId);
                    if (value != null && !String.valueOf(value).trim().isEmpty()) {
                        validAnswers++;
                    }
                }
            }
            
            statistics.put("validAnswers", validAnswers);
            statistics.put("totalAnswers", formDataList.size());
        } else if ("RATE".equals(type) || "SLIDER".equals(type)) {
            // 评分题/滑块：计算平均分
            List<Double> ratings = new ArrayList<>();
            for (com.server.surveyanalystserver.entity.FormData data : formDataList) {
                Map<String, Object> originalData = data.getOriginalData();
                if (originalData != null && originalData.containsKey(formItemId)) {
                    Object value = originalData.get(formItemId);
                    if (value != null) {
                        try {
                            double rating = Double.parseDouble(String.valueOf(value));
                            ratings.add(rating);
                        } catch (NumberFormatException e) {
                            // 忽略无效值
                        }
                    }
                }
            }

            if (!ratings.isEmpty()) {
                double avgRating = ratings.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                statistics.put("averageRating", Math.round(avgRating * 100.0) / 100.0);
                statistics.put("maxRating", ratings.stream().mapToDouble(Double::doubleValue).max().orElse(0));
                statistics.put("minRating", ratings.stream().mapToDouble(Double::doubleValue).min().orElse(0));
            }
            statistics.put("totalRatings", ratings.size());
        }

        // 保存到缓存
        saveCache(surveyId, formItemId, "QUESTION_STAT", statistics);

        return statistics;
    }

    @Override
    public Map<String, Object> getOptionStatistics(String formItemId) {
        // 选项统计与题目统计相同
        return getQuestionStatistics(formItemId);
    }

    /**
     * 判断是否为选择题类型
     */
    private boolean isChoiceType(String type) {
        return "RADIO".equals(type) || "CHECKBOX".equals(type) || "SELECT".equals(type) 
            || "IMAGE_SELECT".equals(type) || "CASCADER".equals(type);
    }

    /**
     * 判断是否为文本题类型
     */
    private boolean isTextType(String type) {
        return "INPUT".equals(type) || "TEXTAREA".equals(type) || "NUMBER".equals(type) 
            || "DATE".equals(type) || "TIME".equals(type) || "DATETIME".equals(type);
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
    private StatisticsCache getCache(Long surveyId, String formItemId, String statType) {
        LambdaQueryWrapper<StatisticsCache> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StatisticsCache::getSurveyId, surveyId)
               .eq(StatisticsCache::getStatType, statType);
        
        if (formItemId != null) {
            wrapper.eq(StatisticsCache::getFormItemId, formItemId);
        } else {
            wrapper.isNull(StatisticsCache::getFormItemId);
        }
        
        return this.getOne(wrapper);
    }

    /**
     * 保存缓存
     */
    private void saveCache(Long surveyId, String formItemId, String statType, Map<String, Object> data) {
        StatisticsCache cache = getCache(surveyId, formItemId, statType);
        
        if (cache == null) {
            cache = new StatisticsCache();
            cache.setSurveyId(surveyId);
            cache.setFormItemId(formItemId);
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

