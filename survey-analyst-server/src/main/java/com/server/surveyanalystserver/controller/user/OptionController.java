package com.server.surveyanalystserver.controller.user;

import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.Option;
import com.server.surveyanalystserver.service.OptionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 选项管理控制器
 */
@RestController
@RequestMapping("/api/option")
@Api(tags = "选项管理")
public class OptionController {

    @Autowired
    private OptionService optionService;

    @ApiOperation(value = "添加选项", notes = "添加新选项")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping
    public Result<Option> addOption(@RequestBody Option option) {
        Option createdOption = optionService.addOption(option);
        return Result.success("添加成功", createdOption);
    }

    @ApiOperation(value = "更新选项", notes = "更新选项信息")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PutMapping("/{id}")
    public Result<Option> updateOption(@PathVariable Long id, @RequestBody Option option) {
        option.setId(id);
        Option updatedOption = optionService.updateOption(option);
        return Result.success("更新成功", updatedOption);
    }

    @ApiOperation(value = "删除选项", notes = "删除选项")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> deleteOption(@PathVariable Long id) {
        optionService.deleteOption(id);
        return Result.success("删除成功");
    }

    @ApiOperation(value = "获取题目的所有选项", notes = "根据题目ID获取所有选项")
    @GetMapping("/question/{questionId}")
    public Result<List<Option>> getOptionsByQuestionId(@PathVariable Long questionId) {
        List<Option> options = optionService.getOptionsByQuestionId(questionId);
        return Result.success("查询成功", options);
    }
}

