package com.server.surveyanalystserver.utils;

import com.server.surveyanalystserver.entity.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据清洗工具类
 */
public class DataCleaningUtils {

    /**
     * 去除重复数据（根据IP、时间等）
     * @param responses 填写记录列表
     * @return 去重后的填写记录列表
     */
    public static List<Response> removeDuplicates(List<Response> responses) {
        if (responses == null || responses.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> seen = new HashSet<>();
        List<Response> result = new ArrayList<>();

        for (Response response : responses) {
            // 使用IP地址和提交时间作为唯一标识
            String key = response.getIpAddress() + "_" + 
                        (response.getSubmitTime() != null ? response.getSubmitTime().toString() : "");
            
            if (!seen.contains(key)) {
                seen.add(key);
                result.add(response);
            }
        }

        return result;
    }

    /**
     * 过滤异常数据（填写时长过短、过长等）
     * @param responses 填写记录列表
     * @return 过滤后的填写记录列表
     */
    public static List<Response> filterAbnormalData(List<Response> responses) {
        if (responses == null || responses.isEmpty()) {
            return new ArrayList<>();
        }

        return responses.stream()
                .filter(response -> {
                    // 过滤条件：
                    // 1. 填写时长不能为null
                    // 2. 填写时长不能小于5秒（可能是误操作）
                    // 3. 填写时长不能超过1小时（可能是异常数据）
                    if (response.getDuration() == null) {
                        return false;
                    }
                    return response.getDuration() >= 5 && response.getDuration() <= 3600;
                })
                .collect(Collectors.toList());
    }

}

