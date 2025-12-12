package com.server.surveyanalystserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.Survey;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.entity.dto.SurveyVO;
import com.server.surveyanalystserver.mapper.UserMapper;
import com.server.surveyanalystserver.service.SurveyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员问卷管理控制器
 */
@RestController
@RequestMapping("/api/admin/survey")
@Api(tags = "管理员-问卷管理")
public class AdminSurveyController {

    @Autowired
    private SurveyService surveyService;

    @Autowired
    private UserMapper userMapper;

    @ApiOperation(value = "分页查询问卷列表", notes = "管理员分页查询所有问卷")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public Result<Page<SurveyVO>> getSurveyList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId) {
        Page<Survey> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Survey> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Survey::getTitle, keyword);
        }
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Survey::getStatus, status);
        }
        
        if (userId != null) {
            wrapper.eq(Survey::getUserId, userId);
        }
        
        wrapper.orderByDesc(Survey::getCreateTime);
        Page<Survey> result = surveyService.page(page, wrapper);
        
        // 转换为VO并填充用户名
        Page<SurveyVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<SurveyVO> voList = result.getRecords().stream().map(survey -> {
            SurveyVO vo = new SurveyVO();
            BeanUtils.copyProperties(survey, vo);
            if (survey.getUserId() != null) {
                User user = userMapper.selectById(survey.getUserId());
                if (user != null) {
                    vo.setUsername(user.getUsername());
                }
            }
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return Result.success("查询成功", voPage);
    }

    @ApiOperation(value = "获取问卷详情", notes = "根据ID获取问卷详情")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public Result<Survey> getSurveyById(@PathVariable Long id) {
        Survey survey = surveyService.getSurveyById(id);
        return Result.success("获取成功", survey);
    }

    @ApiOperation(value = "更新问卷状态", notes = "管理员更新问卷状态")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public Result<Void> updateSurveyStatus(@PathVariable Long id, @RequestParam String status) {
        Survey survey = surveyService.getSurveyById(id);
        if (survey == null) {
            return Result.error("问卷不存在");
        }
        survey.setStatus(status);
        surveyService.updateSurvey(survey);
        return Result.success("更新成功");
    }

    @ApiOperation(value = "删除问卷", notes = "管理员删除问卷（逻辑删除）")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> deleteSurvey(@PathVariable Long id) {
        surveyService.deleteSurvey(id);
        return Result.success("删除成功");
    }

    @ApiOperation(value = "获取问卷统计", notes = "获取问卷统计数据")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/statistics")
    public Result<Object> getSurveyStatistics() {
        long totalSurveys = surveyService.count();
        long draftSurveys = surveyService.count(new LambdaQueryWrapper<Survey>().eq(Survey::getStatus, "DRAFT"));
        long publishedSurveys = surveyService.count(new LambdaQueryWrapper<Survey>().eq(Survey::getStatus, "PUBLISHED"));
        long endedSurveys = surveyService.count(new LambdaQueryWrapper<Survey>().eq(Survey::getStatus, "ENDED"));
        
        java.util.Map<String, Object> statistics = new java.util.HashMap<>();
        statistics.put("totalSurveys", totalSurveys);
        statistics.put("draftSurveys", draftSurveys);
        statistics.put("publishedSurveys", publishedSurveys);
        statistics.put("endedSurveys", endedSurveys);
        
        return Result.success("获取成功", statistics);
    }
}

