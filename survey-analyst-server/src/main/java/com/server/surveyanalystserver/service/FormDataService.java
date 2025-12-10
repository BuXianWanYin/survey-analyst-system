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
     * @param formKey 表单key
     * @param originalData 原始数据
     * @param ipAddress IP地址
     * @param deviceId 设备ID
     * @param userId 用户ID
     * @return 保存的表单数据
     */
    FormData saveFormData(String formKey, Map<String, Object> originalData, String ipAddress, String deviceId, Long userId);
    
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

