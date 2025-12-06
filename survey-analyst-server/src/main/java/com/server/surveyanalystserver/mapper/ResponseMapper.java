package com.server.surveyanalystserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.surveyanalystserver.entity.Response;
import org.apache.ibatis.annotations.Mapper;

/**
 * 填写记录Mapper接口
 */
@Mapper
public interface ResponseMapper extends BaseMapper<Response> {
}

