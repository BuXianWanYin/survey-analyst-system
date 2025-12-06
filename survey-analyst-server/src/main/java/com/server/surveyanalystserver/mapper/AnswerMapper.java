package com.server.surveyanalystserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.surveyanalystserver.entity.Answer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 填写答案Mapper接口
 */
@Mapper
public interface AnswerMapper extends BaseMapper<Answer> {
}

