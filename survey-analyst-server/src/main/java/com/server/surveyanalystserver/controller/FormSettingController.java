package com.server.surveyanalystserver.controller;

import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.FormSetting;
import com.server.surveyanalystserver.service.FormSettingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 表单设置控制器
 */
@RestController
@RequestMapping("/api/form/setting")
@Api(tags = "表单设置")
public class FormSettingController {
    
    @Autowired
    private FormSettingService formSettingService;
    
    /**
     * 保存表单设置
     * 保存或更新表单的设置信息，包括提交设置、填写限制等
     * @param surveyId 问卷ID
     * @param settings 设置信息Map，包含各种表单设置项
     * @return 保存成功后的表单设置对象
     */
    @ApiOperation(value = "保存表单设置", notes = "保存或更新表单设置")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/{surveyId}")
    public Result<FormSetting> saveFormSetting(
            @PathVariable Long surveyId,
            @RequestBody Map<String, Object> settings) {
        FormSetting saved = formSettingService.saveFormSetting(surveyId, settings);
        return Result.success("保存成功", saved);
    }
    
    /**
     * 根据问卷ID获取表单设置
     * 查询指定问卷的表单设置信息
     * @param surveyId 问卷ID
     * @return 表单设置对象，如果不存在则返回null
     */
    @ApiOperation(value = "获取表单设置", notes = "根据问卷ID获取表单设置")
    @GetMapping("/{surveyId}")
    public Result<FormSetting> getFormSetting(@PathVariable Long surveyId) {
        FormSetting setting = formSettingService.getBySurveyId(surveyId);
        return Result.success("查询成功", setting);
    }
    
    @ApiOperation(value = "删除表单设置", notes = "删除指定问卷的表单设置")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @DeleteMapping("/{surveyId}")
    public Result<Void> deleteFormSetting(@PathVariable Long surveyId) {
        formSettingService.deleteBySurveyId(surveyId);
        return Result.success("删除成功");
    }
}

