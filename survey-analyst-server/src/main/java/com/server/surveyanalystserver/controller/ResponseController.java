package com.server.surveyanalystserver.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.Response;
import com.server.surveyanalystserver.service.ResponseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 填写记录控制器
 */
@RestController
@RequestMapping("/api/response")
@Api(tags = "填写记录管理")
public class ResponseController {

    @Autowired
    private ResponseService responseService;

    /**
     * 根据ID获取填写记录详情
     * 查询指定填写记录的完整信息
     * @param id 填写记录ID
     * @return 填写记录详情
     */
    @ApiOperation(value = "获取填写记录详情", notes = "根据ID获取填写记录详情")
    @GetMapping("/{id}")
    public Result<Response> getResponseById(@PathVariable Long id) {
        Response response = responseService.getResponseById(id);
        return Result.success("获取成功", response);
    }

    /**
     * 分页查询问卷的填写记录
     * 查询指定问卷的所有填写记录，支持分页
     * @param surveyId 问卷ID
     * @param pageNum 页码，默认为1
     * @param pageSize 每页数量，默认为10
     * @return 填写记录分页列表
     */
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

    /**
     * 获取问卷填写数量
     * 获取指定问卷的填写总数（公开接口，无需登录）
     * @param surveyId 问卷ID
     * @return 填写数量
     */
    @ApiOperation(value = "获取问卷填写数量", notes = "获取问卷的填写数量（公开接口）")
    @GetMapping("/count/{surveyId}")
    public Result<Long> getResponseCount(@PathVariable Long surveyId) {
        long count = responseService.getResponseCount(surveyId);
        return Result.success("获取成功", count);
    }
}

