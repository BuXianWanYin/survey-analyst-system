package com.server.surveyanalystserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.surveyanalystserver.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 * 提供用户实体的数据库操作方法，继承MyBatis Plus的BaseMapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}

