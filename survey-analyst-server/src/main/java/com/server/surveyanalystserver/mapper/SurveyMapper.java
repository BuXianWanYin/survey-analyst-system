package com.server.surveyanalystserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.surveyanalystserver.entity.Survey;
import org.apache.ibatis.annotations.Mapper;

/**
 * 问卷Mapper接口
 */
@Mapper
public interface SurveyMapper extends BaseMapper<Survey> {
}

