package com.server.surveyanalystserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.surveyanalystserver.entity.FormData;
import org.apache.ibatis.annotations.Mapper;

/**
 * 表单数据Mapper接口
 * 提供表单数据实体的数据库操作方法，继承MyBatis Plus的BaseMapper
 */
@Mapper
public interface FormDataMapper extends BaseMapper<FormData> {
}

