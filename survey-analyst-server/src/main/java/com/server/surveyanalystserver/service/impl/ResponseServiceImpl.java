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
import com.server.surveyanalystserver.mapper.user.UserMapper;
import com.server.surveyanalystserver.service.FormConfigService;
import com.server.surveyanalystserver.service.FormDataService;
import com.server.surveyanalystserver.service.ResponseService;
import com.server.surveyanalystserver.service.SurveyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
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
    private SurveyService surveyService;

    @Autowired
    private UserMapper userMapper;

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
}

