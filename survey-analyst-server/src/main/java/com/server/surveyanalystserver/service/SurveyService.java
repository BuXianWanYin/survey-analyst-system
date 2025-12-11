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
     * 分页查询问卷列表（包含答卷数量）
     * @param page 分页参数
     * @param userId 用户ID
     * @return 问卷分页列表（已填充答卷数量）
     */
    Page<Survey> getSurveyListWithResponseCount(Page<Survey> page, Long userId);

    /**
     * 发布问卷
     * @param id 问卷ID
     * @return 是否成功
     */
    boolean publishSurvey(Long id);
    
    /**
     * 取消发布问卷（改为草稿）
     * @param id 问卷ID
     * @return 是否成功
     */
    boolean unpublishSurvey(Long id);

    /**
     * 删除问卷（逻辑删除）
     * @param id 问卷ID
     * @return 是否成功
     */
    boolean deleteSurvey(Long id);

    /**
     * 验证问卷访问密码
     * @param id 问卷ID
     * @param password 访问密码
     * @return 是否验证通过
     */
    boolean verifyPassword(Long id, String password);
}

