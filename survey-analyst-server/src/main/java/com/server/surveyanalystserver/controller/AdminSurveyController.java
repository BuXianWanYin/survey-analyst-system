package com.server.surveyanalystserver.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.Survey;
import com.server.surveyanalystserver.entity.dto.SurveyVO;
import com.server.surveyanalystserver.service.SurveyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员问卷管理控制器
 */
@RestController
@RequestMapping("/api/admin/survey")
@Api(tags = "管理员-问卷管理")
public class AdminSurveyController {

    @Autowired
    private SurveyService surveyService;

    /**
     * 管理员分页查询问卷列表
     * 支持按关键词、状态、用户ID筛选问卷，返回包含用户名的问卷列表
     * @param pageNum 页码，默认为1
     * @param pageSize 每页数量，默认为10
     * @param keyword 关键词，用于搜索问卷标题，可选
     * @param status 问卷状态（DRAFT-草稿，PUBLISHED-已发布），可选
     * @param userId 用户ID，可选
     * @return 问卷分页列表（包含用户名）
     */
    @ApiOperation(value = "分页查询问卷列表", notes = "管理员分页查询所有问卷")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public Result<Page<SurveyVO>> getSurveyList(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页数量", defaultValue = "10") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam(value = "关键词") @RequestParam(required = false) String keyword,
            @ApiParam(value = "状态") @RequestParam(required = false) String status,
            @ApiParam(value = "用户ID") @RequestParam(required = false) Long userId) {
        Page<SurveyVO> page = new Page<>(pageNum, pageSize);
        Page<SurveyVO> result = surveyService.getAdminSurveyList(page, keyword, status, userId);
        return Result.success("查询成功", result);
    }

    /**
     * 根据ID获取问卷详情
     * 查询问卷的完整信息，包括关联的表单配置等
     * @param id 问卷ID
     * @return 问卷详情信息
     */
    @ApiOperation(value = "获取问卷详情", notes = "根据ID获取问卷详情")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public Result<Survey> getSurveyById(
            @ApiParam(value = "问卷ID", required = true) @PathVariable Long id) {
        Survey survey = surveyService.getSurveyById(id);
        return Result.success("获取成功", survey);
    }

    /**
     * 管理员更新问卷状态
     * 更新指定问卷的状态（DRAFT-草稿，PUBLISHED-已发布等）
     * @param id 问卷ID
     * @param status 新状态
     * @return 更新结果
     */
    @ApiOperation(value = "更新问卷状态", notes = "管理员更新问卷状态")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public Result<Void> updateSurveyStatus(
            @ApiParam(value = "问卷ID", required = true) @PathVariable Long id,
            @ApiParam(value = "状态", required = true) @RequestParam String status) {
        surveyService.updateSurveyStatus(id, status);
        return Result.success("更新成功");
    }

    @ApiOperation(value = "删除问卷", notes = "管理员删除问卷（逻辑删除）")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> deleteSurvey(
            @ApiParam(value = "问卷ID", required = true) @PathVariable Long id) {
        surveyService.deleteSurvey(id);
        return Result.success("删除成功");
    }

    @ApiOperation(value = "获取问卷统计", notes = "获取问卷统计数据")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/statistics")
    public Result<Object> getSurveyStatistics() {
        Object statistics = surveyService.getSurveyStatistics();
        return Result.success("获取成功", statistics);
    }
}

