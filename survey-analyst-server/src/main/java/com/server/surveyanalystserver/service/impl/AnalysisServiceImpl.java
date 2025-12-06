package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.surveyanalystserver.entity.*;
import com.server.surveyanalystserver.mapper.*;
import com.server.surveyanalystserver.service.AnalysisService;
import com.server.surveyanalystserver.utils.DataCleaningUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 分析Service实现类
 */
@Service
public class AnalysisServiceImpl implements AnalysisService {

    @Autowired
    private ResponseMapper responseMapper;

    @Autowired
    private AnswerMapper answerMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private OptionMapper optionMapper;

    @Override
    public Map<String, Object> crossAnalysis(Long surveyId, Long questionId1, Long questionId2) {
        Map<String, Object> result = new HashMap<>();

        Question question1 = questionMapper.selectById(questionId1);
        Question question2 = questionMapper.selectById(questionId2);

        if (question1 == null || question2 == null) {
            throw new RuntimeException("题目不存在");
        }

        Map<String, Object> question1Info = new HashMap<>();
        question1Info.put("id", questionId1);
        question1Info.put("title", question1.getTitle());
        result.put("question1", question1Info);

        Map<String, Object> question2Info = new HashMap<>();
        question2Info.put("id", questionId2);
        question2Info.put("title", question2.getTitle());
        result.put("question2", question2Info);

        // 获取所有填写记录
        LambdaQueryWrapper<Response> responseWrapper = new LambdaQueryWrapper<>();
        responseWrapper.eq(Response::getSurveyId, surveyId)
                       .eq(Response::getStatus, "COMPLETED");
        List<Response> responses = responseMapper.selectList(responseWrapper);

        // 获取两个题目的所有答案
        LambdaQueryWrapper<Answer> answerWrapper1 = new LambdaQueryWrapper<>();
        answerWrapper1.eq(Answer::getQuestionId, questionId1);
        List<Answer> answers1 = answerMapper.selectList(answerWrapper1);

        LambdaQueryWrapper<Answer> answerWrapper2 = new LambdaQueryWrapper<>();
        answerWrapper2.eq(Answer::getQuestionId, questionId2);
        List<Answer> answers2 = answerMapper.selectList(answerWrapper2);

        // 构建答案映射（responseId -> answer）
        Map<Long, Answer> answerMap1 = answers1.stream()
                .collect(Collectors.toMap(Answer::getResponseId, a -> a, (a1, a2) -> a1));
        Map<Long, Answer> answerMap2 = answers2.stream()
                .collect(Collectors.toMap(Answer::getResponseId, a -> a, (a1, a2) -> a1));

        // 构建交叉表
        Map<String, Map<String, Integer>> crossTable = new HashMap<>();

        for (Response response : responses) {
            Answer answer1 = answerMap1.get(response.getId());
            Answer answer2 = answerMap2.get(response.getId());

            if (answer1 != null && answer2 != null) {
                String key1 = getAnswerKey(answer1, question1);
                String key2 = getAnswerKey(answer2, question2);

                crossTable.computeIfAbsent(key1, k -> new HashMap<>())
                         .merge(key2, 1, Integer::sum);
            }
        }

        result.put("crossTable", crossTable);
        result.put("totalCount", responses.size());

        return result;
    }

    @Override
    public Map<String, Object> trendAnalysis(Long surveyId, Long questionId, String timeRange) {
        Map<String, Object> result = new HashMap<>();

        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new RuntimeException("题目不存在");
        }

        result.put("questionId", questionId);
        result.put("questionTitle", question.getTitle());

        // 获取时间范围内的填写记录
        LambdaQueryWrapper<Response> responseWrapper = new LambdaQueryWrapper<>();
        responseWrapper.eq(Response::getSurveyId, surveyId)
                       .eq(Response::getStatus, "COMPLETED")
                       .orderByAsc(Response::getSubmitTime);

        LocalDateTime startTime = null;
        if ("7d".equals(timeRange)) {
            startTime = LocalDateTime.now().minusDays(7);
        } else if ("30d".equals(timeRange)) {
            startTime = LocalDateTime.now().minusDays(30);
        }

        if (startTime != null) {
            responseWrapper.ge(Response::getSubmitTime, startTime);
        }

        List<Response> responses = responseMapper.selectList(responseWrapper);

        // 获取该题目的答案
        LambdaQueryWrapper<Answer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(Answer::getQuestionId, questionId);
        List<Answer> answers = answerMapper.selectList(answerWrapper);

        // 构建答案映射
        Map<Long, Answer> answerMap = answers.stream()
                .collect(Collectors.toMap(Answer::getResponseId, a -> a, (a1, a2) -> a1));

        // 按日期和选项统计趋势
        Map<String, Map<String, Integer>> trendData = new HashMap<>();

        for (Response response : responses) {
            if (response.getSubmitTime() == null) continue;

            String date = response.getSubmitTime().toLocalDate().toString();
            Answer answer = answerMap.get(response.getId());

            if (answer != null) {
                String answerKey = getAnswerKey(answer, question);
                trendData.computeIfAbsent(date, k -> new HashMap<>())
                        .merge(answerKey, 1, Integer::sum);
            }
        }

        result.put("trendData", trendData);
        result.put("totalCount", responses.size());

        return result;
    }

    @Override
    public Map<String, Object> profileAnalysis(Long surveyId) {
        Map<String, Object> result = new HashMap<>();

        // 获取填写记录
        LambdaQueryWrapper<Response> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Response::getSurveyId, surveyId)
               .eq(Response::getStatus, "COMPLETED");
        List<Response> responses = responseMapper.selectList(wrapper);

        // 获取所有题目和答案，用于识别性别、年龄、地域
        List<Question> questions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>().eq(Question::getSurveyId, surveyId)
        );
        
        // 识别性别、年龄、地域题目（通过题目标题关键词）
        Question genderQuestion = null;
        Question ageQuestion = null;
        Question regionQuestion = null;
        
        for (Question q : questions) {
            String title = q.getTitle().toLowerCase();
            if (title.contains("性别") || title.contains("gender") || title.contains("男") || title.contains("女")) {
                genderQuestion = q;
            } else if (title.contains("年龄") || title.contains("age") || title.contains("岁")) {
                ageQuestion = q;
            } else if (title.contains("地域") || title.contains("地区") || title.contains("region") || 
                      title.contains("城市") || title.contains("省份") || title.contains("城市")) {
                regionQuestion = q;
            }
        }
        
        // 统计性别分布
        if (genderQuestion != null) {
            Map<String, Long> genderDistribution = analyzeQuestionDistribution(genderQuestion.getId(), responses);
            result.put("genderDistribution", genderDistribution);
        }
        
        // 统计年龄分布
        if (ageQuestion != null) {
            Map<String, Long> ageDistribution = analyzeAgeDistribution(ageQuestion.getId(), responses);
            result.put("ageDistribution", ageDistribution);
        }
        
        // 统计地域分布
        if (regionQuestion != null) {
            Map<String, Long> regionDistribution = analyzeQuestionDistribution(regionQuestion.getId(), responses);
            result.put("regionDistribution", regionDistribution);
        }

        // 设备类型分布
        Map<String, Long> deviceDistribution = responses.stream()
                .filter(r -> r.getDeviceType() != null)
                .collect(Collectors.groupingBy(
                        Response::getDeviceType,
                        Collectors.counting()
                ));
        result.put("deviceDistribution", deviceDistribution);

        // 填写时间分布（按小时）
        Map<Integer, Long> hourDistribution = responses.stream()
                .filter(r -> r.getSubmitTime() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getSubmitTime().getHour(),
                        Collectors.counting()
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
    private String getAnswerKey(Answer answer, Question question) {
        if (answer.getOptionId() != null) {
            Option option = optionMapper.selectById(answer.getOptionId());
            return option != null ? option.getContent() : "未知选项";
        } else if (answer.getContent() != null) {
            return answer.getContent();
        }
        return "未填写";
    }

    /**
     * 分析题目分布（用于性别、地域等）
     */
    private Map<String, Long> analyzeQuestionDistribution(Long questionId, List<Response> responses) {
        Map<String, Long> distribution = new HashMap<>();
        
        // 获取该题目的所有答案
        LambdaQueryWrapper<Answer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(Answer::getQuestionId, questionId);
        List<Answer> answers = answerMapper.selectList(answerWrapper);
        
        // 构建答案映射
        Map<Long, Answer> answerMap = answers.stream()
                .collect(Collectors.toMap(Answer::getResponseId, a -> a, (a1, a2) -> a1));
        
        // 统计分布
        Question question = questionMapper.selectById(questionId);
        for (Response response : responses) {
            Answer answer = answerMap.get(response.getId());
            if (answer != null) {
                String key = getAnswerKey(answer, question);
                distribution.put(key, distribution.getOrDefault(key, 0L) + 1);
            }
        }
        
        return distribution;
    }

    /**
     * 分析年龄分布（将年龄分组）
     */
    private Map<String, Long> analyzeAgeDistribution(Long questionId, List<Response> responses) {
        Map<String, Long> distribution = new HashMap<>();
        distribution.put("18岁以下", 0L);
        distribution.put("18-25岁", 0L);
        distribution.put("26-35岁", 0L);
        distribution.put("36-45岁", 0L);
        distribution.put("46-55岁", 0L);
        distribution.put("56岁以上", 0L);
        
        // 获取该题目的所有答案
        LambdaQueryWrapper<Answer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(Answer::getQuestionId, questionId);
        List<Answer> answers = answerMapper.selectList(answerWrapper);
        
        // 构建答案映射
        Map<Long, Answer> answerMap = answers.stream()
                .collect(Collectors.toMap(Answer::getResponseId, a -> a, (a1, a2) -> a1));
        
        // 统计年龄分布
        for (Response response : responses) {
            Answer answer = answerMap.get(response.getId());
            if (answer != null && answer.getContent() != null) {
                try {
                    // 尝试从答案中提取年龄数字
                    String content = answer.getContent();
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
}

