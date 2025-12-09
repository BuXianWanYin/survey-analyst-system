package com.server.surveyanalystserver.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.surveyanalystserver.entity.OperationLog;

/**
 * 操作日志Service接口
 */
public interface OperationLogService extends IService<OperationLog> {

    /**
     * 分页查询操作日志
     * @param page 分页参数
     * @param userId 用户ID（可选）
     * @param operationType 操作类型（可选）
     * @return 日志分页列表
     */
    Page<OperationLog> getLogPage(Page<OperationLog> page, Long userId, String operationType);

    /**
     * 保存操作日志
     * @param log 日志信息
     */
    void saveLog(OperationLog log);
}

