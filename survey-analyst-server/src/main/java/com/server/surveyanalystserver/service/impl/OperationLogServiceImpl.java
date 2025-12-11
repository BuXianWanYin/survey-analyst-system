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

import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志Service实现类
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Autowired
    private UserMapper userMapper;

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

    @Override
    public void saveLog(OperationLog log) {
        this.save(log);
    }

    @Override
    public Page<OperationLogVO> getLogPageWithUsername(Page<OperationLogVO> page, Long userId, String operationType, 
            java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
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
            // 结束时间需要包含当天的23:59:59
            java.time.LocalDateTime endDateTime = endTime.toLocalDate().atTime(23, 59, 59);
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
}

