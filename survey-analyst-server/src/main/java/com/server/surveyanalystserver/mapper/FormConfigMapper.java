package com.server.surveyanalystserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.surveyanalystserver.entity.FormConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 表单配置Mapper接口
 * 提供表单配置实体的数据库操作方法，继承MyBatis Plus的BaseMapper
 */
@Mapper
public interface FormConfigMapper extends BaseMapper<FormConfig> {
}

