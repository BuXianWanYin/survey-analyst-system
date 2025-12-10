package com.server.surveyanalystserver.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.surveyanalystserver.entity.Response;

import java.util.Map;

/**
 * 填写记录Service接口
 */
public interface ResponseService extends IService<Response> {

    /**
     * 提交填写记录
     * @param response 填写记录
     * @param answers 答案列表（questionId -> answer）
     * @return 提交的填写记录
     */
    Response submitResponse(Response response, Map<Long, Object> answers);

    /**
     * 保存草稿
     * @param response 填写记录
     * @param answers 答案列表
     * @return 保存的草稿
     */
    Response saveDraft(Response response, Map<Long, Object> answers);

    /**
     * 获取填写记录详情
     * @param id 填写记录ID
     * @return 填写记录详情
     */
    Response getResponseById(Long id);

    /**
     * 分页查询填写记录
     * @param page 分页参数
     * @param surveyId 问卷ID
     * @return 填写记录分页列表
     */
    Page<Response> getResponseList(Page<Response> page, Long surveyId);

    /**
     * 获取问卷填写数量
     * @param surveyId 问卷ID
     * @return 填写数量
     */
    long getResponseCount(Long surveyId);

    /**
     * 分页查询填写记录（包含问卷名称、发布用户名称、填写用户名称）
     * @param page 分页参数
     * @param surveyId 问卷ID（可选）
     * @param surveyTitle 问卷名称（可选，模糊查询）
     * @param publisherName 发布用户名称（可选，模糊查询）
     * @param userName 填写用户名称（可选，模糊查询）
     * @return 填写记录分页列表（包含关联信息）
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.server.surveyanalystserver.entity.dto.ResponseVO> getResponseListWithDetails(
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.server.surveyanalystserver.entity.dto.ResponseVO> page,
            Long surveyId, String surveyTitle, String publisherName, String userName);
}

