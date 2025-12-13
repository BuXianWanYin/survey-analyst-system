package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.surveyanalystserver.entity.OperationLog;
import com.server.surveyanalystserver.entity.Response;
import com.server.surveyanalystserver.entity.Survey;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.service.OperationLogService;
import com.server.surveyanalystserver.service.ResponseService;
import com.server.surveyanalystserver.service.SurveyService;
import com.server.surveyanalystserver.service.SystemService;
import com.server.surveyanalystserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统统计Service实现类
 */
@Service
public class SystemServiceImpl implements SystemService {

    @Autowired
    private UserService userService;

    @Autowired
    private SurveyService surveyService;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private OperationLogService operationLogService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 获取系统概览统计
     * 统计系统中的用户数、问卷数、填写记录数等基础数据
     * @return 系统概览数据Map，包含totalUsers、activeUsers、totalSurveys、publishedSurveys、totalResponses、completedResponses等字段
     */
    @Override
    public Map<String, Object> getSystemOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        long totalUsers = userService.count();
        long activeUsers = userService.count(new LambdaQueryWrapper<User>().eq(User::getStatus, 1));
        overview.put("totalUsers", totalUsers);
        overview.put("activeUsers", activeUsers);
        
        long totalSurveys = surveyService.count();
        long publishedSurveys = surveyService.count(new LambdaQueryWrapper<Survey>().eq(Survey::getStatus, "PUBLISHED"));
        overview.put("totalSurveys", totalSurveys);
        overview.put("publishedSurveys", publishedSurveys);
        
        long totalResponses = responseService.count();
        long completedResponses = responseService.count(new LambdaQueryWrapper<Response>().eq(Response::getStatus, "COMPLETED"));
        overview.put("totalResponses", totalResponses);
        overview.put("completedResponses", completedResponses);
        
        return overview;
    }

    /**
     * 获取今日创建的问卷数量
     * 统计今天（从00:00:00开始）创建的问卷数量
     * @return 统计数据Map，包含todaySurveys字段
     */
    @Override
    public Map<String, Object> getTodaySurveys() {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = todayStart.plusDays(1);
        
        long todaySurveys = surveyService.count(
            new LambdaQueryWrapper<Survey>()
                .ge(Survey::getCreateTime, todayStart)
                .lt(Survey::getCreateTime, todayEnd)
        );
        
        Map<String, Object> result = new HashMap<>();
        result.put("todaySurveys", todaySurveys);
        return result;
    }

    /**
     * 获取问卷创建趋势数据
     * 按日期统计指定时间范围内的问卷创建数量
     * @param timeRange 时间范围（7d-7天，30d-30天，90d-90天，1y-1年）
     * @return 趋势数据列表，每个元素包含date和count字段
     */
    @Override
    public List<Map<String, Object>> getSurveyCreateTrend(String timeRange) {
        LocalDateTime startTime = parseTimeRange(timeRange);
        
        List<Survey> surveys = surveyService.list(
            new LambdaQueryWrapper<Survey>()
                .ge(Survey::getCreateTime, startTime)
                .orderByAsc(Survey::getCreateTime)
        );
        
        Map<String, Long> trendMap = new HashMap<>();
        for (Survey survey : surveys) {
            if (survey.getCreateTime() != null) {
                String date = survey.getCreateTime().toLocalDate().format(DATE_FORMATTER);
                trendMap.put(date, trendMap.getOrDefault(date, 0L) + 1);
            }
        }
        
        return generateDateSequence(startTime.toLocalDate(), LocalDate.now(), trendMap, "count");
    }

    /**
     * 获取填写记录趋势数据
     * 按日期统计指定时间范围内的填写记录数量
     * @param timeRange 时间范围（7d-7天，30d-30天，90d-90天，1y-1年）
     * @return 趋势数据列表，每个元素包含date、total、completed字段
     */
    @Override
    public List<Map<String, Object>> getResponseTrend(String timeRange) {
        LocalDateTime startTime = parseTimeRange(timeRange);
        
        List<Response> responses = responseService.list(
            new LambdaQueryWrapper<Response>()
                .ge(Response::getCreateTime, startTime)
                .orderByAsc(Response::getCreateTime)
        );
        
        Map<String, Long> totalMap = new HashMap<>();
        for (Response response : responses) {
            if (response.getCreateTime() != null) {
                String date = response.getCreateTime().toLocalDate().format(DATE_FORMATTER);
                totalMap.put(date, totalMap.getOrDefault(date, 0L) + 1);
            }
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate currentDate = startTime.toLocalDate();
        LocalDate endDate = LocalDate.now();
        
        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.format(DATE_FORMATTER);
            long total = totalMap.getOrDefault(dateStr, 0L);
            Map<String, Object> item = new HashMap<>();
            item.put("date", dateStr);
            item.put("total", total);
            item.put("completed", total);
            result.add(item);
            currentDate = currentDate.plusDays(1);
        }
        
        return result;
    }

    /**
     * 获取登录趋势数据
     * 按日期统计指定时间范围内的用户登录次数
     * @param timeRange 时间范围（7d-7天，30d-30天，90d-90天，1y-1年）
     * @return 趋势数据列表，每个元素包含date和count字段
     */
    @Override
    public List<Map<String, Object>> getLoginTrend(String timeRange) {
        LocalDateTime startTime = parseTimeRange(timeRange);
        
        List<OperationLog> logs = operationLogService.list(
            new LambdaQueryWrapper<OperationLog>()
                .eq(OperationLog::getOperationType, "登录")
                .ge(OperationLog::getCreateTime, startTime)
                .orderByAsc(OperationLog::getCreateTime)
        );
        
        Map<String, Long> trendMap = new HashMap<>();
        for (OperationLog log : logs) {
            if (log.getCreateTime() != null) {
                String date = log.getCreateTime().toLocalDate().format(DATE_FORMATTER);
                trendMap.put(date, trendMap.getOrDefault(date, 0L) + 1);
            }
        }
        
        return generateDateSequence(startTime.toLocalDate(), LocalDate.now(), trendMap, "count");
    }

    /**
     * 获取填写时长趋势数据
     * 按日期统计指定时间范围内已完成填写记录的平均填写时长
     * @param timeRange 时间范围（7d-7天，30d-30天，90d-90天，1y-1年）
     * @return 趋势数据列表，每个元素包含date、avgDuration、count字段
     */
    @Override
    public List<Map<String, Object>> getResponseDurationTrend(String timeRange) {
        LocalDateTime startTime = parseTimeRange(timeRange);
        
        List<Response> responses = responseService.list(
            new LambdaQueryWrapper<Response>()
                .ge(Response::getCreateTime, startTime)
                .eq(Response::getStatus, "COMPLETED")
                .isNotNull(Response::getDuration)
                .orderByAsc(Response::getCreateTime)
        );
        
        Map<String, List<Integer>> durationMap = new HashMap<>();
        for (Response response : responses) {
            if (response.getCreateTime() != null && response.getDuration() != null) {
                String date = response.getCreateTime().toLocalDate().format(DATE_FORMATTER);
                durationMap.computeIfAbsent(date, k -> new ArrayList<>()).add(response.getDuration());
            }
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate currentDate = startTime.toLocalDate();
        LocalDate endDate = LocalDate.now();
        
        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.format(DATE_FORMATTER);
            Map<String, Object> item = new HashMap<>();
            item.put("date", dateStr);
            
            List<Integer> durations = durationMap.get(dateStr);
            if (durations != null && !durations.isEmpty()) {
                double avgDuration = durations.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                item.put("avgDuration", Math.round(avgDuration * 100.0) / 100.0);
                item.put("count", durations.size());
            } else {
                item.put("avgDuration", 0.0);
                item.put("count", 0);
            }
            result.add(item);
            currentDate = currentDate.plusDays(1);
        }
        
        return result;
    }

    private LocalDateTime parseTimeRange(String timeRange) {
        if ("7d".equals(timeRange)) {
            return LocalDateTime.now().minusDays(7);
        } else if ("30d".equals(timeRange)) {
            return LocalDateTime.now().minusDays(30);
        } else if ("90d".equals(timeRange)) {
            return LocalDateTime.now().minusDays(90);
        } else if ("1y".equals(timeRange)) {
            return LocalDateTime.now().minusYears(1);
        } else {
            return LocalDateTime.now().minusDays(30);
        }
    }

    private List<Map<String, Object>> generateDateSequence(LocalDate startDate, LocalDate endDate, 
                                                           Map<String, Long> dataMap, String countKey) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.format(DATE_FORMATTER);
            Map<String, Object> item = new HashMap<>();
            item.put("date", dateStr);
            item.put(countKey, dataMap.getOrDefault(dateStr, 0L));
            result.add(item);
            currentDate = currentDate.plusDays(1);
        }
        
        return result;
    }
}

