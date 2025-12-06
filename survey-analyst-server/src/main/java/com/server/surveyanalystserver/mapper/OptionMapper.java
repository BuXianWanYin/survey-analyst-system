package com.server.surveyanalystserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.surveyanalystserver.entity.Option;
import org.apache.ibatis.annotations.Mapper;

/**
 * 选项Mapper接口
 */
@Mapper
public interface OptionMapper extends BaseMapper<Option> {
}

