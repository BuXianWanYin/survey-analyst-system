package com.server.surveyanalystserver.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.FormConfig;
import com.server.surveyanalystserver.entity.FormData;
import com.server.surveyanalystserver.entity.FormSetting;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.service.FileService;
import com.server.surveyanalystserver.service.FormDataService;
import com.server.surveyanalystserver.service.FormConfigService;
import com.server.surveyanalystserver.service.FormSettingService;
import com.server.surveyanalystserver.service.UserService;
import com.server.surveyanalystserver.utils.IpUtils;
import com.server.surveyanalystserver.utils.UserAgentUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表单数据控制器
 */
@RestController
@RequestMapping("/api/form/data")
@Api(tags = "表单数据")
public class FormDataController {
    
    @Autowired
    private FormDataService formDataService;
    
    @Autowired
    private FormConfigService formConfigService;
    
    @Autowired
    private FormSettingService formSettingService;
    
    @Autowired(required = false)
    private UserService userService;
    
    @Autowired
    private FileService fileService;
    
    @ApiOperation(value = "填写前校验", notes = "在开始填写问卷前进行校验（检查各种限制）")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/validate")
    public Result<Void> validateBeforeFill(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String formKey = (String) params.get("formKey");
        if (formKey == null || formKey.isEmpty()) {
            return Result.error("表单Key不能为空");
        }
        
        // 获取IP地址
        String ipAddress = IpUtils.getIpAddress(request);
        
        // 获取设备ID（从请求参数中获取，如果前端传递了deviceId）
        String deviceId = (String) params.get("deviceId");
        
        // 获取用户ID（系统不支持匿名提交，必须登录）
        Long userId = null;
        if (userService != null) {
            // 从SecurityContext获取当前登录用户
            User currentUser = userService.getCurrentUser();
            if (currentUser != null) {
                userId = currentUser.getId();
            } else {
                return Result.error("用户未登录，无法填写");
            }
        } else {
            return Result.error("用户服务未配置，无法填写");
        }
        
        // 执行校验
        try {
            formDataService.validateBeforeFill(formKey, ipAddress, deviceId, userId);
            return Result.success("校验通过，可以开始填写");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    @ApiOperation(value = "保存表单数据", notes = "保存表单填写数据（需要登录）")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping
    public Result<Map<String, Object>> saveFormData(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String formKey = (String) params.get("formKey");
        @SuppressWarnings("unchecked")
        Map<String, Object> originalData = (Map<String, Object>) params.get("originalData");
        
        // 处理 base64 图片数据，转换为文件并保存
        if (originalData != null) {
            originalData = processBase64Images(originalData);
        }
        
        // 获取开始填写时间（前端已格式化为 yyyy-MM-dd HH:mm:ss 格式）
        LocalDateTime startTime = null;
        Object startTimeObj = params.get("startTime");
        if (startTimeObj != null) {
            if (startTimeObj instanceof String) {
                String startTimeStr = (String) startTimeObj;
                try {
                    // 前端已格式化为 yyyy-MM-dd HH:mm:ss 格式，直接解析
                    startTime = LocalDateTime.parse(startTimeStr, 
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } catch (Exception e) {
                    // 解析失败，记录日志但不影响主流程
                    System.err.println("解析开始时间失败: " + startTimeStr + ", 错误: " + e.getMessage());
                    // 如果解析失败，startTime 保持为 null，后续代码会处理
                }
            }
        }
        
        // 获取IP地址
        String ipAddress = IpUtils.getIpAddress(request);
        
        // 获取 User-Agent
        String userAgent = request.getHeader("User-Agent");
        
        // 解析浏览器信息
        String browser = UserAgentUtils.getBrowser(userAgent);
        
        // 获取设备ID（从请求参数中获取，如果前端传递了deviceId）
        String deviceId = (String) params.get("deviceId");
        
        // 获取用户ID（系统不支持匿名提交，必须登录）
        Long userId = null;
        if (userService != null) {
            // 从SecurityContext获取当前登录用户
            User currentUser = userService.getCurrentUser();
            if (currentUser != null) {
                userId = currentUser.getId();
            } else {
                return Result.error("用户未登录，无法提交");
            }
        } else {
            return Result.error("用户服务未配置，无法提交");
        }
        
        // 保存表单数据（会进行限制验证）
        FormData saved = formDataService.saveFormData(formKey, originalData, ipAddress, deviceId, userId, startTime, browser, userAgent);
        
        // 获取提交设置信息，返回给前端
        Map<String, Object> result = new HashMap<>();
        result.put("formData", saved);
        
        // 获取表单配置，用于获取surveyId
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
        
        return Result.success("保存成功", result);
    }
    
    @ApiOperation(value = "分页查询表单数据", notes = "根据formKey分页查询表单数据")
    @GetMapping("/list")
    public Result<Page<FormData>> getFormDataList(
            @RequestParam String formKey,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<FormData> page = new Page<>(pageNum, pageSize);
        Page<FormData> result = formDataService.getFormDataList(page, formKey);
        return Result.success("查询成功", result);
    }
    
    @ApiOperation(value = "获取表单数据详情", notes = "根据ID获取表单数据详情")
    @GetMapping("/{id}")
    public Result<FormData> getFormDataById(@PathVariable Long id) {
        FormData formData = formDataService.getFormDataById(id);
        return Result.success("查询成功", formData);
    }
    
    @ApiOperation(value = "删除表单数据", notes = "删除指定表单数据")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> deleteFormData(@PathVariable Long id) {
        formDataService.deleteFormData(id);
        return Result.success("删除成功");
    }
    
    /**
     * 处理 base64 图片数据，转换为文件并保存
     * @param originalData 原始数据
     * @return 处理后的数据（base64 已替换为文件路径）
     */
    private Map<String, Object> processBase64Images(Map<String, Object> originalData) {
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
                        // 保留原始 base64 数据，或者设置为 null
                        // entry.setValue(null);
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
}

