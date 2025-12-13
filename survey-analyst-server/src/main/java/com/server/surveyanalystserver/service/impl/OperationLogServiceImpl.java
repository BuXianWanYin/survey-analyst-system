package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.OperationLog;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.entity.dto.OperationLogVO;
import com.server.surveyanalystserver.mapper.OperationLogMapper;
import com.server.surveyanalystserver.mapper.UserMapper;
import com.server.surveyanalystserver.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志Service实现类
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 分页查询操作日志
     * 支持按用户ID和操作类型筛选
     * @param page 分页参数
     * @param userId 用户ID，可选
     * @param operationType 操作类型（模糊匹配），可选
     * @return 操作日志分页列表
     */
    @Override
    public Page<OperationLog> getLogPage(Page<OperationLog> page, Long userId, String operationType) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq(OperationLog::getUserId, userId);
        }
        
        if (operationType != null && !operationType.isEmpty()) {
            wrapper.like(OperationLog::getOperationType, operationType);
        }
        
        wrapper.orderByDesc(OperationLog::getCreateTime);
        return this.page(page, wrapper);
    }

    /**
     * 保存操作日志
     * @param log 操作日志对象
     */
    @Override
    public void saveLog(OperationLog log) {
        this.save(log);
    }

    /**
     * 分页查询操作日志（包含用户名）
     * 支持按用户ID、操作类型、时间区间筛选，返回包含用户名的日志列表
     * @param page 分页参数
     * @param userId 用户ID，可选
     * @param operationType 操作类型（模糊匹配），可选
     * @param startTime 开始时间，可选
     * @param endTime 结束时间，可选（会自动设置为当天的23:59:59）
     * @return 操作日志分页列表（包含用户名）
     */
    @Override
    public Page<OperationLogVO> getLogPageWithUsername(Page<OperationLogVO> page, Long userId, String operationType, 
            LocalDateTime startTime, LocalDateTime endTime) {
        // 先查询日志
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq(OperationLog::getUserId, userId);
        }
        
        if (operationType != null && !operationType.isEmpty()) {
            wrapper.like(OperationLog::getOperationType, operationType);
        }
        
        // 时间区间筛选
        if (startTime != null) {
            wrapper.ge(OperationLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            LocalDateTime endDateTime = endTime.toLocalDate().atTime(23, 59, 59);
            wrapper.le(OperationLog::getCreateTime, endDateTime);
        }
        
        wrapper.orderByDesc(OperationLog::getCreateTime);
        Page<OperationLog> logPage = new Page<>(page.getCurrent(), page.getSize());
        Page<OperationLog> result = this.page(logPage, wrapper);
        
        // 转换为VO并填充用户名
        Page<OperationLogVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<OperationLogVO> voList = result.getRecords().stream().map(log -> {
            OperationLogVO vo = new OperationLogVO();
            vo.setId(log.getId());
            vo.setUserId(log.getUserId());
            vo.setOperationType(log.getOperationType());
            vo.setOperationDesc(log.getOperationDesc());
            vo.setRequestUrl(log.getRequestUrl());
            vo.setRequestMethod(log.getRequestMethod());
            vo.setRequestParams(log.getRequestParams());
            vo.setResponseResult(log.getResponseResult());
            vo.setIpAddress(log.getIpAddress());
            vo.setCreateTime(log.getCreateTime());
            
            // 查询用户名
            if (log.getUserId() != null) {
                User user = userMapper.selectById(log.getUserId());
                if (user != null) {
                    vo.setUsername(user.getUsername());
                }
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 解析日期时间字符串
     * 支持两种格式：yyyy-MM-dd（解析为当天的00:00:00）和yyyy-MM-dd HH:mm:ss
     * @param timeStr 日期时间字符串
     * @return LocalDateTime对象，如果解析失败则返回null
     */
    @Override
    public LocalDateTime parseDateTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        try {
            if (timeStr.length() == 10) {
                return LocalDate.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .atStartOfDay();
            } else {
                return LocalDateTime.parse(timeStr, 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析结束日期时间字符串
     * 支持两种格式：yyyy-MM-dd（解析为当天的23:59:59）和yyyy-MM-dd HH:mm:ss
     * @param timeStr 日期时间字符串
     * @return LocalDateTime对象，如果解析失败则返回null
     */
    @Override
    public LocalDateTime parseEndDateTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        try {
            if (timeStr.length() == 10) {
                return LocalDate.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .atTime(23, 59, 59);
            } else {
                return LocalDateTime.parse(timeStr, 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        } catch (Exception e) {
            return null;
        }
    }
}

