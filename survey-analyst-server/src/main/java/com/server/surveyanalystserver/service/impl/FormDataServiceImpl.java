package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.FormConfig;
import com.server.surveyanalystserver.entity.FormData;
import com.server.surveyanalystserver.entity.FormSetting;
import com.server.surveyanalystserver.entity.Response;
import com.server.surveyanalystserver.entity.Survey;
import com.server.surveyanalystserver.mapper.FormDataMapper;
import com.server.surveyanalystserver.mapper.ResponseMapper;
import com.server.surveyanalystserver.mapper.SurveyMapper;
import com.server.surveyanalystserver.service.FormConfigService;
import com.server.surveyanalystserver.service.FormDataService;
import com.server.surveyanalystserver.service.FormSettingService;
import com.server.surveyanalystserver.utils.UserAgentUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
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
    
    @Autowired
    private SurveyMapper surveyMapper;
    
    @Override
    public void validateBeforeFill(String formKey, String ipAddress, String deviceId, Long userId) {
        // 获取表单配置，用于获取surveyId
        FormConfig formConfig = formConfigService.getByFormKey(formKey);
        if (formConfig == null) {
            throw new RuntimeException("表单配置不存在");
        }
        
        Long surveyId = formConfig.getSurveyId();
        
        // 检查问卷是否存在（直接使用Mapper避免循环依赖）
        Survey survey = surveyMapper.selectById(surveyId);
        if (survey == null) {
            throw new RuntimeException("问卷不存在");
        }
        
        // 检查问卷状态
        if (!"PUBLISHED".equals(survey.getStatus())) {
            throw new RuntimeException("问卷尚未发布，无法填写");
        }
        
        // 检查时间限制
        LocalDateTime now = LocalDateTime.now();
        if (survey.getStartTime() != null && now.isBefore(survey.getStartTime())) {
            throw new RuntimeException("问卷尚未开始，无法填写");
        }
        if (survey.getEndTime() != null && now.isAfter(survey.getEndTime())) {
            throw new RuntimeException("问卷已结束，无法填写");
        }
        
        // 检查最大填写数
        if (survey.getMaxResponses() != null && survey.getMaxResponses() > 0) {
            LambdaQueryWrapper<Response> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(Response::getSurveyId, surveyId)
                       .eq(Response::getStatus, "COMPLETED");
            long currentCount = responseMapper.selectCount(countWrapper);
            if (currentCount >= survey.getMaxResponses()) {
                throw new RuntimeException("已超出该问卷的填写数量（" + survey.getMaxResponses() + "份），无法继续填写");
            }
        }
        
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
                        throw new RuntimeException("该IP地址已达到答题次数限制（" + ipLimit + "次），无法继续填写");
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
                    
                    // 查询该设备的提交次数（从FormData表查询）
                    LambdaQueryWrapper<FormData> deviceWrapper = new LambdaQueryWrapper<>();
                    deviceWrapper.eq(FormData::getFormKey, formKey);
                    long deviceCount = this.count(deviceWrapper);
                    
                    if (deviceCount >= deviceLimit) {
                        throw new RuntimeException("该设备已达到答题次数限制（" + deviceLimit + "次），无法继续填写");
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
                        throw new RuntimeException("您已达到答题次数限制（" + userLimit + "次），无法继续填写");
                    }
                }
            } else {
                throw new RuntimeException("用户未登录，无法填写");
            }
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FormData saveFormData(String formKey, Map<String, Object> originalData, String ipAddress, String deviceId, Long userId, LocalDateTime startTime, String browser, String userAgent) {
        // 在保存前再次校验（双重校验，确保数据一致性）
        validateBeforeFill(formKey, ipAddress, deviceId, userId);
        
        // 获取表单配置，用于获取surveyId
        FormConfig formConfig = formConfigService.getByFormKey(formKey);
        if (formConfig == null) {
            throw new RuntimeException("表单配置不存在");
        }
        Long surveyId = formConfig.getSurveyId();
        
        // 计算提交时间
        LocalDateTime submitTime = LocalDateTime.now();
        
        // 先保存到 response 表
        Response response = new Response();
        response.setSurveyId(surveyId);
        response.setUserId(userId);
        response.setIpAddress(ipAddress);
        response.setStartTime(startTime);
        response.setSubmitTime(submitTime);
        response.setStatus("COMPLETED");
        
        // 设置设备类型（优先使用User-Agent判断，更准确）
        String deviceType = UserAgentUtils.getDeviceType(userAgent);
        response.setDeviceType(deviceType);
        
        // 计算填写时长（秒）
        if (startTime != null) {
            long durationSeconds = Duration.between(startTime, submitTime).getSeconds();
            response.setDuration((int) durationSeconds);
        }
        
        // 保存到 response 表
        responseMapper.insert(response);
        
        // 保存表单数据到 form_data 表
        FormData formData = new FormData();
        formData.setFormKey(formKey);
        formData.setOriginalData(originalData);
        formData.setSubmitRequestIp(ipAddress);
        formData.setStartTime(startTime);
        formData.setSubmitBrowser(browser);
        
        // 计算填写时长（秒）
        if (startTime != null) {
            long durationSeconds = Duration.between(startTime, submitTime).getSeconds();
            formData.setCompleteTime((int) durationSeconds);
        }
        
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

