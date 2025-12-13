package com.server.surveyanalystserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.surveyanalystserver.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper接口
 * 提供操作日志实体的数据库操作方法，继承MyBatis Plus的BaseMapper
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}

