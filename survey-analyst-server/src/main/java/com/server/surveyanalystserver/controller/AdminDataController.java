package com.server.surveyanalystserver.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.FormData;
import com.server.surveyanalystserver.entity.Response;
import com.server.surveyanalystserver.entity.dto.ResponseVO;
import com.server.surveyanalystserver.service.FormDataService;
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
    
    @Autowired
    private FormDataService formDataService;

    /**
     * 管理员分页查询填写记录
     * 支持按问卷ID、问卷名称、发布用户、填写用户等条件筛选，返回包含关联信息的填写记录列表
     * @param pageNum 页码，默认为1
     * @param pageSize 每页数量，默认为10
     * @param surveyId 问卷ID，可选
     * @param surveyTitle 问卷名称（模糊查询），可选
     * @param publisherName 发布用户名称（模糊查询），可选
     * @param publisherId 发布用户ID（精确查询），可选
     * @param userName 填写用户名称（模糊查询），可选
     * @return 填写记录分页列表（包含问卷名称、发布用户名称、填写用户名称）
     */
    @ApiOperation(value = "分页查询填写记录", notes = "管理员分页查询所有填写记录（包含问卷名称、发布用户名称、填写用户名称）")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/response/list")
    public Result<Page<ResponseVO>> getResponseList(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam(value = "每页数量", defaultValue = "10") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam(value = "问卷ID（可选）") @RequestParam(required = false) Long surveyId,
            @ApiParam(value = "问卷名称（可选，模糊查询）") @RequestParam(required = false) String surveyTitle,
            @ApiParam(value = "发布用户名称（可选，模糊查询）") @RequestParam(required = false) String publisherName,
            @ApiParam(value = "发布用户ID（可选，精确查询）") @RequestParam(required = false) Long publisherId,
            @ApiParam(value = "填写用户名称（可选，模糊查询）") @RequestParam(required = false) String userName) {
        Page<ResponseVO> page = new Page<>(pageNum, pageSize);
        Page<ResponseVO> result = responseService.getResponseListWithDetails(page, surveyId, surveyTitle, publisherName, publisherId, userName);
        return Result.success("查询成功", result);
    }

    /**
     * 根据ID获取填写记录详情
     * 查询指定填写记录的完整信息
     * @param id 填写记录ID
     * @return 填写记录详情
     */
    @ApiOperation(value = "获取填写记录详情", notes = "根据ID获取填写记录详情")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/response/{id}")
    public Result<Response> getResponseById(
            @ApiParam(value = "填写记录ID", required = true) @PathVariable Long id) {
        Response response = responseService.getResponseById(id);
        return Result.success("获取成功", response);
    }

    @ApiOperation(value = "获取填写记录对应的表单数据详情", notes = "根据Response ID获取对应的表单数据详情")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/response/{id}/form-data")
    public Result<FormData> getFormDataByResponseId(
            @ApiParam(value = "填写记录ID", required = true) @PathVariable Long id) {
        FormData formData = formDataService.getFormDataByResponseId(id);
        return Result.success("获取成功", formData);
    }

    @ApiOperation(value = "删除填写记录", notes = "管理员删除填写记录")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/response/{id}")
    public Result<Void> deleteResponse(@PathVariable Long id) {
        responseService.removeById(id);
        return Result.success("删除成功");
    }

    /**
     * 获取数据统计数据
     * 获取填写记录的总数、已完成数、草稿数等统计数据
     * @return 统计数据对象
     */
    @ApiOperation(value = "获取数据统计", notes = "获取数据统计数据")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/statistics")
    public Result<Object> getDataStatistics() {
        Object statistics = responseService.getDataStatistics();
        return Result.success("获取成功", statistics);
    }
}

