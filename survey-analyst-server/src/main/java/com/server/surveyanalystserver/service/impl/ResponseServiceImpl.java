package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.Answer;
import com.server.surveyanalystserver.entity.FormConfig;
import com.server.surveyanalystserver.entity.FormSetting;
import com.server.surveyanalystserver.entity.Response;
import com.server.surveyanalystserver.entity.Survey;
import com.server.surveyanalystserver.mapper.AnswerMapper;
import com.server.surveyanalystserver.mapper.ResponseMapper;
import com.server.surveyanalystserver.service.EmailService;
import com.server.surveyanalystserver.service.FormConfigService;
import com.server.surveyanalystserver.service.FormDataService;
import com.server.surveyanalystserver.service.FormItemService;
import com.server.surveyanalystserver.service.FormSettingService;
import com.server.surveyanalystserver.service.ResponseService;
import com.server.surveyanalystserver.service.SurveyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 填写记录Service实现类
 */
@Slf4j
@Service
public class ResponseServiceImpl extends ServiceImpl<ResponseMapper, Response> implements ResponseService {

    @Autowired
    private AnswerMapper answerMapper;
    
    @Autowired
    private FormConfigService formConfigService;
    
    @Autowired
    private FormDataService formDataService;
    
    @Autowired
    private FormItemService formItemService;
    
    @Autowired
    private SurveyService surveyService;
    
    @Autowired
    private FormSettingService formSettingService;
    
    @Autowired
    private EmailService emailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response submitResponse(Response response, Map<Long, Object> answers) {
        // 验证问卷状态和访问权限
        Survey survey = surveyService.getById(response.getSurveyId());
        if (survey == null) {
            throw new RuntimeException("问卷不存在");
        }
        
        // 检查问卷状态
        if (!"PUBLISHED".equals(survey.getStatus())) {
            throw new RuntimeException("问卷未发布或已暂停，无法提交");
        }
        
        // 检查时间限制
        LocalDateTime now = LocalDateTime.now();
        if (survey.getStartTime() != null && now.isBefore(survey.getStartTime())) {
            throw new RuntimeException("问卷尚未开始，无法提交");
        }
        if (survey.getEndTime() != null && now.isAfter(survey.getEndTime())) {
            throw new RuntimeException("问卷已结束，无法提交");
        }
        
        // 检查填写数量限制
        if (survey.getMaxResponses() != null && survey.getMaxResponses() > 0) {
            long currentCount = getResponseCount(response.getSurveyId());
            if (currentCount >= survey.getMaxResponses()) {
                throw new RuntimeException("问卷已达到最大填写数，无法继续提交");
            }
        }
        
        response.setStatus("COMPLETED");
        response.setSubmitTime(LocalDateTime.now());
        if (response.getStartTime() != null) {
            long duration = java.time.Duration.between(response.getStartTime(), response.getSubmitTime()).getSeconds();
            response.setDuration((int) duration);
        }
        this.save(response);

        // 保存答案到 answer 表
        saveAnswers(response.getId(), answers);
        
        // 同时保存到 form_data 表
        try {
            FormConfig formConfig = formConfigService.getBySurveyId(response.getSurveyId());
            if (formConfig != null && formConfig.getFormKey() != null) {
                // 将 answers 转换为 form_data 格式（formItemId -> value）
                Map<String, Object> originalData = new java.util.HashMap<>();
                for (Map.Entry<Long, Object> entry : answers.entrySet()) {
                    Long questionId = entry.getKey();
                    Object answerValue = entry.getValue();
                    
                    // 通过 questionId 查找对应的 formItemId
                    String formItemId = formItemService.getFormItemIdByFormKeyAndQuestionId(
                        formConfig.getFormKey(), questionId);
                    
                    // 必须找到 formItemId 才保存，不再使用兼容模式
                    if (formItemId != null) {
                        originalData.put(formItemId, answerValue);
                    }
                    // 如果找不到 formItemId，跳过该数据（不再使用 questionId 作为 key）
                }
                
                if (!originalData.isEmpty()) {
                    formDataService.saveFormData(formConfig.getFormKey(), originalData);
                }
            }
        } catch (Exception e) {
            // 如果保存到 form_data 失败，不影响主流程，只记录日志
            // log.error("保存到 form_data 失败", e);
        }
        
        // 发送邮件通知
        try {
            FormSetting formSetting = formSettingService.getBySurveyId(response.getSurveyId());
            if (formSetting != null && formSetting.getSettings() != null) {
                Object emailNotifyObj = formSetting.getSettings().get("emailNotify");
                Object emailListObj = formSetting.getSettings().get("newWriteNotifyEmail");
                
                // 检查是否启用了邮件通知
                boolean emailNotify = false;
                if (emailNotifyObj instanceof Boolean) {
                    emailNotify = (Boolean) emailNotifyObj;
                } else if (emailNotifyObj != null) {
                    emailNotify = Boolean.parseBoolean(emailNotifyObj.toString());
                }
                
                // 如果启用了邮件通知且有邮箱列表，发送邮件
                if (emailNotify && emailListObj != null && !emailListObj.toString().trim().isEmpty()) {
                    String emailList = emailListObj.toString().trim();
                    emailService.sendSurveySubmitNotification(emailList, survey.getTitle(), survey.getId());
                }
            }
        } catch (Exception e) {
            // 邮件发送失败不影响主流程，只记录日志
            log.error("发送邮件通知失败", e);
        }
        
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

    @Override
    public long getResponseCount(Long surveyId) {
        LambdaQueryWrapper<Response> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Response::getSurveyId, surveyId);
        wrapper.eq(Response::getStatus, "COMPLETED");
        return this.count(wrapper);
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

