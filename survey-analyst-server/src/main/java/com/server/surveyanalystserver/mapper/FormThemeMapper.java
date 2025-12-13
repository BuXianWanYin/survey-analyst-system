package com.server.surveyanalystserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.surveyanalystserver.entity.FormTheme;
import org.apache.ibatis.annotations.Mapper;

/**
 * 表单主题Mapper接口
 * 提供表单主题实体的数据库操作方法，继承MyBatis Plus的BaseMapper
 */
@Mapper
public interface FormThemeMapper extends BaseMapper<FormTheme> {
}

