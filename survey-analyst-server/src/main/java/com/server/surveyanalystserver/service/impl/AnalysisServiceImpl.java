package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.surveyanalystserver.entity.FormConfig;
import com.server.surveyanalystserver.entity.FormData;
import com.server.surveyanalystserver.entity.FormItem;
import com.server.surveyanalystserver.entity.Response;
import com.server.surveyanalystserver.mapper.FormItemMapper;
import com.server.surveyanalystserver.mapper.ResponseMapper;
import com.server.surveyanalystserver.service.AnalysisService;
import com.server.surveyanalystserver.service.FormConfigService;
import com.server.surveyanalystserver.service.FormDataService;
import com.server.surveyanalystserver.service.FormItemService;
import com.server.surveyanalystserver.utils.DataCleaningUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分析Service实现类
 */
@Service
public class AnalysisServiceImpl implements AnalysisService {

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

    @Override
    public Map<String, Object> crossAnalysis(Long surveyId, String formItemId1, String formItemId2) {
        Map<String, Object> result = new HashMap<>();

        // 获取formKey
        FormConfig formConfig = formConfigService.getBySurveyId(surveyId);
        if (formConfig == null) {
            throw new RuntimeException("表单配置不存在");
        }
        String formKey = formConfig.getFormKey();

        // 获取formItem信息
        LambdaQueryWrapper<FormItem> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(FormItem::getFormItemId, formItemId1)
                 .eq(FormItem::getFormKey, formKey)
                 .last("LIMIT 1");
        FormItem formItem1 = formItemMapper.selectOne(wrapper1);

        LambdaQueryWrapper<FormItem> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(FormItem::getFormItemId, formItemId2)
                 .eq(FormItem::getFormKey, formKey)
                 .last("LIMIT 1");
        FormItem formItem2 = formItemMapper.selectOne(wrapper2);

        if (formItem1 == null || formItem2 == null) {
            throw new RuntimeException("表单项不存在");
        }

        Map<String, Object> question1Info = new HashMap<>();
        question1Info.put("formItemId", formItemId1);
        question1Info.put("title", formItem1.getLabel());
        result.put("question1", question1Info);

        Map<String, Object> question2Info = new HashMap<>();
        question2Info.put("formItemId", formItemId2);
        question2Info.put("title", formItem2.getLabel());
        result.put("question2", question2Info);

        // 获取所有表单数据
        Page<FormData> page = new Page<>(1, 10000);
        Page<FormData> formDataPage = formDataService.getFormDataList(page, formKey);
        List<FormData> formDataList = formDataPage.getRecords();

        // 构建交叉表
        Map<String, Map<String, Integer>> crossTable = new HashMap<>();

        for (FormData data : formDataList) {
            Map<String, Object> originalData = data.getOriginalData();
            if (originalData == null) continue;

            Object value1 = originalData.get(formItemId1);
            Object value2 = originalData.get(formItemId2);

            if (value1 != null && value2 != null) {
                String key1 = getAnswerKey(value1, formItem1);
                String key2 = getAnswerKey(value2, formItem2);

                crossTable.computeIfAbsent(key1, k -> new HashMap<>())
                         .merge(key2, 1, Integer::sum);
            }
        }

        result.put("crossTable", crossTable);
        result.put("totalCount", formDataList.size());

        return result;
    }

    @Override
    public Map<String, Object> trendAnalysis(Long surveyId, String formItemId, String timeRange) {
        Map<String, Object> result = new HashMap<>();

        // 获取formKey
        FormConfig formConfig = formConfigService.getBySurveyId(surveyId);
        if (formConfig == null) {
            throw new RuntimeException("表单配置不存在");
        }
        String formKey = formConfig.getFormKey();

        // 获取formItem信息
        LambdaQueryWrapper<FormItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormItem::getFormItemId, formItemId)
                .eq(FormItem::getFormKey, formKey)
                .last("LIMIT 1");
        FormItem formItem = formItemMapper.selectOne(wrapper);

        if (formItem == null) {
            throw new RuntimeException("表单项不存在");
        }

        result.put("formItemId", formItemId);
        result.put("questionTitle", formItem.getLabel());

        // 获取时间范围内的填写记录
        LambdaQueryWrapper<Response> responseWrapper = new LambdaQueryWrapper<>();
        responseWrapper.eq(Response::getSurveyId, surveyId)
                       .eq(Response::getStatus, "COMPLETED")
                       .orderByAsc(Response::getSubmitTime);

        java.time.LocalDateTime startTime = null;
        if ("7d".equals(timeRange)) {
            startTime = java.time.LocalDateTime.now().minusDays(7);
        } else if ("30d".equals(timeRange)) {
            startTime = java.time.LocalDateTime.now().minusDays(30);
        }

        if (startTime != null) {
            responseWrapper.ge(Response::getSubmitTime, startTime);
        }

        List<Response> responses = responseMapper.selectList(responseWrapper);

        // 获取表单数据
        Page<FormData> page = 
            new Page<>(1, 10000);
        Page<FormData> formDataPage = 
            formDataService.getFormDataList(page, formKey);
        List<FormData> formDataList = formDataPage.getRecords();

        // 按日期和选项统计趋势
        Map<String, Map<String, Integer>> trendData = new HashMap<>();

        for (Response response : responses) {
            if (response.getSubmitTime() == null) continue;

            String date = response.getSubmitTime().toLocalDate().toString();
            
            // 查找对应的formData（通过时间匹配，简化处理）
            FormData formData = formDataList.stream()
                .filter(d -> d.getCreateTime() != null && 
                    d.getCreateTime().toLocalDate().equals(response.getSubmitTime().toLocalDate()))
                .findFirst()
                .orElse(null);

            if (formData != null) {
                Map<String, Object> originalData = formData.getOriginalData();
                if (originalData != null && originalData.containsKey(formItemId)) {
                    Object value = originalData.get(formItemId);
                    if (value != null) {
                        String answerKey = getAnswerKey(value, formItem);
                        trendData.computeIfAbsent(date, k -> new HashMap<>())
                                .merge(answerKey, 1, Integer::sum);
                    }
                }
            }
        }

        result.put("trendData", trendData);
        result.put("totalCount", responses.size());

        return result;
    }

    @Override
    public Map<String, Object> profileAnalysis(Long surveyId) {
        Map<String, Object> result = new HashMap<>();

        // 获取formKey
        FormConfig formConfig = formConfigService.getBySurveyId(surveyId);
        if (formConfig == null) {
            throw new RuntimeException("表单配置不存在");
        }
        String formKey = formConfig.getFormKey();

        // 获取填写记录
        LambdaQueryWrapper<Response> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(com.server.surveyanalystserver.entity.Response::getSurveyId, surveyId)
               .eq(com.server.surveyanalystserver.entity.Response::getStatus, "COMPLETED");
        List<Response> responses = responseMapper.selectList(wrapper);

        // 获取所有表单项
        List<FormItem> formItems = formItemService.getByFormKey(formKey);
        
        // 识别性别、年龄、地域题目（通过题目标题关键词）
        FormItem genderItem = null;
        FormItem ageItem = null;
        FormItem regionItem = null;
        
        for (FormItem item : formItems) {
            String title = item.getLabel().toLowerCase();
            if (title.contains("性别") || title.contains("gender") || title.contains("男") || title.contains("女")) {
                genderItem = item;
            } else if (title.contains("年龄") || title.contains("age") || title.contains("岁")) {
                ageItem = item;
            } else if (title.contains("地域") || title.contains("地区") || title.contains("region") || 
                      title.contains("城市") || title.contains("省份")) {
                regionItem = item;
            }
        }
        
        // 获取表单数据
        Page<FormData> page = 
            new Page<>(1, 10000);
        Page<FormData> formDataPage = 
            formDataService.getFormDataList(page, formKey);
        List<FormData> formDataList = formDataPage.getRecords();
        
        // 统计性别分布
        if (genderItem != null) {
            Map<String, Long> genderDistribution = analyzeQuestionDistribution(genderItem, formDataList);
            result.put("genderDistribution", genderDistribution);
        }
        
        // 统计年龄分布
        if (ageItem != null) {
            Map<String, Long> ageDistribution = analyzeAgeDistribution(ageItem, formDataList);
            result.put("ageDistribution", ageDistribution);
        }
        
        // 统计地域分布
        if (regionItem != null) {
            Map<String, Long> regionDistribution = analyzeQuestionDistribution(regionItem, formDataList);
            result.put("regionDistribution", regionDistribution);
        }

        // 设备类型分布
        Map<String, Long> deviceDistribution = responses.stream()
                .filter(r -> r.getDeviceType() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        Response::getDeviceType,
                        java.util.stream.Collectors.counting()
                ));
        result.put("deviceDistribution", deviceDistribution);

        // 填写时间分布（按小时）
        Map<Integer, Long> hourDistribution = responses.stream()
                .filter(r -> r.getSubmitTime() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        r -> r.getSubmitTime().getHour(),
                        java.util.stream.Collectors.counting()
                ));
        result.put("hourDistribution", hourDistribution);

        // 填写时长分布
        Map<String, Long> durationDistribution = new HashMap<>();
        long shortDuration = responses.stream()
                .filter(r -> r.getDuration() != null && r.getDuration() < 60)
                .count();
        long mediumDuration = responses.stream()
                .filter(r -> r.getDuration() != null && r.getDuration() >= 60 && r.getDuration() < 300)
                .count();
        long longDuration = responses.stream()
                .filter(r -> r.getDuration() != null && r.getDuration() >= 300)
                .count();

        durationDistribution.put("short", shortDuration); // < 1分钟
        durationDistribution.put("medium", mediumDuration); // 1-5分钟
        durationDistribution.put("long", longDuration); // > 5分钟

        result.put("durationDistribution", durationDistribution);
        result.put("totalCount", responses.size());

        return result;
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

    /**
     * 获取答案的键值（用于交叉分析）
     */
    private String getAnswerKey(Object value, com.server.surveyanalystserver.entity.FormItem formItem) {
        if (value == null) {
            return "未填写";
        }

        // 解析scheme获取选项信息
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> scheme = null;
        try {
            if (formItem.getScheme() != null) {
                scheme = objectMapper.readValue(formItem.getScheme(), Map.class);
            }
        } catch (Exception e) {
            // 解析失败
        }

        // 如果是选择题，尝试获取选项标签
        if (isChoiceType(formItem.getType()) && scheme != null) {
            Map<String, Object> config = scheme.containsKey("config") ? 
                (Map<String, Object>) scheme.get("config") : null;
            if (config != null && config.containsKey("options")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> options = (List<Map<String, Object>>) config.get("options");
                if (options != null) {
                    if (value instanceof List) {
                        // 多选题
                        List<?> values = (List<?>) value;
                        return values.stream()
                            .map(v -> findOptionLabel(String.valueOf(v), options))
                            .filter(s -> !s.isEmpty())
                            .collect(java.util.stream.Collectors.joining("、"));
                    } else {
                        // 单选题
                        return findOptionLabel(String.valueOf(value), options);
                    }
                }
            }
        }

        // 其他情况直接返回字符串值
        if (value instanceof List) {
            List<?> values = (List<?>) value;
            return values.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining("、"));
        }
        return String.valueOf(value);
    }

    /**
     * 查找选项标签
     */
    private String findOptionLabel(String value, List<Map<String, Object>> options) {
        for (Map<String, Object> option : options) {
            if (String.valueOf(option.get("value")).equals(value)) {
                return String.valueOf(option.get("label"));
            }
            // 处理级联选择器
            if (option.containsKey("children")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> children = (List<Map<String, Object>>) option.get("children");
                if (children != null) {
                    String childLabel = findOptionLabel(value, children);
                    if (!childLabel.isEmpty()) {
                        return childLabel;
                    }
                }
            }
        }
        return value;
    }

    /**
     * 判断是否为选择题类型
     */
    private boolean isChoiceType(String type) {
        return "RADIO".equals(type) || "CHECKBOX".equals(type) || "SELECT".equals(type) 
            || "IMAGE_SELECT".equals(type) || "CASCADER".equals(type);
    }

    /**
     * 分析题目分布（用于性别、地域等）
     */
    private Map<String, Long> analyzeQuestionDistribution(
            FormItem formItem, 
            List<FormData> formDataList) {
        Map<String, Long> distribution = new HashMap<>();
        
        for (FormData data : formDataList) {
            Map<String, Object> originalData = data.getOriginalData();
            if (originalData != null && originalData.containsKey(formItem.getFormItemId())) {
                Object value = originalData.get(formItem.getFormItemId());
                if (value != null) {
                    String key = getAnswerKey(value, formItem);
                    distribution.put(key, distribution.getOrDefault(key, 0L) + 1);
                }
            }
        }
        
        return distribution;
    }

    /**
     * 分析年龄分布（将年龄分组）
     */
    private Map<String, Long> analyzeAgeDistribution(
            FormItem formItem, 
            List<FormData> formDataList) {
        Map<String, Long> distribution = new HashMap<>();
        distribution.put("18岁以下", 0L);
        distribution.put("18-25岁", 0L);
        distribution.put("26-35岁", 0L);
        distribution.put("36-45岁", 0L);
        distribution.put("46-55岁", 0L);
        distribution.put("56岁以上", 0L);
        
        // 统计年龄分布
        for (FormData data : formDataList) {
            Map<String, Object> originalData = data.getOriginalData();
            if (originalData != null && originalData.containsKey(formItem.getFormItemId())) {
                Object value = originalData.get(formItem.getFormItemId());
                if (value != null) {
                    try {
                        // 尝试从答案中提取年龄数字
                        String content = String.valueOf(value);
                        int age = extractAge(content);
                        
                        if (age > 0) {
                            if (age < 18) {
                                distribution.put("18岁以下", distribution.get("18岁以下") + 1);
                            } else if (age <= 25) {
                                distribution.put("18-25岁", distribution.get("18-25岁") + 1);
                            } else if (age <= 35) {
                                distribution.put("26-35岁", distribution.get("26-35岁") + 1);
                            } else if (age <= 45) {
                                distribution.put("36-45岁", distribution.get("36-45岁") + 1);
                            } else if (age <= 55) {
                                distribution.put("46-55岁", distribution.get("46-55岁") + 1);
                            } else {
                                distribution.put("56岁以上", distribution.get("56岁以上") + 1);
                            }
                        }
                    } catch (Exception e) {
                        // 如果无法解析年龄，跳过
                    }
                }
            }
        }
        
        return distribution;
    }

    /**
     * 从文本中提取年龄数字
     */
    private int extractAge(String text) {
        // 尝试提取数字
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d+");
        java.util.regex.Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            int age = Integer.parseInt(matcher.group());
            if (age >= 0 && age <= 120) {
                return age;
            }
        }
        return 0;
    }

    @Override
    public Map<String, Object> compareAnalysis(Long surveyId, String compareVariable) {
        Map<String, Object> result = new HashMap<>();

        // 1. 获取表单配置
        FormConfig formConfig = formConfigService.getBySurveyId(surveyId);
        if (formConfig == null) {
            throw new RuntimeException("表单配置不存在");
        }
        String formKey = formConfig.getFormKey();

        // 2. 获取对比变量的表单项
        LambdaQueryWrapper<FormItem> compareItemWrapper = 
            new LambdaQueryWrapper<>();
        compareItemWrapper.eq(com.server.surveyanalystserver.entity.FormItem::getFormItemId, compareVariable)
                         .eq(FormItem::getFormKey, formKey)
                         .last("LIMIT 1");
        FormItem compareItem = formItemMapper.selectOne(compareItemWrapper);
        if (compareItem == null) {
            throw new RuntimeException("对比变量不存在");
        }

        // 3. 获取所有表单项（除了对比变量）
        List<FormItem> allItems = formItemMapper.selectList(
            new LambdaQueryWrapper<FormItem>()
                .eq(FormItem::getFormKey, formKey)
        );

        // 4. 获取所有表单数据
        Page<FormData> page = 
            new Page<>(1, 10000);
        Page<FormData> formDataPage = 
            formDataService.getFormDataList(page, formKey);
        List<FormData> formDataList = formDataPage.getRecords();

        // 5. 解析对比变量的选项
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        Map<String, Object> compareScheme = null;
        try {
            if (compareItem.getScheme() != null) {
                compareScheme = objectMapper.readValue(compareItem.getScheme(), Map.class);
            }
        } catch (Exception e) {
            throw new RuntimeException("解析对比变量配置失败", e);
        }

        List<String> compareGroups = new ArrayList<>();
        if (compareScheme != null && compareScheme.containsKey("config")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> config = (Map<String, Object>) compareScheme.get("config");
            if (config != null && config.containsKey("options")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> options = (List<Map<String, Object>>) config.get("options");
                if (options != null) {
                    for (Map<String, Object> opt : options) {
                        String label = (String) opt.get("label");
                        if (label != null) {
                            compareGroups.add(label);
                        }
                    }
                }
            }
        }

        // 6. 按对比变量分组数据
        Map<String, List<FormData>> groupedData = new HashMap<>();
        for (String group : compareGroups) {
            groupedData.put(group, new ArrayList<>());
        }

        for (FormData data : formDataList) {
            Map<String, Object> originalData = data.getOriginalData();
            if (originalData != null && originalData.containsKey(compareVariable)) {
                Object value = originalData.get(compareVariable);
                String groupKey = getAnswerKey(value, compareItem);
                if (groupedData.containsKey(groupKey)) {
                    groupedData.get(groupKey).add(data);
                }
            }
        }

        // 7. 对每个其他题目进行对比分析
        List<Map<String, Object>> compareResults = new ArrayList<>();
        for (FormItem item : allItems) {
            if (item.getFormItemId().equals(compareVariable)) {
                continue; // 跳过对比变量本身
            }

            if (!isChoiceType(item.getType())) {
                continue; // 只对比选择题
            }

            Map<String, Object> compareResult = new HashMap<>();
            compareResult.put("questionTitle", item.getLabel());
            compareResult.put("formItemId", item.getFormItemId());
            compareResult.put("groups", compareGroups);

            // 解析题目选项
            Map<String, Object> itemScheme = null;
            try {
                if (item.getScheme() != null) {
                    itemScheme = objectMapper.readValue(item.getScheme(), Map.class);
                }
            } catch (Exception e) {
                continue;
            }

            List<Map<String, Object>> itemOptions = new ArrayList<>();
            if (itemScheme != null && itemScheme.containsKey("config")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> config = (Map<String, Object>) itemScheme.get("config");
                if (config != null && config.containsKey("options")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> options = (List<Map<String, Object>>) config.get("options");
                    if (options != null) {
                        itemOptions = options;
                    }
                }
            }

            // 对每个选项，统计各组的分布
            List<Map<String, Object>> compareData = new ArrayList<>();
            for (Map<String, Object> opt : itemOptions) {
                String optionLabel = (String) opt.get("label");
                String optionValue = String.valueOf(opt.get("value"));
                Map<String, Object> rowData = new HashMap<>();
                rowData.put("optionLabel", optionLabel);

                // 统计每个组的分布
                for (String group : compareGroups) {
                    List<FormData> groupData = groupedData.get(group);
                    int count = 0;
                    for (FormData data : groupData) {
                        Map<String, Object> originalData = data.getOriginalData();
                        if (originalData != null && originalData.containsKey(item.getFormItemId())) {
                            Object value = originalData.get(item.getFormItemId());
                            if (value != null) {
                                if (value instanceof List) {
                                    if (((List<?>) value).contains(optionValue)) {
                                        count++;
                                    }
                                } else {
                                    if (optionValue.equals(String.valueOf(value))) {
                                        count++;
                                    }
                                }
                            }
                        }
                    }
                    int total = groupData.size();
                    double percentage = total > 0 ? (count * 100.0 / total) : 0.0;
                    Map<String, Object> groupStat = new HashMap<>();
                    groupStat.put("count", count);
                    groupStat.put("percentage", Math.round(percentage * 100) / 100.0);
                    rowData.put(group, groupStat);
                }
                compareData.add(rowData);
            }
            compareResult.put("compareData", compareData);
            compareResults.add(compareResult);
        }

        result.put("compareVariable", compareVariable);
        result.put("compareVariableTitle", compareItem.getLabel());
        result.put("results", compareResults);

        return result;
    }

}

