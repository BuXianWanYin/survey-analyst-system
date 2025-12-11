package com.server.surveyanalystserver.controller;

import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.FormLogic;
import com.server.surveyanalystserver.service.FormLogicService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 表单逻辑控制器
 */
@RestController
@RequestMapping("/api/form/logic")
@Api(tags = "表单逻辑")
public class FormLogicController {
    
    @Autowired
    private FormLogicService formLogicService;
    
    @ApiOperation(value = "保存表单逻辑", notes = "保存或更新表单逻辑")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/{surveyId}")
    public Result<FormLogic> saveFormLogic(
            @PathVariable Long surveyId,
            @RequestBody List<Map<String, Object>> scheme) {
        FormLogic saved = formLogicService.saveFormLogic(surveyId, scheme);
        return Result.success("保存成功", saved);
    }
    
    @ApiOperation(value = "获取表单逻辑", notes = "根据问卷ID获取表单逻辑")
    @GetMapping("/{surveyId}")
    public Result<FormLogic> getFormLogic(@PathVariable Long surveyId) {
        FormLogic logic = formLogicService.getBySurveyId(surveyId);
        return Result.success("查询成功", logic);
    }
    
    @ApiOperation(value = "删除表单逻辑", notes = "删除指定问卷的表单逻辑")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @DeleteMapping("/{surveyId}")
    public Result<Void> deleteFormLogic(@PathVariable Long surveyId) {
        formLogicService.deleteBySurveyId(surveyId);
        return Result.success("删除成功");
    }
}

