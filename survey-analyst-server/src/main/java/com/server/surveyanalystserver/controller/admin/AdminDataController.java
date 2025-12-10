package com.server.surveyanalystserver.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.Response;
import com.server.surveyanalystserver.service.ResponseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员数据管理控制器
 */
@RestController
@RequestMapping("/api/admin/data")
@Api(tags = "管理员-数据管理")
public class AdminDataController {

    @Autowired
    private ResponseService responseService;

    @ApiOperation(value = "分页查询填写记录", notes = "管理员分页查询所有填写记录（包含问卷名称、发布用户名称、填写用户名称）")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/response/list")
    public Result<Page<com.server.surveyanalystserver.entity.dto.ResponseVO>> getResponseList(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页数量", defaultValue = "10") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam(value = "问卷ID（可选）") @RequestParam(required = false) Long surveyId,
            @ApiParam(value = "问卷名称（可选，模糊查询）") @RequestParam(required = false) String surveyTitle,
            @ApiParam(value = "发布用户名称（可选，模糊查询）") @RequestParam(required = false) String publisherName,
            @ApiParam(value = "填写用户名称（可选，模糊查询）") @RequestParam(required = false) String userName) {
        Page<com.server.surveyanalystserver.entity.dto.ResponseVO> page = 
            new Page<>(pageNum, pageSize);
        Page<com.server.surveyanalystserver.entity.dto.ResponseVO> result = 
            responseService.getResponseListWithDetails(page, surveyId, surveyTitle, publisherName, userName);
        return Result.success("查询成功", result);
    }

    @ApiOperation(value = "获取填写记录详情", notes = "根据ID获取填写记录详情")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/response/{id}")
    public Result<Response> getResponseById(@PathVariable Long id) {
        Response response = responseService.getResponseById(id);
        return Result.success("获取成功", response);
    }

    @ApiOperation(value = "删除填写记录", notes = "管理员删除填写记录")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/response/{id}")
    public Result<Void> deleteResponse(@PathVariable Long id) {
        responseService.removeById(id);
        return Result.success("删除成功");
    }

    @ApiOperation(value = "获取数据统计", notes = "获取数据统计数据")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/statistics")
    public Result<Object> getDataStatistics() {
        long totalResponses = responseService.count();
        long completedResponses = responseService.count(new LambdaQueryWrapper<Response>().eq(Response::getStatus, "COMPLETED"));
        long draftResponses = responseService.count(new LambdaQueryWrapper<Response>().eq(Response::getStatus, "DRAFT"));
        
        java.util.Map<String, Object> statistics = new java.util.HashMap<>();
        statistics.put("totalResponses", totalResponses);
        statistics.put("completedResponses", completedResponses);
        statistics.put("draftResponses", draftResponses);
        
        return Result.success("获取成功", statistics);
    }
}

