package com.server.surveyanalystserver.controller.user;

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
import java.util.stream.Collectors;

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
    
    @ApiOperation(value = "批量保存表单项", notes = "批量保存表单项列表")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/item/batch")
    public Result<Void> batchSaveFormItems(@RequestBody Map<String, Object> data) {
        String formKey = (String) data.get("formKey");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) data.get("items");
        
        List<FormItem> items = itemsData.stream().map(itemData -> {
            FormItem item = new FormItem();
            item.setFormKey(formKey);
            item.setFormItemId((String) itemData.get("formItemId"));
            item.setType((String) itemData.get("type"));
            item.setLabel((String) itemData.get("label"));
            item.setRequired((Integer) itemData.get("required"));
            item.setPlaceholder((String) itemData.get("placeholder"));
            item.setSort(((Number) itemData.get("sort")).longValue());
            item.setScheme((String) itemData.get("scheme"));
            
            // 设置 span（栅格宽度）
            if (itemData.get("span") != null) {
                Object spanObj = itemData.get("span");
                if (spanObj instanceof Number) {
                    item.setSpan(((Number) spanObj).intValue());
                } else if (spanObj instanceof String) {
                    try {
                        item.setSpan(Integer.parseInt((String) spanObj));
                    } catch (NumberFormatException e) {
                        item.setSpan(24); // 默认值
                    }
                }
            } else {
                item.setSpan(24); // 默认值
            }
            
            // 设置 regList（正则验证规则）
            if (itemData.get("regList") != null) {
                Object regListObj = itemData.get("regList");
                if (regListObj instanceof String) {
                    item.setRegList((String) regListObj);
                } else {
                    // 如果是对象，转换为JSON字符串
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        item.setRegList(objectMapper.writeValueAsString(regListObj));
                    } catch (Exception e) {
                        // 忽略转换错误
                    }
                }
            }
            
            // 设置 isHideType（隐藏类型）
            if (itemData.get("isHideType") != null) {
                Object hideTypeObj = itemData.get("isHideType");
                if (hideTypeObj instanceof Number) {
                    item.setIsHideType(((Number) hideTypeObj).intValue());
                } else if (hideTypeObj instanceof Boolean) {
                    item.setIsHideType((Boolean) hideTypeObj ? 1 : 0);
                } else {
                    item.setIsHideType(0);
                }
            } else {
                item.setIsHideType(0);
            }
            
            return item;
        }).collect(Collectors.toList());
        
        formItemService.batchSave(formKey, items);
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

