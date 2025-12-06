package com.server.surveyanalystserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.surveyanalystserver.entity.FormItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 表单项 Mapper
 */
@Mapper
public interface FormItemMapper extends BaseMapper<FormItem> {
    /**
     * 根据 formKey 查询表单项列表
     */
    List<FormItem> selectByFormKey(String formKey);
    
    /**
     * 根据 formKey 删除表单项
     */
    void deleteByFormKey(String formKey);
}

