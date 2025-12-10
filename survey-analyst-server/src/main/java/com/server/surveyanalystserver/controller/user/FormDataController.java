package com.server.surveyanalystserver.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.FormData;
import com.server.surveyanalystserver.entity.FormSetting;
import com.server.surveyanalystserver.service.FormDataService;
import com.server.surveyanalystserver.service.FormConfigService;
import com.server.surveyanalystserver.service.FormSettingService;
import com.server.surveyanalystserver.service.user.UserService;
import com.server.surveyanalystserver.utils.IpUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
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
            com.server.surveyanalystserver.entity.User currentUser = userService.getCurrentUser();
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
        
        // 获取IP地址
        String ipAddress = IpUtils.getIpAddress(request);
        
        // 获取设备ID（从请求参数中获取，如果前端传递了deviceId）
        String deviceId = (String) params.get("deviceId");
        
        // 获取用户ID（系统不支持匿名提交，必须登录）
        Long userId = null;
        if (userService != null) {
            // 从SecurityContext获取当前登录用户
            com.server.surveyanalystserver.entity.User currentUser = userService.getCurrentUser();
            if (currentUser != null) {
                userId = currentUser.getId();
            } else {
                return Result.error("用户未登录，无法提交");
            }
        } else {
            return Result.error("用户服务未配置，无法提交");
        }
        
        // 保存表单数据（会进行限制验证）
        FormData saved = formDataService.saveFormData(formKey, originalData, ipAddress, deviceId, userId);
        
        // 获取提交设置信息，返回给前端
        Map<String, Object> result = new HashMap<>();
        result.put("formData", saved);
        
        // 获取表单配置，用于获取surveyId
        com.server.surveyanalystserver.entity.FormConfig formConfig = formConfigService.getByFormKey(formKey);
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
}

