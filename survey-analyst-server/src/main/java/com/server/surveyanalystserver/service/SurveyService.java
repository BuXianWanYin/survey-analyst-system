package com.server.surveyanalystserver.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.surveyanalystserver.entity.Survey;

/**
 * 问卷Service接口
 */
public interface SurveyService extends IService<Survey> {

    /**
     * 创建问卷
     * @param survey 问卷信息
     * @return 创建的问卷
     */
    Survey createSurvey(Survey survey);

    /**
     * 更新问卷
     * @param survey 问卷信息
     * @return 更新后的问卷
     */
    Survey updateSurvey(Survey survey);

    /**
     * 获取问卷详情
     * @param id 问卷ID
     * @return 问卷详情
     */
    Survey getSurveyById(Long id);

    /**
     * 分页查询问卷列表
     * @param page 分页参数
     * @param userId 用户ID
     * @return 问卷分页列表
     */
    Page<Survey> getSurveyList(Page<Survey> page, Long userId);

    /**
     * 发布问卷
     * @param id 问卷ID
     * @return 是否成功
     */
    boolean publishSurvey(Long id);
    
    /**
     * 停止发布问卷
     * @param id 问卷ID
     * @return 是否成功
     */
    boolean stopPublishSurvey(Long id);

    /**
     * 删除问卷（逻辑删除）
     * @param id 问卷ID
     * @return 是否成功
     */
    boolean deleteSurvey(Long id);
}

