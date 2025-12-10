package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.Answer;
import com.server.surveyanalystserver.entity.FormConfig;
import com.server.surveyanalystserver.entity.FormData;
import com.server.surveyanalystserver.entity.FormSetting;
import com.server.surveyanalystserver.entity.Response;
import com.server.surveyanalystserver.entity.Survey;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.utils.UserAgentUtils;
import com.server.surveyanalystserver.entity.dto.ResponseVO;
import com.server.surveyanalystserver.mapper.AnswerMapper;
import com.server.surveyanalystserver.mapper.ResponseMapper;
import com.server.surveyanalystserver.mapper.user.UserMapper;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

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

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response submitResponse(Response response, Map<Long, Object> answers, HttpServletRequest request) {
        // 验证问卷状态和访问权限
        Survey survey = surveyService.getById(response.getSurveyId());
        if (survey == null) {
            throw new RuntimeException("问卷不存在");
        }
        
        // 检查问卷状态
        if (!"PUBLISHED".equals(survey.getStatus())) {
            throw new RuntimeException("问卷尚未发布，无法提交");
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
                throw new RuntimeException("已超出该问卷的填写数量（" + survey.getMaxResponses() + "份），无法继续提交");
            }
        }
        
        // 检查IP/设备/用户限制（从表单设置中读取）
        FormSetting formSetting = formSettingService.getBySurveyId(response.getSurveyId());
        if (formSetting != null && formSetting.getSettings() != null) {
            Map<String, Object> settings = formSetting.getSettings();
            
            // 检查IP限制
            if (response.getIpAddress() != null && !response.getIpAddress().isEmpty()) {
                Object ipLimitStatusObj = settings.get("ipWriteCountLimitStatus");
                boolean ipLimitStatus = false;
                if (ipLimitStatusObj instanceof Boolean) {
                    ipLimitStatus = (Boolean) ipLimitStatusObj;
                } else if (ipLimitStatusObj != null) {
                    ipLimitStatus = Boolean.parseBoolean(ipLimitStatusObj.toString());
                }
                
                if (ipLimitStatus) {
                    Object ipLimitObj = settings.get("ipWriteCountLimit");
                    int ipLimit = 1;
                    if (ipLimitObj instanceof Number) {
                        ipLimit = ((Number) ipLimitObj).intValue();
                    } else if (ipLimitObj != null) {
                        try {
                            ipLimit = Integer.parseInt(ipLimitObj.toString());
                        } catch (NumberFormatException e) {
                            ipLimit = 1;
                        }
                    }
                    
                    LambdaQueryWrapper<Response> ipWrapper = new LambdaQueryWrapper<>();
                    ipWrapper.eq(Response::getSurveyId, response.getSurveyId())
                             .eq(Response::getIpAddress, response.getIpAddress())
                             .eq(Response::getStatus, "COMPLETED");
                    long ipCount = this.count(ipWrapper);
                    
                    if (ipCount >= ipLimit) {
                        throw new RuntimeException("该IP地址已达到答题次数限制（" + ipLimit + "次），无法继续提交");
                    }
                }
            }
            
            // 检查用户限制
            if (response.getUserId() != null) {
                Object userLimitStatusObj = settings.get("accountWriteCountLimitStatus");
                boolean userLimitStatus = false;
                if (userLimitStatusObj instanceof Boolean) {
                    userLimitStatus = (Boolean) userLimitStatusObj;
                } else if (userLimitStatusObj != null) {
                    userLimitStatus = Boolean.parseBoolean(userLimitStatusObj.toString());
                }
                
                if (userLimitStatus) {
                    Object userLimitObj = settings.get("accountWriteCountLimit");
                    int userLimit = 1;
                    if (userLimitObj instanceof Number) {
                        userLimit = ((Number) userLimitObj).intValue();
                    } else if (userLimitObj != null) {
                        try {
                            userLimit = Integer.parseInt(userLimitObj.toString());
                        } catch (NumberFormatException e) {
                            userLimit = 1;
                        }
                    }
                    
                    LambdaQueryWrapper<Response> userWrapper = new LambdaQueryWrapper<>();
                    userWrapper.eq(Response::getSurveyId, response.getSurveyId())
                               .eq(Response::getUserId, response.getUserId())
                               .eq(Response::getStatus, "COMPLETED");
                    long userCount = this.count(userWrapper);
                    
                    if (userCount >= userLimit) {
                        throw new RuntimeException("您已达到答题次数限制（" + userLimit + "次），无法继续提交");
                    }
                }
            }
        }
        
        response.setStatus("COMPLETED");
        response.setSubmitTime(LocalDateTime.now());
        if (response.getStartTime() != null) {
            long duration = Duration.between(response.getStartTime(), response.getSubmitTime()).getSeconds();
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
                Map<String, Object> originalData = new HashMap<>();
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
                    // 获取IP地址（从response中获取）
                    String ipAddress = response.getIpAddress();
                    // 设备ID暂时为空，后续可以从request中获取
                    String deviceId = null;
                    // 用户ID从response中获取
                    Long userId = response.getUserId();
                    
                    // 解析User-Agent信息
                    String browser = null;
                    String os = null;
                    Map<String, Object> uaInfo = null;
                    if (request != null) {
                        String userAgent = request.getHeader("User-Agent");
                        if (userAgent != null && !userAgent.isEmpty()) {
                            browser = UserAgentUtils.getBrowser(userAgent);
                            os = UserAgentUtils.getOS(userAgent);
                            uaInfo = UserAgentUtils.getUaInfo(userAgent);
                        }
                    }
                    
                    formDataService.saveFormData(formConfig.getFormKey(), originalData, ipAddress, deviceId, userId, response.getStartTime(), browser, os, uaInfo);
                }
            }
        } catch (Exception e) {
            // 如果保存到 form_data 失败，不影响主流程，只记录日志
            // log.error("保存到 form_data 失败", e);
        }
        
        // 发送邮件通知
        try {
            // 复用之前获取的formSetting，如果为null则重新获取
            if (formSetting == null) {
                formSetting = formSettingService.getBySurveyId(response.getSurveyId());
            }
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
        // 通过 surveyId 获取 formKey，然后统计 form_data 表的数据
        // 这样与数据管理页面显示的数据保持一致
        FormConfig formConfig = formConfigService.getBySurveyId(surveyId);
        if (formConfig == null || formConfig.getFormKey() == null) {
            // 如果没有 formConfig，回退到统计 response 表
            LambdaQueryWrapper<Response> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Response::getSurveyId, surveyId);
            wrapper.ne(Response::getStatus, "DRAFT");
            return this.count(wrapper);
        }
        
        // 统计 form_data 表的数据
        LambdaQueryWrapper<FormData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormData::getFormKey, formConfig.getFormKey());
        return formDataService.count(wrapper);
    }

    @Override
    public Page<ResponseVO> getResponseListWithDetails(Page<ResponseVO> page, Long surveyId, 
            String surveyTitle, String publisherName, String userName) {
        // 判断是否有需要在内存中筛选的条件
        boolean needMemoryFilter = (surveyTitle != null && !surveyTitle.isEmpty()) 
            || (publisherName != null && !publisherName.isEmpty())
            || (userName != null && !userName.isEmpty());
        
        // 如果需要在内存中筛选，查询更多数据（最多1000条）
        int querySize = needMemoryFilter ? 1000 : (int) page.getSize();
        long queryPage = needMemoryFilter ? 1 : page.getCurrent();
        
        // 先查询Response
        LambdaQueryWrapper<Response> wrapper = new LambdaQueryWrapper<>();
        
        if (surveyId != null) {
            wrapper.eq(Response::getSurveyId, surveyId);
        }
        
        wrapper.orderByDesc(Response::getCreateTime);
        Page<Response> responsePage = new Page<>(queryPage, querySize);
        Page<Response> result = this.page(responsePage, wrapper);
        
        // 转换为VO并填充关联信息
        List<ResponseVO> voList = result.getRecords().stream().map(response -> {
            ResponseVO vo = new ResponseVO();
            vo.setId(response.getId());
            vo.setSurveyId(response.getSurveyId());
            vo.setUserId(response.getUserId());
            vo.setIpAddress(response.getIpAddress());
            vo.setDeviceType(response.getDeviceType());
            vo.setStartTime(response.getStartTime());
            vo.setSubmitTime(response.getSubmitTime());
            vo.setDuration(response.getDuration());
            vo.setStatus(response.getStatus());
            vo.setCreateTime(response.getCreateTime());
            
            // 查询问卷信息
            if (response.getSurveyId() != null) {
                Survey survey = surveyService.getById(response.getSurveyId());
                if (survey != null) {
                    vo.setSurveyTitle(survey.getTitle());
                    vo.setPublisherId(survey.getUserId());
                    
                    // 查询发布用户信息
                    if (survey.getUserId() != null) {
                        User publisher = userMapper.selectById(survey.getUserId());
                        if (publisher != null) {
                            vo.setPublisherName(publisher.getUsername());
                        }
                    }
                }
            }
            
            // 查询填写用户信息
            if (response.getUserId() != null) {
                User user = userMapper.selectById(response.getUserId());
                if (user != null) {
                    vo.setUserName(user.getUsername());
                }
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        // 应用筛选条件（问卷名称、发布用户名称、填写用户名称）
        if (surveyTitle != null && !surveyTitle.isEmpty()) {
            voList = voList.stream()
                .filter(vo -> vo.getSurveyTitle() != null && vo.getSurveyTitle().contains(surveyTitle))
                .collect(Collectors.toList());
        }
        if (publisherName != null && !publisherName.isEmpty()) {
            voList = voList.stream()
                .filter(vo -> vo.getPublisherName() != null && vo.getPublisherName().contains(publisherName))
                .collect(Collectors.toList());
        }
        if (userName != null && !userName.isEmpty()) {
            voList = voList.stream()
                .filter(vo -> vo.getUserName() != null && vo.getUserName().contains(userName))
                .collect(Collectors.toList());
        }
        
        // 手动分页
        int total = voList.size();
        int start = (int) ((page.getCurrent() - 1) * page.getSize());
        int end = Math.min(start + (int) page.getSize(), total);
        List<ResponseVO> pagedList = start < total ? voList.subList(start, end) : new ArrayList<>();
        
        Page<ResponseVO> voPage = new Page<>(page.getCurrent(), page.getSize(), total);
        voPage.setRecords(pagedList);
        return voPage;
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

