package com.server.surveyanalystserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.surveyanalystserver.entity.FormData;
import org.apache.ibatis.annotations.Mapper;

/**
 * 表单数据 Mapper
 */
@Mapper
public interface FormDataMapper extends BaseMapper<FormData> {
}

