package com.server.surveyanalystserver.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.surveyanalystserver.entity.FormData;

import java.util.Map;

/**
 * 表单数据服务接口
 */
public interface FormDataService extends IService<FormData> {
    /**
     * 保存表单数据
     */
    FormData saveFormData(String formKey, Map<String, Object> originalData);
    
    /**
     * 分页查询表单数据
     */
    Page<FormData> getFormDataList(Page<FormData> page, String formKey);
    
    /**
     * 根据ID获取表单数据
     */
    FormData getFormDataById(Long id);
    
    /**
     * 删除表单数据
     */
    boolean deleteFormData(Long id);
}

