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
import com.server.surveyanalystserver.service.FileService;
import com.server.surveyanalystserver.service.FormConfigService;
import com.server.surveyanalystserver.service.FormDataService;
import com.server.surveyanalystserver.service.FormSettingService;
import com.server.surveyanalystserver.utils.IpUtils;
import com.server.surveyanalystserver.utils.UserAgentUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    
    @Autowired
    private FileService fileService;
    
    @Override
    public void validateBeforeFill(String formKey, HttpServletRequest request, String deviceId, Long userId) {
        String ipAddress = IpUtils.getIpAddress(request);
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
        // 注意：这个方法保留原有签名，供内部调用
        // 在保存前再次校验（双重校验，确保数据一致性）
        // 由于validateBeforeFill需要HttpServletRequest，这里跳过校验
        // 实际校验已在Controller层调用validateBeforeFill时完成
        
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
    public FormData getFormDataByResponseId(Long responseId) {
        // 1. 通过 Response ID 获取 Response 信息
        Response response = responseMapper.selectById(responseId);
        if (response == null || response.getSurveyId() == null) {
            return null;
        }
        
        // 2. 通过 surveyId 获取 formKey
        FormConfig formConfig = formConfigService.getBySurveyId(response.getSurveyId());
        if (formConfig == null || formConfig.getFormKey() == null) {
            return null;
        }
        
        // 3. 通过 formKey 和提交时间匹配 FormData
        // 优先使用 submitTime，如果没有则使用 createTime
        LocalDateTime matchTime = response.getSubmitTime() != null 
            ? response.getSubmitTime() 
            : response.getCreateTime();
        
        if (matchTime == null) {
            return null;
        }
        
        // 查询匹配的 FormData（通过 formKey 和创建时间，允许一定的时间误差）
        LambdaQueryWrapper<FormData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormData::getFormKey, formConfig.getFormKey());
        
        // 如果 Response 有 startTime，也尝试匹配
        if (response.getStartTime() != null) {
            wrapper.and(w -> w
                .eq(FormData::getStartTime, response.getStartTime())
                .or()
                .eq(FormData::getCreateTime, matchTime)
            );
        } else {
            wrapper.eq(FormData::getCreateTime, matchTime);
        }
        
        // 如果 Response 有 IP 地址，也尝试匹配
        if (response.getIpAddress() != null) {
            wrapper.or(w -> w
                .eq(FormData::getFormKey, formConfig.getFormKey())
                .eq(FormData::getSubmitRequestIp, response.getIpAddress())
                .eq(FormData::getCreateTime, matchTime)
            );
        }
        
        wrapper.orderByDesc(FormData::getCreateTime);
        wrapper.last("LIMIT 1");
        
        return this.getOne(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFormData(Long id) {
        return this.removeById(id);
    }
    
    @Override
    public Map<String, Object> processBase64Images(Map<String, Object> originalData) {
        if (originalData == null) {
            return null;
        }
        
        Map<String, Object> processedData = new HashMap<>(originalData);
        
        for (Map.Entry<String, Object> entry : processedData.entrySet()) {
            Object value = entry.getValue();
            
            if (value instanceof String) {
                // 检查是否是 base64 图片数据
                String strValue = (String) value;
                if (strValue.startsWith("data:image/")) {
                    try {
                        // 提取图片格式（png, jpeg, jpg 等）
                        String mimeType = strValue.substring(5, strValue.indexOf(";"));
                        String extension = getExtensionFromMimeType(mimeType);
                        
                        // 保存 base64 图片为文件
                        String fileUrl = fileService.saveBase64Image(strValue, extension);
                        
                        // 用文件路径替换 base64 数据
                        entry.setValue(fileUrl);
                    } catch (Exception e) {
                        // 如果保存失败，记录日志但不影响主流程
                        System.err.println("保存base64图片失败: " + e.getMessage());
                        // 保留原始 base64 数据
                    }
                }
            } else if (value instanceof List) {
                // 处理 List 中的 base64 数据
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) value;
                List<Object> processedList = new ArrayList<>();
                
                for (Object item : list) {
                    if (item instanceof String) {
                        String strItem = (String) item;
                        if (strItem.startsWith("data:image/")) {
                            try {
                                // 提取图片格式
                                String mimeType = strItem.substring(5, strItem.indexOf(";"));
                                String extension = getExtensionFromMimeType(mimeType);
                                
                                // 保存 base64 图片为文件
                                String fileUrl = fileService.saveBase64Image(strItem, extension);
                                processedList.add(fileUrl);
                            } catch (Exception e) {
                                System.err.println("保存base64图片失败: " + e.getMessage());
                                processedList.add(item); // 保留原始数据
                            }
                        } else {
                            processedList.add(item);
                        }
                    } else {
                        processedList.add(item);
                    }
                }
                entry.setValue(processedList);
            }
        }
        
        return processedData;
    }
    
    /**
     * 根据 MIME 类型获取文件扩展名
     * @param mimeType MIME 类型（如 image/png, image/jpeg）
     * @return 文件扩展名（如 .png, .jpg）
     */
    private String getExtensionFromMimeType(String mimeType) {
        if (mimeType == null) {
            return ".png";
        }
        
        switch (mimeType.toLowerCase()) {
            case "image/png":
                return ".png";
            case "image/jpeg":
            case "image/jpg":
                return ".jpg";
            case "image/gif":
                return ".gif";
            case "image/webp":
                return ".webp";
            case "image/bmp":
                return ".bmp";
            default:
                return ".png"; // 默认使用 png
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> saveFormDataWithSettings(String formKey, Map<String, Object> originalData, 
            HttpServletRequest request, String deviceId, Long userId, LocalDateTime startTime) {
        // 获取IP地址、User-Agent和浏览器信息
        String ipAddress = IpUtils.getIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String browser = UserAgentUtils.getBrowser(userAgent);
        
        // 保存表单数据
        FormData saved = saveFormData(formKey, originalData, ipAddress, deviceId, userId, startTime, browser, userAgent);
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("formData", saved);
        
        // 获取提交设置信息
        FormConfig formConfig = formConfigService.getByFormKey(formKey);
        if (formConfig != null) {
            FormSetting formSetting = formSettingService.getBySurveyId(formConfig.getSurveyId());
            if (formSetting != null && formSetting.getSettings() != null) {
                Map<String, Object> settings = formSetting.getSettings();
                Map<String, Object> submitSettings = new HashMap<>();
                
                // 提取提交相关设置
                if (settings.containsKey("submitShowType")) {
                    submitSettings.put("submitShowType", settings.get("submitShowType"));
                }
                if (settings.containsKey("submitShowCustomPageContent")) {
                    submitSettings.put("submitShowCustomPageContent", settings.get("submitShowCustomPageContent"));
                }
                if (settings.containsKey("submitJump")) {
                    submitSettings.put("submitJump", settings.get("submitJump"));
                }
                if (settings.containsKey("submitJumpUrl")) {
                    submitSettings.put("submitJumpUrl", settings.get("submitJumpUrl"));
                }
                
                result.put("submitSettings", submitSettings);
            }
        }
        
        return result;
    }
}

