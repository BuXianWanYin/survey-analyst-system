package com.server.surveyanalystserver.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.FormTemplate;
import com.server.surveyanalystserver.entity.FormTemplateCategory;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.service.FormTemplateCategoryService;
import com.server.surveyanalystserver.service.FormTemplateService;
import com.server.surveyanalystserver.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 表单模板控制器
 */
@RestController
@RequestMapping("/api/form/template")
@Api(tags = "表单模板")
public class FormTemplateController {

    @Autowired
    private FormTemplateService formTemplateService;

    @Autowired
    private FormTemplateCategoryService formTemplateCategoryService;

    @Autowired
    private UserService userService;

    /**
     * 获取模板分类列表
     * 获取系统分类和当前用户的自定义分类，按排序号降序排列
     * @return 模板分类列表
     */
    @ApiOperation(value = "获取模板分类列表", notes = "获取系统分类和当前用户的自定义分类，按排序号降序")
    @GetMapping("/type/list")
    public Result<List<FormTemplateCategory>> getTemplateTypeList() {
        User currentUser = userService.getCurrentUser();
        List<FormTemplateCategory> list = formTemplateCategoryService.listByUserId(currentUser.getId());
        return Result.success("查询成功", list);
    }

    @ApiOperation(value = "分页查询模板列表", notes = "支持按名称模糊查询、分类筛选和模板类型筛选")
    @GetMapping("/page")
    public Result<Page<FormTemplate>> getTemplatePage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long type,
            @RequestParam(required = false) Integer isPublic) {
        User currentUser = userService.getCurrentUser();
        Page<FormTemplate> page = new Page<>(current, size);
        Page<FormTemplate> result = formTemplateService.getTemplatePage(page, name, type, isPublic, 
            currentUser != null ? currentUser.getId() : null);
        return Result.success("查询成功", result);
    }

    @ApiOperation(value = "获取模板详情", notes = "根据模板 formKey 获取模板详情（包含 scheme）")
    @GetMapping("/details/{key}")
    public Result<FormTemplate.Definition> getTemplateDetails(@PathVariable String key) {
        FormTemplate template = formTemplateService.getByFormKey(key);
        if (template == null) {
            return Result.error("模板不存在");
        }
        return Result.success("查询成功", template.getScheme());
    }

    /**
     * 将表单保存为模板
     * 将当前表单保存为我的模板（isPublic=0），用于后续复用
     * @param template 模板信息对象，必须包含scheme（表单定义）
     * @return 创建成功后的模板formKey
     */
    @ApiOperation(value = "保存为模板", notes = "将表单保存为模板（保存到我的模板）")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/create")
    public Result<String> createTemplate(@RequestBody FormTemplate template) {
        // 从 SecurityContext 获取当前用户ID
        User currentUser = userService.getCurrentUser();
        template.setUserId(currentUser.getId());
        // 保存为我的模板（isPublic=0）
        template.setIsPublic(0);
        
        FormTemplate created = formTemplateService.createFormTemplate(template);
        return Result.success("保存成功", created.getFormKey());
    }

    /**
     * 更新我的模板信息
     * 更新模板信息，只能更新自己创建的模板（包含权限检查）
     * @param template 包含更新信息的模板对象
     * @return 更新成功后的模板formKey
     */
    @ApiOperation(value = "更新模板", notes = "更新我的模板信息（只能更新自己创建的模板）")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/update")
    public Result<String> updateTemplate(@RequestBody FormTemplate template) {
        User currentUser = userService.getCurrentUser();
        String formKey = formTemplateService.updateTemplate(template, currentUser.getId());
        return Result.success("更新成功", formKey);
    }

    @ApiOperation(value = "删除模板", notes = "根据 formKey 删除模板（只能删除我的模板，不能删除公共模板）")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/delete")
    public Result<String> deleteTemplate(@RequestBody FormTemplate template) {
        User currentUser = userService.getCurrentUser();
        String formKey = formTemplateService.deleteTemplate(template.getFormKey(), currentUser.getId());
        return Result.success("删除成功", formKey);
    }

    @ApiOperation(value = "创建用户自定义分类", notes = "创建用户自己的模板分类")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/category/create")
    public Result<FormTemplateCategory> createCategory(@RequestBody FormTemplateCategory category) {
        User currentUser = userService.getCurrentUser();
        FormTemplateCategory created = formTemplateCategoryService.createCategory(category, currentUser.getId());
        return Result.success("创建成功", created);
    }

    @ApiOperation(value = "更新分类", notes = "更新分类。普通用户只能更新自己的分类，管理员可以更新所有分类（包括系统分类）。")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/category/update")
    public Result<String> updateCategory(@RequestBody FormTemplateCategory category) {
        User currentUser = userService.getCurrentUser();
        Long categoryId = formTemplateCategoryService.updateCategory(category, currentUser.getId());
        return Result.success("更新成功", categoryId.toString());
    }

    @ApiOperation(value = "删除分类", notes = "删除分类（会同时删除该分类下的所有模板及其相关数据）。普通用户只能删除自己的分类，管理员可以删除所有分类。")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/category/delete")
    public Result<String> deleteCategory(@RequestBody FormTemplateCategory category) {
        User currentUser = userService.getCurrentUser();
        Long categoryId = formTemplateCategoryService.deleteCategory(category.getId(), currentUser.getId());
        return Result.success("删除成功", categoryId.toString());
    }
}

