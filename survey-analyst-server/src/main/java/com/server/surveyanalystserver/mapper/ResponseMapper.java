package com.server.surveyanalystserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.surveyanalystserver.entity.Response;
import org.apache.ibatis.annotations.Mapper;

/**
 * 填写记录Mapper接口
 * 提供填写记录实体的数据库操作方法，继承MyBatis Plus的BaseMapper
 */
@Mapper
public interface ResponseMapper extends BaseMapper<Response> {
}

