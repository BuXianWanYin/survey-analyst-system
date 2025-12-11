package com.server.surveyanalystserver.controller;

import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.FormConfig;
import com.server.surveyanalystserver.entity.FormItem;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.service.FormConfigService;
import com.server.surveyanalystserver.service.FormItemService;
import com.server.surveyanalystserver.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 表单设计控制器
 */
@RestController
@RequestMapping("/api/form")
@Api(tags = "表单设计")
public class FormController {
    
    @Autowired
    private FormConfigService formConfigService;
    
    @Autowired
    private FormItemService formItemService;
    
    @Autowired
    private UserService userService;
    
    @ApiOperation(value = "保存表单配置", notes = "保存或更新表单配置")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/config")
    public Result<FormConfig> saveFormConfig(@RequestBody FormConfig config) {
        // 从 SecurityContext 获取当前用户ID
        User currentUser = userService.getCurrentUser();
        config.setUserId(currentUser.getId());
        
        FormConfig saved = formConfigService.saveFormConfig(config);
        return Result.success("保存成功", saved);
    }
    
    @ApiOperation(value = "获取表单配置", notes = "根据问卷ID获取表单配置")
    @GetMapping("/config/{surveyId}")
    public Result<FormConfig> getFormConfig(@PathVariable Long surveyId) {
        FormConfig config = formConfigService.getBySurveyId(surveyId);
        return Result.success("查询成功", config);
    }
    
    @ApiOperation(value = "批量保存表单项", notes = "批量保存表单项列表，如果 form_config 不存在会自动创建")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/item/batch")
    public Result<Void> batchSaveFormItems(@RequestBody Map<String, Object> data) {
        String formKey = (String) data.get("formKey");
        if (formKey == null || formKey.isEmpty()) {
            return Result.error("formKey 不能为空");
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) data.get("items");
        Long surveyId = data.get("surveyId") != null ? 
            ((Number) data.get("surveyId")).longValue() : null;
        
        // 确保 form_config 存在
        FormConfig existingConfig = formConfigService.getByFormKey(formKey);
        if (existingConfig == null) {
            // 如果不存在，创建一个基本的 form_config
            FormConfig newConfig = new FormConfig();
            newConfig.setFormKey(formKey);
            newConfig.setSurveyId(surveyId);
            User currentUser = userService.getCurrentUser();
            newConfig.setUserId(currentUser.getId());
            newConfig.setName("未命名表单");
            newConfig.setDescription("");
            newConfig.setStatus(1);
            newConfig.setIsDeleted(0);
            formConfigService.saveFormConfig(newConfig);
        } else if (surveyId != null && existingConfig.getSurveyId() == null) {
            // 如果 form_config 存在但没有 surveyId，而前端传入了 surveyId，则更新
            existingConfig.setSurveyId(surveyId);
            formConfigService.saveFormConfig(existingConfig);
        }
        
        formItemService.batchSaveFromMap(formKey, itemsData);
        return Result.success("保存成功");
    }
    
    @ApiOperation(value = "获取表单项列表", notes = "根据 formKey 获取表单项列表")
    @GetMapping("/item/list")
    public Result<List<FormItem>> getFormItems(@RequestParam String key) {
        List<FormItem> items = formItemService.getByFormKey(key);
        return Result.success("查询成功", items);
    }
    
    @ApiOperation(value = "删除表单项", notes = "删除指定表单项")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @DeleteMapping("/item/{id}")
    public Result<Void> deleteFormItem(@PathVariable Long id) {
        formItemService.deleteById(id);
        return Result.success("删除成功");
    }
}

