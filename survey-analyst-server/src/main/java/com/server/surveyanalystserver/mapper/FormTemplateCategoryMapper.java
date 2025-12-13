package com.server.surveyanalystserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.surveyanalystserver.entity.FormTemplateCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 表单模板分类Mapper接口
 * 提供表单模板分类实体的数据库操作方法，继承MyBatis Plus的BaseMapper
 */
@Mapper
public interface FormTemplateCategoryMapper extends BaseMapper<FormTemplateCategory> {
}

