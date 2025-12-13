package com.server.surveyanalystserver.controller;

import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.FormConfig;
import com.server.surveyanalystserver.entity.FormItem;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.service.FormItemService;
import com.server.surveyanalystserver.service.FormConfigService;
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
    
    /**
     * 保存表单配置
     * 保存或更新表单的基本配置信息，自动设置当前用户为创建者
     * @param config 表单配置对象
     * @return 保存成功后的表单配置
     */
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
    
    /**
     * 批量保存表单项
     * 批量保存表单项列表，如果form_config不存在会自动创建
     * @param data 数据Map，包含formKey（表单Key）、items（表单项列表）、surveyId（问卷ID，可选）
     * @return 保存结果
     */
    @ApiOperation(value = "批量保存表单项", notes = "批量保存表单项列表，如果 form_config 不存在会自动创建")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/item/batch")
    public Result<Void> batchSaveFormItems(@RequestBody Map<String, Object> data) {
        String formKey = (String) data.get("formKey");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) data.get("items");
        Long surveyId = data.get("surveyId") != null ? 
            ((Number) data.get("surveyId")).longValue() : null;
        User currentUser = userService.getCurrentUser();
        formItemService.batchSaveFormItemsWithConfig(formKey, itemsData, surveyId, currentUser.getId());
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

