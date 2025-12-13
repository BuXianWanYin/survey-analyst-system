package com.server.surveyanalystserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.surveyanalystserver.entity.FormItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 表单项Mapper接口
 * 提供表单项实体的数据库操作方法，继承MyBatis Plus的BaseMapper
 */
@Mapper
@SuppressWarnings("all")
public interface FormItemMapper extends BaseMapper<FormItem> {
    /**
     * 根据表单Key查询表单项列表
     * @param formKey 表单Key
     * @return 表单项列表
     */
    @Select("SELECT * FROM form_item WHERE form_key = #{formKey}")
    List<FormItem> selectByFormKey(@Param("formKey") String formKey);
}

