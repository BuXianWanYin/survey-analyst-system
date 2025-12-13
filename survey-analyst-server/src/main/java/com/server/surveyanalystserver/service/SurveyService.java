package com.server.surveyanalystserver.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.surveyanalystserver.entity.Survey;
import com.server.surveyanalystserver.entity.dto.SurveyVO;

import java.util.Map;

/**
 * 问卷Service接口
 */
public interface SurveyService extends IService<Survey> {

    /**
     * 创建新问卷
     * 创建问卷并设置创建时间和创建用户
     * @param survey 问卷信息对象，必须包含title和userId
     * @return 创建成功后的问卷对象（包含生成的ID）
     */
    Survey createSurvey(Survey survey);

    /**
     * 更新问卷信息
     * 更新问卷的基本信息，不包括状态和发布相关字段
     * @param survey 包含更新信息的问卷对象，必须包含id
     * @return 更新后的问卷对象
     */
    Survey updateSurvey(Survey survey);

    /**
     * 根据ID获取问卷详情
     * 查询问卷的完整信息，包括关联的表单配置等
     * @param id 问卷ID
     * @return 问卷详情对象，如果不存在则返回null
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

    /**
     * 管理员分页查询问卷列表（包含用户名）
     * @param page 分页参数
     * @param keyword 关键词（可选）
     * @param status 状态（可选）
     * @param userId 用户ID（可选）
     * @return 问卷分页列表（包含用户名）
     */
    Page<SurveyVO> getAdminSurveyList(Page<SurveyVO> page, String keyword, String status, Long userId);

    /**
     * 更新问卷状态
     * @param id 问卷ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateSurveyStatus(Long id, String status);

    /**
     * 获取问卷统计数据
     * @return 统计数据
     */
    Map<String, Object> getSurveyStatistics();
}

