package com.server.surveyanalystserver.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.FormData;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.service.FormDataService;
import com.server.surveyanalystserver.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
    private UserService userService;
    
    /**
     * 填写前校验接口
     * 在开始填写问卷前进行校验，检查各种限制条件（如填写次数限制、IP限制等）
     * @param params 参数Map，包含formKey（表单Key）和deviceId（设备ID）
     * @param request HTTP请求对象，用于获取IP地址等信息
     * @return 校验结果
     */
    @ApiOperation(value = "填写前校验", notes = "在开始填写问卷前进行校验（检查各种限制）")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/validate")
    public Result<Void> validateBeforeFill(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String formKey = (String) params.get("formKey");
        String deviceId = (String) params.get("deviceId");
        User currentUser = userService.getCurrentUser();
        
        formDataService.validateBeforeFill(formKey, request, deviceId, currentUser.getId());
        return Result.success("校验通过，可以开始填写");
    }
    
    /**
     * 保存表单填写数据
     * 保存用户填写的表单数据，处理base64图片，记录IP地址、设备信息等，返回提交设置
     * @param params 参数Map，包含formKey（表单Key）、originalData（原始数据）、startTime（开始时间）、deviceId（设备ID）
     * @param request HTTP请求对象，用于获取IP地址等信息
     * @return 保存结果，包含表单数据和提交设置
     */
    @ApiOperation(value = "保存表单数据", notes = "保存表单填写数据（需要登录）")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping
    public Result<Map<String, Object>> saveFormData(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String formKey = (String) params.get("formKey");
        @SuppressWarnings("unchecked")
        Map<String, Object> originalData = (Map<String, Object>) params.get("originalData");
        
        if (originalData != null) {
            originalData = formDataService.processBase64Images(originalData);
        }
        
        Object startTimeObj = params.get("startTime");
        String startTimeStr = startTimeObj != null ? startTimeObj.toString() : null;
        String deviceId = (String) params.get("deviceId");
        User currentUser = userService.getCurrentUser();
        
        Map<String, Object> result = formDataService.saveFormDataWithSettings(formKey, originalData, request, 
            deviceId, currentUser.getId(), startTimeStr);
        
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

