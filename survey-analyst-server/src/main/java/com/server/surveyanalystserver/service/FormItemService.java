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
     * 批量保存表单项列表到数据库
     * @param formKey 表单Key
     * @param items 表单项列表
     */
    void batchSave(String formKey, List<FormItem> items);
    
    /**
     * 批量保存表单项（从Map数据转换）
     * 将Map格式的表单项数据转换为FormItem实体并批量保存
     * @param formKey 表单Key
     * @param itemsData 表单项数据列表（Map格式）
     */
    void batchSaveFromMap(String formKey, List<Map<String, Object>> itemsData);
    
    /**
     * 根据表单Key获取表单项列表
     * 查询指定表单的所有表单项
     * @param formKey 表单Key
     * @return 表单项列表
     */
    List<FormItem> getByFormKey(String formKey);
    
    /**
     * 根据ID删除表单项
     * 删除指定的表单项（逻辑删除）
     * @param id 表单项ID
     */
    void deleteById(Long id);
    
    /**
     * 根据表单Key删除所有表单项
     * 删除指定表单的所有表单项，用于逻辑外键联动删除
     * @param formKey 表单Key
     */
    void deleteByFormKey(String formKey);
    
    /**
     * 统计表单项数量
     * 统计指定表单的表单项总数
     * @param formKey 表单Key
     * @return 表单项数量
     */
    long countByFormKey(String formKey);

    /**
     * 批量保存表单项（包含form_config的创建和更新逻辑）
     * @param formKey 表单Key
     * @param itemsData 表单项数据列表（Map格式）
     * @param surveyId 问卷ID（可选）
     * @param userId 用户ID
     */
    void batchSaveFormItemsWithConfig(String formKey, List<Map<String, Object>> itemsData, Long surveyId, Long userId);
}

