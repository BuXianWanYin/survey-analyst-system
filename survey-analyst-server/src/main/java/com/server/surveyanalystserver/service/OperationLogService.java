package com.server.surveyanalystserver.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.surveyanalystserver.entity.OperationLog;
import com.server.surveyanalystserver.entity.dto.OperationLogVO;

import java.time.LocalDateTime;

/**
 * 操作日志Service接口
 */
public interface OperationLogService extends IService<OperationLog> {

    /**
     * 分页查询操作日志
     * 查询系统操作日志，支持按用户ID和操作类型筛选
     * @param page 分页参数
     * @param userId 用户ID（可选），用于筛选指定用户的操作日志
     * @param operationType 操作类型（可选），如：查询、新增、更新、删除等
     * @return 操作日志分页列表
     */
    Page<OperationLog> getLogPage(Page<OperationLog> page, Long userId, String operationType);

    /**
     * 分页查询操作日志（包含用户名）
     * 查询系统操作日志，包含用户名称等关联信息，支持多条件筛选
     * @param page 分页参数
     * @param userId 用户ID（可选），用于筛选指定用户的操作日志
     * @param operationType 操作类型（可选），如：查询、新增、更新、删除等
     * @param startTime 开始时间（可选），用于时间范围筛选
     * @param endTime 结束时间（可选），用于时间范围筛选
     * @return 操作日志分页列表（包含用户名）
     */
    Page<OperationLogVO> getLogPageWithUsername(
            Page<OperationLogVO> page, 
            Long userId, String operationType, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 保存操作日志
     * 异步保存操作日志，记录用户的操作行为
     * @param log 操作日志对象，包含用户ID、操作类型、操作描述等信息
     */
    void saveLog(OperationLog log);

    /**
     * 解析开始时间字符串
     * @param timeStr 时间字符串（格式：yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss）
     * @return LocalDateTime对象，如果解析失败返回null
     */
    LocalDateTime parseDateTime(String timeStr);

    /**
     * 解析结束时间字符串
     * @param timeStr 时间字符串（格式：yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss）
     * @return LocalDateTime对象，如果解析失败返回null
     */
    LocalDateTime parseEndDateTime(String timeStr);
}

