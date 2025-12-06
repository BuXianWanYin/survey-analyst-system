package com.server.surveyanalystserver.controller.admin;

import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.Survey;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.service.ResponseService;
import com.server.surveyanalystserver.service.SurveyService;
import com.server.surveyanalystserver.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员系统监控控制器
 */
@RestController
@RequestMapping("/api/admin/system")
@Api(tags = "管理员-系统监控")
public class AdminSystemController {

    @Autowired
    private UserService userService;

    @Autowired
    private SurveyService surveyService;

    @Autowired
    private ResponseService responseService;

    @ApiOperation(value = "获取系统概览", notes = "获取系统整体数据概览")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getSystemOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // 用户统计
        long totalUsers = userService.count();
        long activeUsers = userService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getStatus, 1));
        overview.put("totalUsers", totalUsers);
        overview.put("activeUsers", activeUsers);
        
        // 问卷统计
        long totalSurveys = surveyService.count();
        long publishedSurveys = surveyService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Survey>()
                .eq(Survey::getStatus, "PUBLISHED"));
        overview.put("totalSurveys", totalSurveys);
        overview.put("publishedSurveys", publishedSurveys);
        
        // 填写记录统计
        long totalResponses = responseService.count();
        long completedResponses = responseService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.server.surveyanalystserver.entity.Response>()
                .eq(com.server.surveyanalystserver.entity.Response::getStatus, "COMPLETED"));
        overview.put("totalResponses", totalResponses);
        overview.put("completedResponses", completedResponses);
        
        return Result.success("获取成功", overview);
    }
}

