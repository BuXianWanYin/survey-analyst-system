package com.server.surveyanalystserver.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.Survey;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.service.SurveyService;
import com.server.surveyanalystserver.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 问卷管理控制器
 */
@RestController
@RequestMapping("/api/survey")
@Api(tags = "问卷管理")
public class SurveyController {

    @Autowired
    private SurveyService surveyService;

    @Autowired
    private UserService userService;

    @Autowired
    private com.server.surveyanalystserver.service.FormTemplateService formTemplateService;

    @ApiOperation(value = "创建问卷", notes = "创建新问卷")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping
    public Result<Survey> createSurvey(@RequestBody Survey survey) {
        User currentUser = userService.getCurrentUser();
        survey.setUserId(currentUser.getId());
        Survey createdSurvey = surveyService.createSurvey(survey);
        return Result.success("创建成功", createdSurvey);
    }

    @ApiOperation(value = "更新问卷", notes = "更新问卷信息")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PutMapping("/{id}")
    public Result<Survey> updateSurvey(@PathVariable Long id, @RequestBody Survey survey) {
        survey.setId(id);
        Survey updatedSurvey = surveyService.updateSurvey(survey);
        return Result.success("更新成功", updatedSurvey);
    }

    @ApiOperation(value = "获取问卷详情", notes = "根据ID获取问卷详情")
    @GetMapping("/{id}")
    public Result<Survey> getSurveyById(@PathVariable Long id) {
        Survey survey = surveyService.getSurveyById(id);
        return Result.success("获取成功", survey);
    }

    @ApiOperation(value = "分页查询问卷列表", notes = "分页查询当前用户的问卷列表")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/list")
    public Result<Page<Survey>> getSurveyList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        User currentUser = userService.getCurrentUser();
        Page<Survey> page = new Page<>(pageNum, pageSize);
        Page<Survey> result = surveyService.getSurveyList(page, currentUser.getId());
        return Result.success("查询成功", result);
    }

    @ApiOperation(value = "发布问卷", notes = "发布问卷")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/{id}/publish")
    public Result<Void> publishSurvey(@PathVariable Long id) {
        surveyService.publishSurvey(id);
        return Result.success("发布成功");
    }
    
    @ApiOperation(value = "取消发布问卷", notes = "取消发布问卷（改为已结束）")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/{id}/unpublish")
    public Result<Void> unpublishSurvey(@PathVariable Long id) {
        surveyService.unpublishSurvey(id);
        return Result.success("取消发布成功");
    }

    @ApiOperation(value = "删除问卷", notes = "删除问卷（逻辑删除）")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> deleteSurvey(@PathVariable Long id) {
        surveyService.deleteSurvey(id);
        return Result.success("删除成功");
    }

    @ApiOperation(value = "使用模板创建问卷", notes = "根据模板 formKey 创建新问卷")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/use-template/create")
    public Result<Long> createSurveyByTemplate(@RequestBody com.server.surveyanalystserver.entity.FormTemplate request) {
        User currentUser = userService.getCurrentUser();
        Survey survey = formTemplateService.createSurveyByTemplate(request.getFormKey(), currentUser.getId());
        return Result.success("创建成功", survey.getId());
    }

    @ApiOperation(value = "验证问卷访问密码", notes = "验证问卷访问密码（公开接口）")
    @PostMapping("/{id}/verify-password")
    public Result<Boolean> verifyPassword(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String password = params.get("password");
        boolean isValid = surveyService.verifyPassword(id, password);
        if (isValid) {
            return Result.success("密码验证成功", true);
        } else {
            return Result.error("密码错误");
        }
    }
}
