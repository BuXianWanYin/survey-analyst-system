package com.server.surveyanalystserver.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.FormTemplate;
import com.server.surveyanalystserver.service.FormTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员公共模板管理控制器
 */
@RestController
@RequestMapping("/api/admin/template")
@Api(tags = "管理员-公共模板管理")
public class AdminTemplateController {

    @Autowired
    private FormTemplateService formTemplateService;

    @ApiOperation(value = "分页查询公共模板列表", notes = "管理员查询所有公共模板")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/page")
    public Result<Page<FormTemplate>> getPublicTemplatePage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId) {
        Page<FormTemplate> page = new Page<>(current, size);
        LambdaQueryWrapper<FormTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormTemplate::getIsDeleted, 0);
        wrapper.eq(FormTemplate::getIsPublic, 1); // 只查询公共模板
        
        if (StringUtils.hasText(name)) {
            wrapper.like(FormTemplate::getName, name);
        }
        
        if (categoryId != null) {
            wrapper.eq(FormTemplate::getCategoryId, categoryId);
        }
        
        wrapper.orderByDesc(FormTemplate::getCreateTime);
        formTemplateService.page(page, wrapper);
        return Result.success("查询成功", page);
    }

    @ApiOperation(value = "获取公共模板详情", notes = "根据模板 formKey 获取公共模板详情")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/details/{key}")
    public Result<FormTemplate.Definition> getPublicTemplateDetails(@PathVariable String key) {
        FormTemplate template = formTemplateService.getByFormKey(key);
        if (template == null) {
            return Result.error("模板不存在");
        }
        if (template.getIsPublic() == null || template.getIsPublic() != 1) {
            return Result.error("该模板不是公共模板");
        }
        return Result.success("查询成功", template.getScheme());
    }

    @ApiOperation(value = "创建公共模板", notes = "管理员创建公共模板")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public Result<String> createPublicTemplate(@RequestBody FormTemplate template) {
        // 设置为公共模板
        template.setIsPublic(1);
        FormTemplate created = formTemplateService.createFormTemplate(template);
        return Result.success("创建成功", created.getFormKey());
    }

    @ApiOperation(value = "更新公共模板", notes = "管理员更新公共模板信息")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/update")
    public Result<String> updatePublicTemplate(@RequestBody FormTemplate template) {
        FormTemplate existing = formTemplateService.getByFormKey(template.getFormKey());
        if (existing == null) {
            return Result.error("模板不存在");
        }
        if (existing.getIsPublic() == null || existing.getIsPublic() != 1) {
            return Result.error("该模板不是公共模板");
        }
        
        // 更新模板信息
        if (StringUtils.hasText(template.getName())) {
            existing.setName(template.getName());
        }
        if (StringUtils.hasText(template.getDescription())) {
            existing.setDescription(template.getDescription());
        }
        if (template.getCategoryId() != null) {
            existing.setCategoryId(template.getCategoryId());
        }
        if (StringUtils.hasText(template.getCoverImg())) {
            existing.setCoverImg(template.getCoverImg());
        }
        if (template.getStatus() != null) {
            existing.setStatus(template.getStatus());
        }
        
        formTemplateService.getBaseMapper().updateById(existing);
        return Result.success("更新成功", template.getFormKey());
    }

    @ApiOperation(value = "删除公共模板", notes = "管理员删除公共模板（逻辑删除）")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete")
    public Result<String> deletePublicTemplate(@RequestBody FormTemplate template) {
        FormTemplate existing = formTemplateService.getByFormKey(template.getFormKey());
        if (existing == null) {
            return Result.error("模板不存在");
        }
        if (existing.getIsPublic() == null || existing.getIsPublic() != 1) {
            return Result.error("该模板不是公共模板");
        }
        
        // 逻辑删除
        existing.setIsDeleted(1);
        formTemplateService.getBaseMapper().updateById(existing);
        
        return Result.success("删除成功", template.getFormKey());
    }
}

