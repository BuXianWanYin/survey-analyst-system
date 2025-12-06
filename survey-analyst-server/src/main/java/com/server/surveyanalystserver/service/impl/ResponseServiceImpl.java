package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.Answer;
import com.server.surveyanalystserver.entity.Response;
import com.server.surveyanalystserver.mapper.AnswerMapper;
import com.server.surveyanalystserver.mapper.ResponseMapper;
import com.server.surveyanalystserver.service.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 填写记录Service实现类
 */
@Service
public class ResponseServiceImpl extends ServiceImpl<ResponseMapper, Response> implements ResponseService {

    @Autowired
    private AnswerMapper answerMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response submitResponse(Response response, Map<Long, Object> answers) {
        response.setStatus("COMPLETED");
        response.setSubmitTime(LocalDateTime.now());
        if (response.getStartTime() != null) {
            long duration = java.time.Duration.between(response.getStartTime(), response.getSubmitTime()).getSeconds();
            response.setDuration((int) duration);
        }
        this.save(response);

        // 保存答案
        saveAnswers(response.getId(), answers);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response saveDraft(Response response, Map<Long, Object> answers) {
        response.setStatus("DRAFT");
        this.saveOrUpdate(response);

        // 保存答案
        if (answers != null && !answers.isEmpty()) {
            saveAnswers(response.getId(), answers);
        }
        return response;
    }

    @Override
    public Response getResponseById(Long id) {
        return this.getById(id);
    }

    @Override
    public Page<Response> getResponseList(Page<Response> page, Long surveyId) {
        LambdaQueryWrapper<Response> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Response::getSurveyId, surveyId);
        wrapper.orderByDesc(Response::getCreateTime);
        return this.page(page, wrapper);
    }

    /**
     * 保存答案
     */
    private void saveAnswers(Long responseId, Map<Long, Object> answers) {
        for (Map.Entry<Long, Object> entry : answers.entrySet()) {
            Long questionId = entry.getKey();
            Object answerValue = entry.getValue();

            if (answerValue instanceof List) {
                // 多选题：保存多个选项
                @SuppressWarnings("unchecked")
                List<Long> optionIds = (List<Long>) answerValue;
                for (Long optionId : optionIds) {
                    Answer answer = new Answer();
                    answer.setResponseId(responseId);
                    answer.setQuestionId(questionId);
                    answer.setOptionId(optionId);
                    answerMapper.insert(answer);
                }
            } else if (answerValue instanceof Long) {
                // 单选题：保存单个选项
                Answer answer = new Answer();
                answer.setResponseId(responseId);
                answer.setQuestionId(questionId);
                answer.setOptionId((Long) answerValue);
                answerMapper.insert(answer);
            } else if (answerValue instanceof String) {
                // 填空题：保存文本内容
                Answer answer = new Answer();
                answer.setResponseId(responseId);
                answer.setQuestionId(questionId);
                answer.setContent((String) answerValue);
                answerMapper.insert(answer);
            }
        }
    }
}

