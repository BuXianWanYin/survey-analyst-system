package com.server.surveyanalystserver.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.Survey;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.entity.FormTemplate;
import com.server.surveyanalystserver.service.FormTemplateService;
import com.server.surveyanalystserver.service.SurveyService;
import com.server.surveyanalystserver.service.UserService;
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
    private FormTemplateService formTemplateService;

    /**
     * 创建新问卷
     * 创建问卷并自动设置当前用户为创建者
     * @param survey 问卷信息对象，必须包含title字段
     * @return 创建成功后的问卷信息
     */
    @ApiOperation(value = "创建问卷", notes = "创建新问卷")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping
    public Result<Survey> createSurvey(@RequestBody Survey survey) {
        User currentUser = userService.getCurrentUser();
        survey.setUserId(currentUser.getId());
        Survey createdSurvey = surveyService.createSurvey(survey);
        return Result.success("创建成功", createdSurvey);
    }

    /**
     * 更新问卷信息
     * 更新问卷的基本信息，不包括状态和发布相关字段
     * @param id 问卷ID
     * @param survey 包含更新信息的问卷对象
     * @return 更新后的问卷信息
     */
    @ApiOperation(value = "更新问卷", notes = "更新问卷信息")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PutMapping("/{id}")
    public Result<Survey> updateSurvey(@PathVariable Long id, @RequestBody Survey survey) {
        survey.setId(id);
        Survey updatedSurvey = surveyService.updateSurvey(survey);
        return Result.success("更新成功", updatedSurvey);
    }

    /**
     * 根据ID获取问卷详情
     * 查询问卷的完整信息，包括关联的表单配置等
     * @param id 问卷ID
     * @return 问卷详情信息
     */
    @ApiOperation(value = "获取问卷详情", notes = "根据ID获取问卷详情")
    @GetMapping("/{id}")
    public Result<Survey> getSurveyById(@PathVariable Long id) {
        Survey survey = surveyService.getSurveyById(id);
        return Result.success("获取成功", survey);
    }

    /**
     * 分页查询当前用户的问卷列表
     * 查询当前登录用户创建的所有问卷，包含答卷数量统计
     * @param pageNum 页码，默认为1
     * @param pageSize 每页数量，默认为10
     * @return 问卷分页列表
     */
    @ApiOperation(value = "分页查询问卷列表", notes = "分页查询当前用户的问卷列表")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/list")
    public Result<Page<Survey>> getSurveyList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        User currentUser = userService.getCurrentUser();
        Page<Survey> page = new Page<>(pageNum, pageSize);
        Page<Survey> result = surveyService.getSurveyListWithResponseCount(page, currentUser.getId());
        return Result.success("查询成功", result);
    }

    /**
     * 发布问卷
     * 将问卷状态设置为已发布，允许用户填写
     * @param id 问卷ID
     * @return 发布结果
     */
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
    public Result<Long> createSurveyByTemplate(@RequestBody FormTemplate request) {
        User currentUser = userService.getCurrentUser();
        Survey survey = formTemplateService.createSurveyByTemplate(request.getFormKey(), currentUser.getId());
        return Result.success("创建成功", survey.getId());
    }

    @ApiOperation(value = "验证问卷访问密码", notes = "验证问卷访问密码（公开接口）")
    @PostMapping("/{id}/verify-password")
    public Result<Boolean> verifyPassword(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String password = params.get("password");
        boolean isValid = surveyService.verifyPassword(id, password);
        return isValid ? Result.success("密码验证成功", true) : Result.error("密码错误");
    }
}
