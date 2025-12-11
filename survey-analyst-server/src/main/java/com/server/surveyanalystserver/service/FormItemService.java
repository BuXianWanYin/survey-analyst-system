package com.server.surveyanalystserver.service;

import com.server.surveyanalystserver.entity.FormItem;

import java.util.List;

/**
 * 表单项服务接口
 */
public interface FormItemService {
    /**
     * 批量保存表单项
     */
    void batchSave(String formKey, List<FormItem> items);
    
    /**
     * 根据 formKey 获取表单项列表
     */
    List<FormItem> getByFormKey(String formKey);
    
    /**
     * 删除表单项
     */
    void deleteById(Long id);
    
    /**
     * 根据 formKey 删除所有表单项（逻辑外键联动删除）
     */
    void deleteByFormKey(String formKey);
    
    /**
     * 统计表单项数量
     */
    long countByFormKey(String formKey);
}

