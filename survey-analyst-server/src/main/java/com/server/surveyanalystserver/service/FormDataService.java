package com.server.surveyanalystserver.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.surveyanalystserver.entity.FormData;

import java.time.LocalDateTime;
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
     * @param startTime 开始时间
     * @param browser 浏览器
     * @param os 操作系统
     * @param uaInfo UA信息
     * @return 保存的表单数据
     */
    FormData saveFormData(String formKey, Map<String, Object> originalData, String ipAddress, String deviceId, Long userId, LocalDateTime startTime, String browser, String os, Map<String, Object> uaInfo);
    
    /**
     * 填写前校验（检查各种限制）
     * @param formKey 表单key
     * @param ipAddress IP地址
     * @param deviceId 设备ID
     * @param userId 用户ID
     * @throws RuntimeException 如果校验失败，抛出异常
     */
    void validateBeforeFill(String formKey, String ipAddress, String deviceId, Long userId);
    
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

