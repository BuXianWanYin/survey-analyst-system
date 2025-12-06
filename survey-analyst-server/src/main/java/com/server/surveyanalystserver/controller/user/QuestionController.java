package com.server.surveyanalystserver.controller.user;

import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.entity.Question;
import com.server.surveyanalystserver.service.QuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题目管理控制器
 */
@RestController
@RequestMapping("/api/question")
@Api(tags = "题目管理")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @ApiOperation(value = "添加题目", notes = "添加新题目")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping
    public Result<Question> addQuestion(@RequestBody Question question) {
        Question createdQuestion = questionService.addQuestion(question);
        return Result.success("添加成功", createdQuestion);
    }

    @ApiOperation(value = "更新题目", notes = "更新题目信息")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PutMapping("/{id}")
    public Result<Question> updateQuestion(@PathVariable Long id, @RequestBody Question question) {
        question.setId(id);
        Question updatedQuestion = questionService.updateQuestion(question);
        return Result.success("更新成功", updatedQuestion);
    }

    @ApiOperation(value = "删除题目", notes = "删除题目")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return Result.success("删除成功");
    }

    @ApiOperation(value = "获取问卷的所有题目", notes = "根据问卷ID获取所有题目")
    @GetMapping("/survey/{surveyId}")
    public Result<List<Question>> getQuestionsBySurveyId(@PathVariable Long surveyId) {
        List<Question> questions = questionService.getQuestionsBySurveyId(surveyId);
        return Result.success("查询成功", questions);
    }

    @ApiOperation(value = "更新题目排序", notes = "批量更新题目排序")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PutMapping("/order")
    public Result<Void> updateQuestionOrder(@RequestBody List<Question> questions) {
        questionService.updateQuestionOrder(questions);
        return Result.success("排序更新成功");
    }
}

