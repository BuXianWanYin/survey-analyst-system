package com.server.surveyanalystserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.surveyanalystserver.entity.FormSetting;
import org.apache.ibatis.annotations.Mapper;

/**
 * 表单设置Mapper接口
 * 提供表单设置实体的数据库操作方法，继承MyBatis Plus的BaseMapper
 */
@Mapper
public interface FormSettingMapper extends BaseMapper<FormSetting> {
}

