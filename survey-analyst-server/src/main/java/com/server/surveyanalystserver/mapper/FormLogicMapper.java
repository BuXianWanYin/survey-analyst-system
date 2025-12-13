package com.server.surveyanalystserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.surveyanalystserver.entity.FormLogic;
import org.apache.ibatis.annotations.Mapper;

/**
 * 表单逻辑Mapper接口
 * 提供表单逻辑实体的数据库操作方法，继承MyBatis Plus的BaseMapper
 */
@Mapper
public interface FormLogicMapper extends BaseMapper<FormLogic> {
}

