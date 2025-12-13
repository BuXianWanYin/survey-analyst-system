package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.FormConfig;
import com.server.surveyanalystserver.entity.FormData;
import com.server.surveyanalystserver.entity.Response;
import com.server.surveyanalystserver.entity.Survey;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.entity.dto.ResponseVO;
import com.server.surveyanalystserver.mapper.ResponseMapper;
import com.server.surveyanalystserver.mapper.UserMapper;
import com.server.surveyanalystserver.mapper.SurveyMapper;
import com.server.surveyanalystserver.service.FormConfigService;
import com.server.surveyanalystserver.service.FormDataService;
import com.server.surveyanalystserver.service.ResponseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 填写记录Service实现类
 */
@Slf4j
@Service
public class ResponseServiceImpl extends ServiceImpl<ResponseMapper, Response> implements ResponseService {

    @Autowired
    private FormConfigService formConfigService;
    
    @Autowired
    private FormDataService formDataService;
    
    @Autowired
    private SurveyMapper surveyMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 根据ID获取填写记录详情
     * @param id 填写记录ID
     * @return 填写记录对象，如果不存在则返回null
     */
    @Override
    public Response getResponseById(Long id) {
        return this.getById(id);
    }

    /**
     * 分页查询填写记录列表
     * 查询指定问卷的所有填写记录，按创建时间降序排列
     * @param page 分页参数
     * @param surveyId 问卷ID
     * @return 填写记录分页列表
     */
    @Override
    public Page<Response> getResponseList(Page<Response> page, Long surveyId) {
        LambdaQueryWrapper<Response> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Response::getSurveyId, surveyId);
        wrapper.orderByDesc(Response::getCreateTime);
        return this.page(page, wrapper);
    }

    /**
     * 获取问卷的填写数量
     * 优先通过formKey统计form_data表的数据，如果没有formConfig则回退到统计response表
     * @param surveyId 问卷ID
     * @return 填写数量
     */
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

    /**
     * 分页查询填写记录列表（包含详细信息）
     * 支持多条件筛选（问卷ID、问卷标题、发布用户、填写用户等），返回包含问卷标题、发布用户、填写用户等详细信息的VO列表
     * @param page 分页参数
     * @param surveyId 问卷ID，可选
     * @param surveyTitle 问卷标题（模糊匹配），可选
     * @param publisherName 发布用户名称（模糊匹配），可选
     * @param publisherId 发布用户ID，可选
     * @param userName 填写用户名称（模糊匹配），可选
     * @return 填写记录分页列表（包含详细信息）
     */
    @Override
    public Page<ResponseVO> getResponseListWithDetails(Page<ResponseVO> page, Long surveyId, 
            String surveyTitle, String publisherName, Long publisherId, String userName) {
        // 先通过数据库层面筛选符合条件的问卷ID
        List<Long> surveyIdList = null;
        
        // 如果有问卷标题、发布用户名称或发布用户ID筛选，先查询符合条件的问卷
        if ((surveyTitle != null && !surveyTitle.isEmpty()) || (publisherName != null && !publisherName.isEmpty()) || publisherId != null) {
            LambdaQueryWrapper<Survey> surveyWrapper = new LambdaQueryWrapper<>();
            
            if (surveyId != null) {
                surveyWrapper.eq(Survey::getId, surveyId);
            }
            
            if (surveyTitle != null && !surveyTitle.isEmpty()) {
                surveyWrapper.like(Survey::getTitle, surveyTitle);
            }
            
            if (publisherId != null) {
                // 通过发布用户ID筛选
                surveyWrapper.eq(Survey::getUserId, publisherId);
            } else if (publisherName != null && !publisherName.isEmpty()) {
                // 先查询符合条件的用户ID
                LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
                userWrapper.like(User::getUsername, publisherName);
                List<User> users = userMapper.selectList(userWrapper);
                if (users.isEmpty()) {
                    // 如果没有匹配的用户，返回空结果
                    Page<ResponseVO> emptyPage = new Page<>(page.getCurrent(), page.getSize(), 0);
                    emptyPage.setRecords(new ArrayList<>());
                    return emptyPage;
                }
                List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
                surveyWrapper.in(Survey::getUserId, userIds);
            }
            
            List<Survey> surveys = surveyMapper.selectList(surveyWrapper);
            surveyIdList = surveys.stream().map(Survey::getId).collect(Collectors.toList());
            
            if (surveyIdList.isEmpty()) {
                // 如果没有匹配的问卷，返回空结果
                Page<ResponseVO> emptyPage = new Page<>(page.getCurrent(), page.getSize(), 0);
                emptyPage.setRecords(new ArrayList<>());
                return emptyPage;
            }
        } else if (surveyId != null) {
            surveyIdList = new ArrayList<>();
            surveyIdList.add(surveyId);
        }
        
        // 查询Response
        LambdaQueryWrapper<Response> wrapper = new LambdaQueryWrapper<>();
        
        if (surveyIdList != null && !surveyIdList.isEmpty()) {
            wrapper.in(Response::getSurveyId, surveyIdList);
        }
        
        // 如果有填写用户名称筛选，先查询符合条件的用户ID
        if (userName != null && !userName.isEmpty()) {
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.like(User::getUsername, userName);
            List<User> users = userMapper.selectList(userWrapper);
            if (users.isEmpty()) {
                // 如果没有匹配的用户，返回空结果
                Page<ResponseVO> emptyPage = new Page<>(page.getCurrent(), page.getSize(), 0);
                emptyPage.setRecords(new ArrayList<>());
                return emptyPage;
            }
            List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            wrapper.in(Response::getUserId, userIds);
        }
        
        wrapper.orderByDesc(Response::getCreateTime);
        Page<Response> responsePage = new Page<>(page.getCurrent(), page.getSize());
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
                Survey survey = surveyMapper.selectById(response.getSurveyId());
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
        
        Page<ResponseVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 获取数据统计概览
     * 统计所有填写记录的总数、已完成数、草稿数
     * @return 统计数据Map，包含totalResponses、completedResponses、draftResponses等字段
     */
    @Override
    public Map<String, Object> getDataStatistics() {
        long totalResponses = this.count();
        long completedResponses = this.count(new LambdaQueryWrapper<Response>().eq(Response::getStatus, "COMPLETED"));
        long draftResponses = this.count(new LambdaQueryWrapper<Response>().eq(Response::getStatus, "DRAFT"));
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalResponses", totalResponses);
        statistics.put("completedResponses", completedResponses);
        statistics.put("draftResponses", draftResponses);
        
        return statistics;
    }
}

