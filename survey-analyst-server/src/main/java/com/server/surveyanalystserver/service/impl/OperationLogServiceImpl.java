package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.OperationLog;
import com.server.surveyanalystserver.mapper.OperationLogMapper;
import com.server.surveyanalystserver.service.OperationLogService;
import org.springframework.stereotype.Service;

/**
 * 操作日志Service实现类
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    public Page<OperationLog> getLogPage(Page<OperationLog> page, Long userId, String operationType) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq(OperationLog::getUserId, userId);
        }
        
        if (operationType != null && !operationType.isEmpty()) {
            wrapper.eq(OperationLog::getOperationType, operationType);
        }
        
        wrapper.orderByDesc(OperationLog::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public void saveLog(OperationLog log) {
        this.save(log);
    }
}

