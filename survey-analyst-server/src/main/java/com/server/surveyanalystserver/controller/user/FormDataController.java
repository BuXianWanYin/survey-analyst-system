package com.server.surveyanalystserver.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.FormData;
import com.server.surveyanalystserver.service.FormDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    
    @ApiOperation(value = "保存表单数据", notes = "保存表单填写数据")
    @PostMapping
    public Result<FormData> saveFormData(@RequestBody Map<String, Object> params) {
        String formKey = (String) params.get("formKey");
        @SuppressWarnings("unchecked")
        Map<String, Object> originalData = (Map<String, Object>) params.get("originalData");
        
        FormData saved = formDataService.saveFormData(formKey, originalData);
        return Result.success("保存成功", saved);
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

