package com.server.surveyanalystserver.service;

import com.server.surveyanalystserver.entity.FormItem;

import java.util.List;
import java.util.Map;

/**
 * 表单项服务接口
 */
public interface FormItemService {
    /**
     * 批量保存表单项
     */
    void batchSave(String formKey, List<FormItem> items);
    
    /**
     * 批量保存表单项（从Map数据转换）
     * @param formKey 表单Key
     * @param itemsData 表单项数据列表（Map格式）
     */
    void batchSaveFromMap(String formKey, List<Map<String, Object>> itemsData);
    
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

