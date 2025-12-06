package com.server.surveyanalystserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.surveyanalystserver.entity.StatisticsCache;
import org.apache.ibatis.annotations.Mapper;

/**
 * 统计缓存Mapper接口
 */
@Mapper
public interface StatisticsCacheMapper extends BaseMapper<StatisticsCache> {
}

