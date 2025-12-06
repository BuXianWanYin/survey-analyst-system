package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.Question;
import com.server.surveyanalystserver.mapper.QuestionMapper;
import com.server.surveyanalystserver.service.QuestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 题目Service实现类
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Question addQuestion(Question question) {
        this.save(question);
        return question;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Question updateQuestion(Question question) {
        this.updateById(question);
        return this.getById(question.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteQuestion(Long id) {
        return this.removeById(id);
    }

    @Override
    public List<Question> getQuestionsBySurveyId(Long surveyId) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getSurveyId, surveyId);
        wrapper.orderByAsc(Question::getOrderNum);
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateQuestionOrder(List<Question> questions) {
        return this.updateBatchById(questions);
    }
}

