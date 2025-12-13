package com.server.surveyanalystserver.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.FormTemplate;
import com.server.surveyanalystserver.service.FormTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

    /**
     * 管理员分页查询公共模板列表
     * 支持按模板名称和分类ID筛选公共模板
     * @param current 当前页码，默认为1
     * @param size 每页数量，默认为12
     * @param name 模板名称（模糊查询），可选
     * @param categoryId 分类ID，可选
     * @return 公共模板分页列表
     */
    @ApiOperation(value = "分页查询公共模板列表", notes = "管理员查询所有公共模板")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/page")
    public Result<Page<FormTemplate>> getPublicTemplatePage(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam(value = "每页数量", defaultValue = "12") @RequestParam(defaultValue = "12") Integer size,
            @ApiParam(value = "模板名称") @RequestParam(required = false) String name,
            @ApiParam(value = "分类ID") @RequestParam(required = false) Long categoryId) {
        Page<FormTemplate> page = new Page<>(current, size);
        Page<FormTemplate> result = formTemplateService.getAdminPublicTemplatePage(page, name, categoryId);
        return Result.success("查询成功", result);
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

    /**
     * 管理员创建公共模板
     * 创建新的公共模板，自动设置为公共模板（isPublic=1）
     * @param template 模板信息对象
     * @return 创建成功后的模板formKey
     */
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
    public Result<String> updatePublicTemplate(@ApiParam(value = "模板信息", required = true) @RequestBody FormTemplate template) {
        String formKey = formTemplateService.updateAdminPublicTemplate(template);
        return Result.success("更新成功", formKey);
    }

    @ApiOperation(value = "删除公共模板", notes = "管理员删除公共模板（逻辑删除）")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete")
    public Result<String> deletePublicTemplate(@ApiParam(value = "模板信息", required = true) @RequestBody FormTemplate template) {
        String formKey = formTemplateService.deleteAdminPublicTemplate(template.getFormKey());
        return Result.success("删除成功", formKey);
    }
}

