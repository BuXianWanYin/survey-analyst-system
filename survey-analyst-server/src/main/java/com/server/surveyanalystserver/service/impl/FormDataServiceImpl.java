package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.FormConfig;
import com.server.surveyanalystserver.entity.FormData;
import com.server.surveyanalystserver.entity.FormSetting;
import com.server.surveyanalystserver.entity.Response;
import com.server.surveyanalystserver.mapper.FormDataMapper;
import com.server.surveyanalystserver.mapper.ResponseMapper;
import com.server.surveyanalystserver.service.FormConfigService;
import com.server.surveyanalystserver.service.FormDataService;
import com.server.surveyanalystserver.service.FormSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 表单数据服务实现类
 */
@Service
public class FormDataServiceImpl extends ServiceImpl<FormDataMapper, FormData> implements FormDataService {
    
    @Autowired
    private FormConfigService formConfigService;
    
    @Autowired
    private FormSettingService formSettingService;
    
    @Autowired
    private ResponseMapper responseMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FormData saveFormData(String formKey, Map<String, Object> originalData, String ipAddress, String deviceId, Long userId) {
        // 获取表单配置，用于获取surveyId
        FormConfig formConfig = formConfigService.getByFormKey(formKey);
        if (formConfig == null) {
            throw new RuntimeException("表单配置不存在");
        }
        
        Long surveyId = formConfig.getSurveyId();
        
        // 获取表单设置，检查限制
        FormSetting formSetting = formSettingService.getBySurveyId(surveyId);
        if (formSetting != null && formSetting.getSettings() != null) {
            Map<String, Object> settings = formSetting.getSettings();
            
            // 检查IP限制
            if (ipAddress != null && !ipAddress.isEmpty()) {
                Object ipLimitStatusObj = settings.get("ipWriteCountLimitStatus");
                boolean ipLimitStatus = false;
                if (ipLimitStatusObj instanceof Boolean) {
                    ipLimitStatus = (Boolean) ipLimitStatusObj;
                } else if (ipLimitStatusObj != null) {
                    ipLimitStatus = Boolean.parseBoolean(ipLimitStatusObj.toString());
                }
                
                if (ipLimitStatus) {
                    Object ipLimitObj = settings.get("ipWriteCountLimit");
                    int ipLimit = 1;
                    if (ipLimitObj instanceof Number) {
                        ipLimit = ((Number) ipLimitObj).intValue();
                    } else if (ipLimitObj != null) {
                        try {
                            ipLimit = Integer.parseInt(ipLimitObj.toString());
                        } catch (NumberFormatException e) {
                            ipLimit = 1;
                        }
                    }
                    
                    // 查询该IP的提交次数（从Response表查询）
                    LambdaQueryWrapper<Response> ipWrapper = new LambdaQueryWrapper<>();
                    ipWrapper.eq(Response::getSurveyId, surveyId)
                             .eq(Response::getIpAddress, ipAddress)
                             .eq(Response::getStatus, "COMPLETED");
                    long ipCount = responseMapper.selectCount(ipWrapper);
                    
                    if (ipCount >= ipLimit) {
                        throw new RuntimeException("该IP地址已达到答题次数限制（" + ipLimit + "次），无法继续提交");
                    }
                }
            }
            
            // 检查设备限制（需要设备ID）
            if (deviceId != null && !deviceId.isEmpty()) {
                Object deviceLimitStatusObj = settings.get("deviceWriteCountLimitStatus");
                boolean deviceLimitStatus = false;
                if (deviceLimitStatusObj instanceof Boolean) {
                    deviceLimitStatus = (Boolean) deviceLimitStatusObj;
                } else if (deviceLimitStatusObj != null) {
                    deviceLimitStatus = Boolean.parseBoolean(deviceLimitStatusObj.toString());
                }
                
                if (deviceLimitStatus) {
                    Object deviceLimitObj = settings.get("deviceWriteCountLimit");
                    int deviceLimit = 1;
                    if (deviceLimitObj instanceof Number) {
                        deviceLimit = ((Number) deviceLimitObj).intValue();
                    } else if (deviceLimitObj != null) {
                        try {
                            deviceLimit = Integer.parseInt(deviceLimitObj.toString());
                        } catch (NumberFormatException e) {
                            deviceLimit = 1;
                        }
                    }
                    
                    // 查询该设备的提交次数（从FormData表查询，通过submit_ua中的设备标识）
                    LambdaQueryWrapper<FormData> deviceWrapper = new LambdaQueryWrapper<>();
                    deviceWrapper.eq(FormData::getFormKey, formKey);
                    // 注意：这里需要根据实际存储的设备标识字段来查询
                    // 如果设备ID存储在submit_ua中，需要特殊处理
                    // 暂时先查询所有，后续可以根据实际需求优化
                    long deviceCount = this.count(deviceWrapper);
                    
                    if (deviceCount >= deviceLimit) {
                        throw new RuntimeException("该设备已达到答题次数限制（" + deviceLimit + "次），无法继续提交");
                    }
                }
            }
            
            // 检查用户限制（系统不支持匿名提交，userId必须不为null）
            if (userId != null) {
                Object userLimitStatusObj = settings.get("accountWriteCountLimitStatus");
                boolean userLimitStatus = false;
                if (userLimitStatusObj instanceof Boolean) {
                    userLimitStatus = (Boolean) userLimitStatusObj;
                } else if (userLimitStatusObj != null) {
                    userLimitStatus = Boolean.parseBoolean(userLimitStatusObj.toString());
                }
                
                if (userLimitStatus) {
                    Object userLimitObj = settings.get("accountWriteCountLimit");
                    int userLimit = 1;
                    if (userLimitObj instanceof Number) {
                        userLimit = ((Number) userLimitObj).intValue();
                    } else if (userLimitObj != null) {
                        try {
                            userLimit = Integer.parseInt(userLimitObj.toString());
                        } catch (NumberFormatException e) {
                            userLimit = 1;
                        }
                    }
                    
                    // 查询该用户的提交次数（从Response表查询）
                    LambdaQueryWrapper<Response> userWrapper = new LambdaQueryWrapper<>();
                    userWrapper.eq(Response::getSurveyId, surveyId)
                              .eq(Response::getUserId, userId)
                              .eq(Response::getStatus, "COMPLETED");
                    long userCount = responseMapper.selectCount(userWrapper);
                    
                    if (userCount >= userLimit) {
                        throw new RuntimeException("该用户已达到答题次数限制（" + userLimit + "次），无法继续提交");
                    }
                }
            }
        }
        
        // 保存表单数据
        FormData formData = new FormData();
        formData.setFormKey(formKey);
        formData.setOriginalData(originalData);
        formData.setSubmitRequestIp(ipAddress);
        
        // 计算序号
        LambdaQueryWrapper<FormData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormData::getFormKey, formKey);
        long count = this.count(wrapper);
        formData.setSerialNumber((int) (count + 1));
        
        this.save(formData);
        return formData;
    }
    
    @Override
    public Page<FormData> getFormDataList(Page<FormData> page, String formKey) {
        LambdaQueryWrapper<FormData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormData::getFormKey, formKey);
        wrapper.orderByDesc(FormData::getCreateTime);
        return this.page(page, wrapper);
    }
    
    @Override
    public FormData getFormDataById(Long id) {
        return this.getById(id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFormData(Long id) {
        return this.removeById(id);
    }
}

