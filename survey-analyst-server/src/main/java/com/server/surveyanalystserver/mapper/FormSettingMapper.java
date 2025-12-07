package com.server.surveyanalystserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.surveyanalystserver.entity.FormSetting;
import org.apache.ibatis.annotations.Mapper;

/**
 * 表单设置 Mapper
 */
@Mapper
public interface FormSettingMapper extends BaseMapper<FormSetting> {
}

