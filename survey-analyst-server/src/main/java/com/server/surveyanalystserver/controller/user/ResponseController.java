package com.server.surveyanalystserver.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.Response;
import com.server.surveyanalystserver.service.ResponseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 填写记录控制器
 */
@RestController
@RequestMapping("/api/response")
@Api(tags = "填写记录管理")
public class ResponseController {

    @Autowired
    private ResponseService responseService;

    @ApiOperation(value = "提交填写记录", notes = "提交问卷填写记录")
    @PostMapping
    public Result<Response> submitResponse(@RequestBody Map<String, Object> params) {
        Response response = new Response();
        response.setSurveyId(Long.valueOf(params.get("surveyId").toString()));
        response.setIpAddress(params.get("ipAddress") != null ? params.get("ipAddress").toString() : null);
        response.setDeviceType(params.get("deviceType") != null ? params.get("deviceType").toString() : null);
        
        @SuppressWarnings("unchecked")
        Map<Long, Object> answers = (Map<Long, Object>) params.get("answers");
        
        Response submittedResponse = responseService.submitResponse(response, answers);
        return Result.success("提交成功", submittedResponse);
    }

    @ApiOperation(value = "保存草稿", notes = "保存问卷填写草稿")
    @PostMapping("/draft")
    public Result<Response> saveDraft(@RequestBody Map<String, Object> params) {
        Response response = new Response();
        response.setSurveyId(Long.valueOf(params.get("surveyId").toString()));
        response.setIpAddress(params.get("ipAddress") != null ? params.get("ipAddress").toString() : null);
        response.setDeviceType(params.get("deviceType") != null ? params.get("deviceType").toString() : null);
        
        @SuppressWarnings("unchecked")
        Map<Long, Object> answers = (Map<Long, Object>) params.get("answers");
        
        Response savedDraft = responseService.saveDraft(response, answers);
        return Result.success("保存成功", savedDraft);
    }

    @ApiOperation(value = "获取填写记录详情", notes = "根据ID获取填写记录详情")
    @GetMapping("/{id}")
    public Result<Response> getResponseById(@PathVariable Long id) {
        Response response = responseService.getResponseById(id);
        return Result.success("获取成功", response);
    }

    @ApiOperation(value = "分页查询填写记录", notes = "分页查询问卷的填写记录")
    @GetMapping("/list")
    public Result<Page<Response>> getResponseList(
            @RequestParam Long surveyId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Response> page = new Page<>(pageNum, pageSize);
        Page<Response> result = responseService.getResponseList(page, surveyId);
        return Result.success("查询成功", result);
    }
}

