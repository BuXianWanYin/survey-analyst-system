package com.server.surveyanalystserver.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.surveyanalystserver.entity.FormConfig;
import com.server.surveyanalystserver.entity.FormData;
import com.server.surveyanalystserver.entity.FormItem;
import com.server.surveyanalystserver.entity.Response;
import com.server.surveyanalystserver.mapper.FormItemMapper;
import com.server.surveyanalystserver.mapper.ResponseMapper;
import com.server.surveyanalystserver.mapper.SurveyMapper;
import com.server.surveyanalystserver.service.FormConfigService;
import com.server.surveyanalystserver.service.FormDataService;
import com.server.surveyanalystserver.service.FormItemService;
import com.server.surveyanalystserver.service.RedisCacheService;
import com.server.surveyanalystserver.service.StatisticsService;
import com.server.surveyanalystserver.config.FileConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 统计Service实现类
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private SurveyMapper surveyMapper;

    @Autowired
    private ResponseMapper responseMapper;

    @Autowired
    private FormConfigService formConfigService;

    @Autowired
    private FormItemService formItemService;

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private FormItemMapper formItemMapper;

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private FileConfig fileConfig;

    /**
     * Redis缓存过期时间（秒）- 1小时
     */
    private static final long CACHE_EXPIRE_TIME = 3600L;

    /**
     * Redis缓存键前缀
     */
    private static final String CACHE_KEY_PREFIX = "statistics:";

    /**
     * 获取问卷统计概览
     * 获取指定问卷的整体统计数据，包括填写数量、完成率等，优先从Redis缓存获取
     * @param surveyId 问卷ID
     * @return 统计概览数据Map，包含totalResponses、completedResponses、draftResponses、validRate等字段
     */
    @Override
    public Map<String, Object> getSurveyStatistics(Long surveyId) {
        // 先从Redis缓存获取
        String cacheKey = buildCacheKey(surveyId, null, "SURVEY_OVERVIEW");
        Map<String, Object> cachedData = redisCacheService.getMap(cacheKey);
        if (cachedData != null) {
            return cachedData;
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

        // 保存到Redis缓存
        redisCacheService.set(cacheKey, statistics, CACHE_EXPIRE_TIME);

        return statistics;
    }

    /**
     * 获取题目统计数据
     * 获取指定题目的统计数据，包括填写数量、填写率等，优先从Redis缓存获取
     * @param formItemId 表单项ID（字符串格式）
     * @param surveyIdParam 问卷ID（可选，用于容错处理）
     * @return 题目统计数据Map
     */
    @Override
    public Map<String, Object> getQuestionStatistics(String formItemId, Long surveyIdParam) {
        // 通过formItemId查找formItem
        LambdaQueryWrapper<FormItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(FormItem::getFormItemId, formItemId)
                   .last("LIMIT 1");
        FormItem formItem = formItemMapper.selectOne(itemWrapper);
        
        if (formItem == null) {
            throw new RuntimeException("表单项不存在");
        }
        
        String formKey = formItem.getFormKey();
        
        // 通过formKey获取surveyId
        Long surveyId = surveyIdParam;
        FormConfig formConfig = formConfigService.getByFormKey(formKey);
        if (formConfig != null) {
            // 如果找到了formConfig，使用formConfig中的surveyId
            surveyId = formConfig.getSurveyId();
        } else if (surveyId == null) {
            // 如果找不到formConfig且没有传入surveyId，抛出异常
            throw new RuntimeException("表单配置不存在，且未提供问卷ID");
        }
        // 如果找不到formConfig但传入了surveyId，使用传入的surveyId继续处理

        // 先从Redis缓存获取
        String cacheKey = buildCacheKey(surveyId, formItemId, "QUESTION_STAT");
        Map<String, Object> cachedData = redisCacheService.getMap(cacheKey);
        if (cachedData != null) {
            return cachedData;
        }

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("formItemId", formItemId);
        statistics.put("questionTitle", formItem.getLabel());
        statistics.put("questionType", formItem.getType());

        // 获取该表单的所有数据（分批查询）
        List<FormData> formDataList = new ArrayList<>();
        int pageSize = 5000; // 每批5000条
        int currentPage = 1;
        boolean hasMore = true;
        
        while (hasMore) {
            Page<FormData> page = 
                new Page<>(currentPage, pageSize);
            Page<FormData> formDataPage = 
                formDataService.getFormDataList(page, formKey);
            List<FormData> records = formDataPage.getRecords();
            
            if (records != null && !records.isEmpty()) {
                formDataList.addAll(records);
                hasMore = records.size() == pageSize; // 如果返回的数量等于pageSize，说明可能还有更多数据
                currentPage++;
            } else {
                hasMore = false;
            }
        }

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
            
            for (FormData data : formDataList) {
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
            List<String> textAnswers = new ArrayList<>(); // 用于词云分析
            
            for (FormData data : formDataList) {
                Map<String, Object> originalData = data.getOriginalData();
                if (originalData != null && originalData.containsKey(formItemId)) {
                    Object value = originalData.get(formItemId);
                    if (value != null && !String.valueOf(value).trim().isEmpty()) {
                        validAnswers++;
                        // 收集文本答案用于词云分析（仅 INPUT 和 TEXTAREA）
                        if ("INPUT".equals(type) || "TEXTAREA".equals(type)) {
                            textAnswers.add(String.valueOf(value).trim());
                        }
                    }
                }
            }
            
            statistics.put("validAnswers", validAnswers);
            statistics.put("totalAnswers", formDataList.size());
            
            // 生成词云数据（仅 INPUT 和 TEXTAREA）
            if (("INPUT".equals(type) || "TEXTAREA".equals(type)) && !textAnswers.isEmpty()) {
                Map<String, Integer> wordFrequency = generateWordFrequency(textAnswers);
                List<Map<String, Object>> wordCloudData = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : wordFrequency.entrySet()) {
                    Map<String, Object> word = new HashMap<>();
                    word.put("name", entry.getKey());
                    word.put("value", entry.getValue());
                    word.put("word", entry.getKey());
                    word.put("count", entry.getValue());
                    wordCloudData.add(word);
                }
                statistics.put("wordCloudData", wordCloudData);
            }
        } else if ("RATE".equals(type) || "SLIDER".equals(type)) {
            // 评分题/滑块：计算平均分
            List<Double> ratings = new ArrayList<>();
            for (FormData data : formDataList) {
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

        // 保存到Redis缓存
        redisCacheService.set(cacheKey, statistics, CACHE_EXPIRE_TIME);

        return statistics;
    }

    @Override
    public Map<String, Object> getOptionStatistics(String formItemId) {
        // 选项统计与题目统计相同，但需要兼容旧接口
        return getQuestionStatistics(formItemId, null);
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
        // 先从Redis缓存获取
        String cacheKey = buildCacheKey(surveyId, null, "RESPONSE_TREND_" + timeRange);
        Map<String, Object> cachedData = redisCacheService.getMap(cacheKey);
        if (cachedData != null) {
            return cachedData;
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

        // 保存到Redis缓存
        redisCacheService.set(cacheKey, trend, CACHE_EXPIRE_TIME);

        return trend;
    }

    @Override
    public Map<String, Object> getResponseSource(Long surveyId) {
        // 先从Redis缓存获取
        String cacheKey = buildCacheKey(surveyId, null, "RESPONSE_SOURCE");
        Map<String, Object> cachedData = redisCacheService.getMap(cacheKey);
        if (cachedData != null) {
            return cachedData;
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

        // 保存到Redis缓存
        redisCacheService.set(cacheKey, source, CACHE_EXPIRE_TIME);

        return source;
    }

    @Override
    public Map<String, Object> getDeviceStatistics(Long surveyId) {
        // 先从Redis缓存获取
        String cacheKey = buildCacheKey(surveyId, null, "DEVICE_STAT");
        Map<String, Object> cachedData = redisCacheService.getMap(cacheKey);
        if (cachedData != null) {
            return cachedData;
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

        // 保存到Redis缓存
        redisCacheService.set(cacheKey, device, CACHE_EXPIRE_TIME);

        return device;
    }

    @Override
    public boolean refreshStatistics(Long surveyId) {
        // 删除该问卷的所有Redis缓存
        String pattern = CACHE_KEY_PREFIX + "survey:" + surveyId + ":*";
        redisCacheService.deleteByPattern(pattern);
        return true;
    }

    @Override
    public Map<String, Object> getAllStatistics(Long surveyId, boolean includeTrend, boolean includeSource, boolean includeDevice) {
        // 先从Redis缓存获取
        String cacheKey = CACHE_KEY_PREFIX + "survey:" + surveyId + ":ALL_STATISTICS";
        Map<String, Object> cachedData = redisCacheService.getMap(cacheKey);
        if (cachedData != null) {
            return cachedData;
        }

        Map<String, Object> result = new HashMap<>();

        // 1. 获取问卷整体统计
        Map<String, Object> surveyStatistics = getSurveyStatistics(surveyId);
        result.put("surveyStatistics", surveyStatistics);

        // 2. 获取表单配置和表单项
        FormConfig formConfig = formConfigService.getBySurveyId(surveyId);
        if (formConfig == null) {
            throw new RuntimeException("表单配置不存在");
        }
        String formKey = formConfig.getFormKey();

        // 3. 获取所有表单项
        List<FormItem> formItems = formItemService.getByFormKey(formKey);
        
        // 4. 获取所有表单数据（分批查询，避免数据量过大时的内存问题）
        List<FormData> formDataList = new ArrayList<>();
        int pageSize = 5000; // 每批5000条
        int currentPage = 1;
        boolean hasMore = true;
        
        while (hasMore) {
            Page<FormData> page = 
                new Page<>(currentPage, pageSize);
            Page<FormData> formDataPage = 
                formDataService.getFormDataList(page, formKey);
            List<FormData> records = formDataPage.getRecords();
            
            if (records != null && !records.isEmpty()) {
                formDataList.addAll(records);
                hasMore = records.size() == pageSize; // 如果返回的数量等于pageSize，说明可能还有更多数据
                currentPage++;
            } else {
                hasMore = false;
            }
        }

        // 5. 统计每个题目的数据
        Map<String, Map<String, Object>> questionStatistics = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        for (FormItem formItem : formItems) {
            try {
                String formItemId = formItem.getFormItemId();
                Map<String, Object> stat = calculateQuestionStatistics(formItem, formDataList, objectMapper);
                questionStatistics.put(formItemId, stat);
            } catch (Exception e) {
                // 单个题目统计失败不影响整体
                System.err.println("统计题目失败: " + formItem.getFormItemId() + ", " + e.getMessage());
            }
        }

        result.put("questionStatistics", questionStatistics);

        // 6. 可选统计数据
        if (includeTrend) {
            result.put("trend", getResponseTrend(surveyId, "30d"));
        }
        if (includeSource) {
            result.put("source", getResponseSource(surveyId));
        }
        if (includeDevice) {
            result.put("device", getDeviceStatistics(surveyId));
        }

        // 保存到Redis缓存（缓存时间稍短，因为数据量大）
        redisCacheService.set(cacheKey, result, CACHE_EXPIRE_TIME);

        return result;
    }

    /**
     * 计算单个题目的统计数据（提取的公共方法）
     */
    private Map<String, Object> calculateQuestionStatistics(
            FormItem formItem,
            List<FormData> formDataList,
            ObjectMapper objectMapper) {
        
        Map<String, Object> statistics = new HashMap<>();
        String formItemId = formItem.getFormItemId();
        statistics.put("formItemId", formItemId);
        statistics.put("questionTitle", formItem.getLabel());
        statistics.put("questionType", formItem.getType());

        // 解析scheme获取选项信息
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
            
            for (FormData data : formDataList) {
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
            List<String> textAnswers = new ArrayList<>(); // 用于词云分析
            
            for (FormData data : formDataList) {
                Map<String, Object> originalData = data.getOriginalData();
                if (originalData != null && originalData.containsKey(formItemId)) {
                    Object value = originalData.get(formItemId);
                    if (value != null && !String.valueOf(value).trim().isEmpty()) {
                        validAnswers++;
                        // 收集文本答案用于词云分析（仅 INPUT 和 TEXTAREA）
                        if ("INPUT".equals(type) || "TEXTAREA".equals(type)) {
                            textAnswers.add(String.valueOf(value).trim());
                        }
                    }
                }
            }
            
            statistics.put("validAnswers", validAnswers);
            statistics.put("totalAnswers", formDataList.size());
            
            // 生成词云数据（仅 INPUT 和 TEXTAREA）
            if (("INPUT".equals(type) || "TEXTAREA".equals(type)) && !textAnswers.isEmpty()) {
                Map<String, Integer> wordFrequency = generateWordFrequency(textAnswers);
                List<Map<String, Object>> wordCloudData = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : wordFrequency.entrySet()) {
                    Map<String, Object> word = new HashMap<>();
                    word.put("name", entry.getKey());
                    word.put("value", entry.getValue());
                    word.put("word", entry.getKey());
                    word.put("count", entry.getValue());
                    wordCloudData.add(word);
                }
                statistics.put("wordCloudData", wordCloudData);
            }
        } else if ("RATE".equals(type) || "SLIDER".equals(type)) {
            // 评分题/滑块：计算平均分
            List<Double> ratings = new ArrayList<>();
            for (FormData data : formDataList) {
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
        } else if ("UPLOAD".equals(type)) {
            // 文件上传：统计上传数量和文件信息
            int validUploads = 0;
            int totalFiles = 0;
            long totalSize = 0;
            long maxSize = 0;
            long minSize = Long.MAX_VALUE;
            Map<String, Integer> fileTypeCount = new HashMap<>();
            List<Map<String, Object>> fileList = new ArrayList<>();
            
            for (FormData data : formDataList) {
                Map<String, Object> originalData = data.getOriginalData();
                if (originalData != null && originalData.containsKey(formItemId)) {
                    Object value = originalData.get(formItemId);
                    if (value != null) {
                        List<?> files = null;
                        if (value instanceof List) {
                            files = (List<?>) value;
                        } else {
                            // 单个文件，转为列表
                            files = java.util.Arrays.asList(value);
                        }
                        
                        if (files != null && !files.isEmpty()) {
                            validUploads++;
                            
                            for (Object fileObj : files) {
                                totalFiles++;
                                
                                // 获取文件大小（支持Map对象和String URL）
                                long fileSize = getFileSize(fileObj);
                                totalSize += fileSize;
                                if (fileSize > maxSize) maxSize = fileSize;
                                if (fileSize < minSize && fileSize > 0) minSize = fileSize;
                                
                                // 文件类型和文件信息
                                if (fileObj instanceof Map) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> file = (Map<String, Object>) fileObj;
                                    
                                    // 文件类型
                                    String fileName = String.valueOf(file.get("name") != null ? file.get("name") : "");
                                    // 如果name为空，尝试从url中提取文件名
                                    if (fileName.isEmpty() || "null".equals(fileName)) {
                                        String fileUrl = String.valueOf(file.get("url") != null ? file.get("url") : 
                                                                        (file.get("rawUrl") != null ? file.get("rawUrl") : ""));
                                        if (!fileUrl.isEmpty() && !"null".equals(fileUrl)) {
                                            fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                                        }
                                    }
                                    String fileType = getFileExtension(fileName);
                                    if (!fileType.isEmpty()) {
                                        fileTypeCount.put(fileType, fileTypeCount.getOrDefault(fileType, 0) + 1);
                                    }
                                    
                                    // 添加到文件列表
                                    fileList.add(file);
                                } else if (fileObj instanceof String) {
                                    // 如果是字符串URL，尝试提取文件类型
                                    String fileUrl = (String) fileObj;
                                    String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                                    String fileType = getFileExtension(fileName);
                                    if (!fileType.isEmpty()) {
                                        fileTypeCount.put(fileType, fileTypeCount.getOrDefault(fileType, 0) + 1);
                                    }
                                    
                                    // 将字符串URL转换为Map格式保存
                                    Map<String, Object> fileMap = new HashMap<>();
                                    fileMap.put("url", fileUrl);
                                    fileMap.put("name", fileName);
                                    fileMap.put("size", fileSize);
                                    fileList.add(fileMap);
                                }
                            }
                        }
                    }
                }
            }
            
            if (minSize == Long.MAX_VALUE) minSize = 0;
            
            // 构建文件类型统计
            List<Map<String, Object>> fileTypeStats = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : fileTypeCount.entrySet()) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("type", entry.getKey().toUpperCase());
                stat.put("count", entry.getValue());
                double percentage = totalFiles > 0 ? (double) entry.getValue() / totalFiles * 100 : 0;
                stat.put("percentage", Math.round(percentage * 100.0) / 100.0);
                fileTypeStats.add(stat);
            }
            
            statistics.put("validUploads", validUploads);
            statistics.put("totalAnswers", formDataList.size());
            double uploadRate = formDataList.size() > 0 ? (double) validUploads / formDataList.size() * 100 : 0;
            statistics.put("uploadRate", Math.round(uploadRate * 100.0) / 100.0);
            statistics.put("totalFiles", totalFiles);
            double averageFiles = validUploads > 0 ? (double) totalFiles / validUploads : 0;
            statistics.put("averageFiles", Math.round(averageFiles * 100.0) / 100.0);
            statistics.put("fileTypeStats", fileTypeStats);
            statistics.put("totalSize", totalSize);
            double averageSize = totalFiles > 0 ? (double) totalSize / totalFiles : 0;
            statistics.put("averageSize", Math.round(averageSize * 100.0) / 100.0);
            statistics.put("maxSize", maxSize);
            statistics.put("minSize", minSize);
            statistics.put("fileList", fileList);
        } else if ("IMAGE_UPLOAD".equals(type)) {
            // 图片上传：统计上传数量和图片信息
            int validUploads = 0;
            int totalImages = 0;
            long totalSize = 0;
            long maxSize = 0;
            long minSize = Long.MAX_VALUE;
            List<Map<String, Object>> imageList = new ArrayList<>();
            
            for (FormData data : formDataList) {
                Map<String, Object> originalData = data.getOriginalData();
                if (originalData != null && originalData.containsKey(formItemId)) {
                    Object value = originalData.get(formItemId);
                    if (value != null) {
                        List<?> images = null;
                        if (value instanceof List) {
                            images = (List<?>) value;
                        } else {
                            // 单个图片，转为列表
                            images = java.util.Arrays.asList(value);
                        }
                        
                        if (images != null && !images.isEmpty()) {
                            validUploads++;
                            
                            for (Object imageObj : images) {
                                totalImages++;
                                
                                // 获取图片大小（支持Map对象和String URL）
                                long imageSize = getFileSize(imageObj);
                                totalSize += imageSize;
                                if (imageSize > maxSize) maxSize = imageSize;
                                if (imageSize < minSize && imageSize > 0) minSize = imageSize;
                                
                                // 处理图片信息
                                if (imageObj instanceof Map) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> image = (Map<String, Object>) imageObj;
                                    // 添加到图片列表
                                    imageList.add(image);
                                } else if (imageObj instanceof String) {
                                    // 如果是字符串URL，转换为Map格式保存
                                    String imageUrl = (String) imageObj;
                                    String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                                    Map<String, Object> imageMap = new HashMap<>();
                                    imageMap.put("url", imageUrl);
                                    imageMap.put("name", fileName);
                                    imageMap.put("size", imageSize);
                                    imageList.add(imageMap);
                                }
                            }
                        }
                    }
                }
            }
            
            if (minSize == Long.MAX_VALUE) minSize = 0;
            
            statistics.put("validUploads", validUploads);
            statistics.put("totalAnswers", formDataList.size());
            double uploadRate = formDataList.size() > 0 ? (double) validUploads / formDataList.size() * 100 : 0;
            statistics.put("uploadRate", Math.round(uploadRate * 100.0) / 100.0);
            statistics.put("totalImages", totalImages);
            double averageImages = validUploads > 0 ? (double) totalImages / validUploads : 0;
            statistics.put("averageImages", Math.round(averageImages * 100.0) / 100.0);
            statistics.put("totalSize", totalSize);
            double averageSize = totalImages > 0 ? (double) totalSize / totalImages : 0;
            statistics.put("averageSize", Math.round(averageSize * 100.0) / 100.0);
            statistics.put("maxSize", maxSize);
            statistics.put("minSize", minSize);
            statistics.put("imageList", imageList);
        }

        return statistics;
    }
    
    /**
     * 获取文件扩展名
     * @param fileName 文件名
     * @return 扩展名（不含点号）
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 从文件URL获取文件大小
     * @param fileUrl 文件URL（可能是完整URL如 http://... 或相对路径如 /upload/2024/01/01/xxx.jpg）
     * @return 文件大小（字节），如果文件不存在则返回0
     */
    private long getFileSizeFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return 0;
        }
        try {
            String relativePath = fileUrl;
            
            // 如果是完整URL（http:// 或 https://），尝试提取路径部分
            if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
                try {
                    java.net.URL url = new java.net.URL(fileUrl);
                    String path = url.getPath();
                    // 提取 /upload/ 之后的部分
                    int uploadIndex = path.indexOf("/upload/");
                    if (uploadIndex >= 0) {
                        relativePath = path.substring(uploadIndex + 8); // 移除 "/upload/"
                    } else {
                        // 如果没有 /upload/ 前缀，使用整个路径（去掉开头的 /）
                        relativePath = path.startsWith("/") ? path.substring(1) : path;
                    }
                } catch (Exception e) {
                    // 如果URL解析失败，尝试直接从URL中提取路径
                    int uploadIndex = fileUrl.indexOf("/upload/");
                    if (uploadIndex >= 0) {
                        relativePath = fileUrl.substring(uploadIndex + 8);
                    }
                }
            } else if (fileUrl.startsWith("/upload/")) {
                // 如果是相对路径，移除 "/upload/" 前缀
                relativePath = fileUrl.substring(8);
            }
            
            // 构建完整路径
            String fullPath = fileConfig.getPath();
            if (!fullPath.endsWith("/") && !relativePath.startsWith("/")) {
                fullPath += "/";
            }
            fullPath += relativePath;
            
            File file = new File(fullPath);
            
            if (file.exists() && file.isFile()) {
                return file.length();
            }
        } catch (Exception e) {
            // 忽略异常，返回0
        }
        return 0;
    }

    /**
     * 从文件对象或URL获取文件大小
     * 优先从文件对象的size字段获取，如果不存在则从文件URL读取实际文件大小
     * @param fileObj 文件对象（Map）或文件URL（String）
     * @return 文件大小（字节）
     */
    private long getFileSize(Object fileObj) {
        if (fileObj == null) {
            return 0;
        }
        
        // 如果是Map对象，尝试获取size字段
        if (fileObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> file = (Map<String, Object>) fileObj;
            Object sizeObj = file.get("size");
            if (sizeObj instanceof Number) {
                long size = ((Number) sizeObj).longValue();
                if (size > 0) {
                    return size;
                }
            }
            // 如果没有size字段或size为0，尝试从url或rawUrl获取
            String fileUrl = null;
            if (file.containsKey("url")) {
                fileUrl = String.valueOf(file.get("url"));
            } else if (file.containsKey("rawUrl")) {
                fileUrl = String.valueOf(file.get("rawUrl"));
            }
            if (fileUrl != null && !fileUrl.isEmpty()) {
                return getFileSizeFromUrl(fileUrl);
            }
        }
        
        // 如果是字符串，直接作为URL处理
        if (fileObj instanceof String) {
            return getFileSizeFromUrl((String) fileObj);
        }
        
        return 0;
    }

    @Override
    public Map<String, Object> getFilteredStatistics(Long surveyId, java.util.List<Map<String, Object>> filters) {
        Map<String, Object> result = new HashMap<>();

        // 1. 获取表单配置
        FormConfig formConfig = formConfigService.getBySurveyId(surveyId);
        if (formConfig == null) {
            throw new RuntimeException("表单配置不存在");
        }
        String formKey = formConfig.getFormKey();

        // 2. 获取所有表单项
        List<FormItem> formItems = formItemService.getByFormKey(formKey);

        // 3. 获取所有表单数据（分批查询）
        List<FormData> formDataList = new ArrayList<>();
        int pageSize = 5000; // 每批5000条
        int currentPage = 1;
        boolean hasMore = true;
        
        while (hasMore) {
            Page<FormData> page = 
                new Page<>(currentPage, pageSize);
            Page<FormData> formDataPage = 
                formDataService.getFormDataList(page, formKey);
            List<FormData> records = formDataPage.getRecords();
            
            if (records != null && !records.isEmpty()) {
                formDataList.addAll(records);
                hasMore = records.size() == pageSize; // 如果返回的数量等于pageSize，说明可能还有更多数据
                currentPage++;
            } else {
                hasMore = false;
            }
        }

        // 4. 根据筛选条件过滤数据
        List<FormData> filteredDataList = formDataList;
        if (filters != null && !filters.isEmpty()) {
            filteredDataList = formDataList.stream()
                .filter(data -> {
                    Map<String, Object> originalData = data.getOriginalData();
                    if (originalData == null) return false;
                    
                    // 检查是否满足所有筛选条件（AND关系）
                    for (Map<String, Object> filter : filters) {
                        String formItemId = (String) filter.get("formItemId");
                        String optionValue = (String) filter.get("optionValue");
                        
                        if (formItemId == null || optionValue == null) continue;
                        
                        Object value = originalData.get(formItemId);
                        if (value == null) return false;
                        
                        // 处理单选和多选
                        boolean matches = false;
                        if (value instanceof List) {
                            // 多选题
                            List<?> values = (List<?>) value;
                            matches = values.contains(optionValue);
                        } else {
                            // 单选题
                            matches = optionValue.equals(String.valueOf(value));
                        }
                        
                        if (!matches) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
        }

        // 5. 计算问卷整体统计（基于筛选后的数据）
        long totalResponses = filteredDataList.size();
        long completedResponses = filteredDataList.size(); // FormData都是已完成的
        Map<String, Object> surveyStatistics = new HashMap<>();
        surveyStatistics.put("totalResponses", totalResponses);
        surveyStatistics.put("completedResponses", completedResponses);
        surveyStatistics.put("draftResponses", 0L);
        surveyStatistics.put("validRate", totalResponses > 0 ? 100.0 : 0.0);
        result.put("surveyStatistics", surveyStatistics);

        // 6. 统计每个题目的数据
        Map<String, Map<String, Object>> questionStatistics = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        for (FormItem formItem : formItems) {
            try {
                String formItemId = formItem.getFormItemId();
                Map<String, Object> stat = calculateQuestionStatistics(formItem, filteredDataList, objectMapper);
                questionStatistics.put(formItemId, stat);
            } catch (Exception e) {
                // 单个题目统计失败不影响整体
                System.err.println("统计题目失败: " + formItem.getFormItemId() + ", " + e.getMessage());
            }
        }

        result.put("questionStatistics", questionStatistics);

        return result;
    }

    /**
     * 生成词频统计（用于词云图）
     * @param textAnswers 文本答案列表
     * @return 词频Map（词 -> 频次）
     */
    private Map<String, Integer> generateWordFrequency(List<String> textAnswers) {
        Map<String, Integer> wordFrequency = new HashMap<>();
        
        // 停用词列表（常见无意义词汇）
        Set<String> stopWords = new HashSet<>(java.util.Arrays.asList(
            "的", "了", "是", "在", "有", "和", "就", "不", "人", "都", "一", "一个", "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好", "自己", "这"
        ));
        
        for (String answer : textAnswers) {
            // 简单的中文分词（按字符分割，后续可优化为专业分词）
            // 这里使用简单的方法：按单个字符或常见词组分割
            String[] words = answer.split("[\\s，。、；：！？,.;:!?\\n\\r]+");
            
            for (String word : words) {
                word = word.trim();
                // 过滤空字符串、单个字符、停用词
                if (word.length() > 1 && !stopWords.contains(word)) {
                    wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
                }
            }
        }
        
        // 按频次排序，返回前50个高频词
        return wordFrequency.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .limit(50)
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                java.util.LinkedHashMap::new
            ));
    }

    /**
     * 构建Redis缓存键
     * @param surveyId 问卷ID
     * @param formItemId 表单项ID（可为null）
     * @param statType 统计类型
     * @return 缓存键
     */
    private String buildCacheKey(Long surveyId, String formItemId, String statType) {
        if (formItemId != null) {
            return CACHE_KEY_PREFIX + "survey:" + surveyId + ":item:" + formItemId + ":" + statType;
        } else {
            return CACHE_KEY_PREFIX + "survey:" + surveyId + ":" + statType;
        }
    }
}

