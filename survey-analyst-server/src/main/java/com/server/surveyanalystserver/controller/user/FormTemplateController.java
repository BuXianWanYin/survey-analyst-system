package com.server.surveyanalystserver.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.FormTemplate;
import com.server.surveyanalystserver.entity.FormTemplateCategory;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.service.FormTemplateCategoryService;
import com.server.surveyanalystserver.service.FormTemplateService;
import com.server.surveyanalystserver.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
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
        Page<FormTemplate> page = new Page<>(current, size);
        LambdaQueryWrapper<FormTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormTemplate::getIsDeleted, 0);
        
        // 如果指定了模板类型，则按类型筛选；否则查询所有类型
        if (isPublic != null) {
            wrapper.eq(FormTemplate::getIsPublic, isPublic);
        }
        
        // 如果是查询我的模板，需要过滤当前用户的模板
        if (isPublic != null && isPublic == 0) {
            User currentUser = userService.getCurrentUser();
            if (currentUser != null) {
                wrapper.eq(FormTemplate::getUserId, currentUser.getId());
            }
        }
        
        if (StringUtils.hasText(name)) {
            wrapper.like(FormTemplate::getName, name);
        }
        
        if (type != null) {
            wrapper.eq(FormTemplate::getCategoryId, type);
        }
        
        wrapper.orderByDesc(FormTemplate::getCreateTime);
        formTemplateService.page(page, wrapper);
        return Result.success("查询成功", page);
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

    @ApiOperation(value = "删除模板", notes = "根据 formKey 删除模板（只能删除我的模板，不能删除公共模板）")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/delete")
    public Result<String> deleteTemplate(@RequestBody FormTemplate template) {
        FormTemplate existing = formTemplateService.getByFormKey(template.getFormKey());
        if (existing == null) {
            return Result.error("模板不存在");
        }
        
        // 公共模板不允许删除
        if (existing.getIsPublic() != null && existing.getIsPublic() == 1) {
            return Result.error("公共模板不允许删除");
        }
        
        // 检查权限：只能删除自己创建的模板
        User currentUser = userService.getCurrentUser();
        if (!existing.getUserId().equals(currentUser.getId()) && !currentUser.getRole().equals("ROLE_ADMIN")) {
            return Result.error("无权删除此模板");
        }
        
        // 逻辑删除
        existing.setIsDeleted(1);
        formTemplateService.getBaseMapper().updateById(existing);
        
        return Result.success("删除成功", template.getFormKey());
    }

    @ApiOperation(value = "创建用户自定义分类", notes = "创建用户自己的模板分类")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/category/create")
    public Result<FormTemplateCategory> createCategory(@RequestBody FormTemplateCategory category) {
        User currentUser = userService.getCurrentUser();
        category.setUserId(currentUser.getId());
        
        // 设置默认排序号
        if (category.getSort() == null) {
            category.setSort(0);
        }
        
        formTemplateCategoryService.save(category);
        return Result.success("创建成功", category);
    }

    @ApiOperation(value = "更新分类", notes = "更新分类。普通用户只能更新自己的分类，管理员可以更新所有分类（包括系统分类）。")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/category/update")
    public Result<String> updateCategory(@RequestBody FormTemplateCategory category) {
        FormTemplateCategory existing = formTemplateCategoryService.getById(category.getId());
        if (existing == null) {
            return Result.error("分类不存在");
        }
        
        User currentUser = userService.getCurrentUser();
        
        // 如果是系统分类，只有管理员可以更新
        if (existing.getUserId() == null && !"ADMIN".equals(currentUser.getRole())) {
            return Result.error("系统分类只能由管理员修改");
        }
        
        // 如果是用户分类，只能更新自己的分类（管理员可以更新所有）
        if (existing.getUserId() != null && !existing.getUserId().equals(currentUser.getId()) 
            && !"ADMIN".equals(currentUser.getRole())) {
            return Result.error("无权修改此分类");
        }
        
        existing.setName(category.getName());
        if (category.getSort() != null) {
            existing.setSort(category.getSort());
        }
        formTemplateCategoryService.updateById(existing);
        
        return Result.success("更新成功", category.getId().toString());
    }

    @ApiOperation(value = "删除分类", notes = "删除分类（会同时删除该分类下的所有模板及其相关数据）。普通用户只能删除自己的分类，管理员可以删除所有分类。")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/category/delete")
    public Result<String> deleteCategory(@RequestBody FormTemplateCategory category) {
        FormTemplateCategory existing = formTemplateCategoryService.getById(category.getId());
        if (existing == null) {
            return Result.error("分类不存在");
        }
        
        User currentUser = userService.getCurrentUser();
        
        // 权限检查：普通用户只能删除自己的分类，管理员可以删除所有分类
        if (existing.getUserId() != null && !existing.getUserId().equals(currentUser.getId()) 
            && !"ADMIN".equals(currentUser.getRole())) {
            return Result.error("无权删除此分类");
        }
        
        // 如果是系统分类，只有管理员可以删除
        if (existing.getUserId() == null && !"ADMIN".equals(currentUser.getRole())) {
            return Result.error("系统分类只能由管理员删除");
        }
        
        // 删除该分类下的所有模板及其相关数据
        formTemplateCategoryService.deleteCategoryWithTemplates(existing.getId());
        
        return Result.success("删除成功", category.getId().toString());
    }
}

