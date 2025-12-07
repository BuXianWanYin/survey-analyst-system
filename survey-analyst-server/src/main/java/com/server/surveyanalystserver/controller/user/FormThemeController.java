package com.server.surveyanalystserver.controller.user;

import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.FormTheme;
import com.server.surveyanalystserver.service.FormThemeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 表单主题控制器
 */
@RestController
@RequestMapping("/api/form/theme")
@Api(tags = "表单主题")
public class FormThemeController {
    
    @Autowired
    private FormThemeService formThemeService;
    
    @ApiOperation(value = "保存表单主题", notes = "保存或更新表单主题")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping
    public Result<FormTheme> saveFormTheme(@RequestBody FormTheme theme) {
        FormTheme saved = formThemeService.saveFormTheme(theme);
        return Result.success("保存成功", saved);
    }
    
    @ApiOperation(value = "获取表单主题", notes = "根据问卷ID获取表单主题")
    @GetMapping("/{surveyId}")
    public Result<FormTheme> getFormTheme(@PathVariable Long surveyId) {
        FormTheme theme = formThemeService.getBySurveyId(surveyId);
        return Result.success("查询成功", theme);
    }
    
    @ApiOperation(value = "删除表单主题", notes = "删除指定问卷的表单主题")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @DeleteMapping("/{surveyId}")
    public Result<Void> deleteFormTheme(@PathVariable Long surveyId) {
        formThemeService.deleteBySurveyId(surveyId);
        return Result.success("删除成功");
    }
}

