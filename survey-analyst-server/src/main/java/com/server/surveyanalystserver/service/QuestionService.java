package com.server.surveyanalystserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.server.surveyanalystserver.entity.Question;

import java.util.List;

/**
 * 题目Service接口
 */
public interface QuestionService extends IService<Question> {

    /**
     * 添加题目
     * @param question 题目信息
     * @return 创建的题目
     */
    Question addQuestion(Question question);

    /**
     * 更新题目
     * @param question 题目信息
     * @return 更新后的题目
     */
    Question updateQuestion(Question question);

    /**
     * 删除题目
     * @param id 题目ID
     * @return 是否成功
     */
    boolean deleteQuestion(Long id);

    /**
     * 获取问卷的所有题目
     * @param surveyId 问卷ID
     * @return 题目列表
     */
    List<Question> getQuestionsBySurveyId(Long surveyId);

    /**
     * 更新题目排序
     * @param questions 题目列表（包含orderNum）
     * @return 是否成功
     */
    boolean updateQuestionOrder(List<Question> questions);
}

