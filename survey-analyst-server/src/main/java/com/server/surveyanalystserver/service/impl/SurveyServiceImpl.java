package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.FormConfig;
import com.server.surveyanalystserver.entity.FormData;
import com.server.surveyanalystserver.entity.Survey;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.entity.dto.SurveyVO;
import com.server.surveyanalystserver.mapper.FormDataMapper;
import com.server.surveyanalystserver.mapper.SurveyMapper;
import com.server.surveyanalystserver.mapper.UserMapper;
import com.server.surveyanalystserver.entity.Response;
import com.server.surveyanalystserver.mapper.ResponseMapper;
import org.springframework.beans.BeanUtils;
import com.server.surveyanalystserver.service.FormConfigService;
import com.server.surveyanalystserver.service.FormItemService;
import com.server.surveyanalystserver.service.FormLogicService;
import com.server.surveyanalystserver.service.FormSettingService;
import com.server.surveyanalystserver.service.FormThemeService;
import com.server.surveyanalystserver.service.SurveyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 问卷Service实现类
 */
@Service
public class SurveyServiceImpl extends ServiceImpl<SurveyMapper, Survey> implements SurveyService {
    
    @Autowired
    private FormConfigService formConfigService;
    
    @Autowired
    private FormItemService formItemService;
    
    @Autowired
    private FormSettingService formSettingService;
    
    @Autowired
    private FormLogicService formLogicService;
    
    @Autowired
    private FormThemeService formThemeService;
    
    @Autowired
    private FormDataMapper formDataMapper;
    
    @Autowired
    private ResponseMapper responseMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 创建新问卷
     * 创建问卷并设置状态为草稿（DRAFT）
     * @param survey 问卷信息对象，必须包含title和userId
     * @return 创建成功后的问卷对象（包含生成的ID）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Survey createSurvey(Survey survey) {
        survey.setStatus("DRAFT");
        this.save(survey);
        return survey;
    }

    /**
     * 更新问卷信息
     * 更新问卷的基本信息，不包括状态和发布相关字段
     * @param survey 包含更新信息的问卷对象，必须包含id
     * @return 更新后的问卷对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Survey updateSurvey(Survey survey) {
        this.updateById(survey);
        return this.getById(survey.getId());
    }

    /**
     * 根据ID获取问卷详情
     * 查询问卷的完整信息
     * @param id 问卷ID
     * @return 问卷详情对象，如果不存在则返回null
     */
    @Override
    public Survey getSurveyById(Long id) {
        return this.getById(id);
    }

    /**
     * 分页查询问卷列表
     * 查询指定用户创建的所有问卷，按创建时间降序排列
     * @param page 分页参数
     * @param userId 用户ID
     * @return 问卷分页列表
     */
    @Override
    public Page<Survey> getSurveyList(Page<Survey> page, Long userId) {
        LambdaQueryWrapper<Survey> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Survey::getUserId, userId);
        wrapper.orderByDesc(Survey::getCreateTime);
        return this.page(page, wrapper);
    }
    
    /**
     * 分页查询问卷列表（包含答卷数量）
     * 查询指定用户创建的所有问卷，并为每个问卷填充答卷数量统计
     * @param page 分页参数
     * @param userId 用户ID
     * @return 问卷分页列表（已填充答卷数量）
     */
    @Override
    public Page<Survey> getSurveyListWithResponseCount(Page<Survey> page, Long userId) {
        Page<Survey> result = getSurveyList(page, userId);
        // 为每个问卷设置答卷数量
        result.getRecords().forEach(survey -> {
            long responseCount = getResponseCountBySurveyId(survey.getId());
            survey.setResponseCount(responseCount);
        });
        return result;
    }
    
    /**
     * 获取问卷的答卷数量
     * 通过formKey统计form_data表的数据，如果没有formConfig则回退到统计response表
     * 此方法直接使用Mapper，避免循环依赖
     * @param surveyId 问卷ID
     * @return 答卷数量
     */
    private long getResponseCountBySurveyId(Long surveyId) {
        // 通过 surveyId 获取 formKey
        FormConfig formConfig = formConfigService.getBySurveyId(surveyId);
        if (formConfig == null || formConfig.getFormKey() == null) {
            // 如果没有 formConfig，回退到统计 response 表
            LambdaQueryWrapper<Response> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Response::getSurveyId, surveyId);
            wrapper.ne(Response::getStatus, "DRAFT");
            return responseMapper.selectCount(wrapper);
        }
        
        // 统计 form_data 表的数据
        LambdaQueryWrapper<FormData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormData::getFormKey, formConfig.getFormKey());
        return formDataMapper.selectCount(wrapper);
    }

    /**
     * 发布问卷
     * 将问卷状态设置为已发布（PUBLISHED），发布前检查表单配置和表单项是否存在
     * @param id 问卷ID
     * @return true表示发布成功，false表示发布失败
     * @throws RuntimeException 如果问卷不存在、表单配置不存在或没有表单项则抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishSurvey(Long id) {
        Survey survey = this.getById(id);
        if (survey == null) {
            throw new RuntimeException("问卷不存在");
        }
        
        // 检查是否有表单项
        FormConfig formConfig = formConfigService.getBySurveyId(id);
        if (formConfig == null) {
            throw new RuntimeException("表单配置不存在，无法发布");
        }
        
        long itemCount = formItemService.countByFormKey(formConfig.getFormKey());
        if (itemCount == 0) {
            throw new RuntimeException("无有效表单项，无法发布");
        }
        
        survey.setStatus("PUBLISHED");
        return this.updateById(survey);
    }
    
    /**
     * 取消发布问卷
     * 将问卷状态设置为已结束（ENDED），停止收集数据
     * @param id 问卷ID
     * @return true表示取消发布成功，false表示失败
     * @throws RuntimeException 如果问卷不存在则抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unpublishSurvey(Long id) {
        Survey survey = this.getById(id);
        if (survey == null) {
            throw new RuntimeException("问卷不存在");
        }
        survey.setStatus("ENDED");
        return this.updateById(survey);
    }

    /**
     * 删除问卷（逻辑删除）
     * 删除问卷及其关联的所有数据（表单配置、表单项、表单逻辑、表单设置、表单主题等）
     * @param id 问卷ID
     * @return true表示删除成功，false表示删除失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSurvey(Long id) {
        // 逻辑外键联动删除：删除问卷时，删除相关的所有数据
        // 1. 查找并删除 form_config
        FormConfig formConfig = formConfigService.getBySurveyId(id);
        if (formConfig != null) {
            String formKey = formConfig.getFormKey();
            
            // 2. 删除 form_item（通过 formKey）
            formItemService.deleteByFormKey(formKey);
            
            // 3. 删除 form_data（通过 formKey，直接使用Mapper避免循环依赖）
            LambdaQueryWrapper<FormData> dataWrapper = new LambdaQueryWrapper<>();
            dataWrapper.eq(FormData::getFormKey, formKey);
            formDataMapper.delete(dataWrapper);
            
            // 4. 删除 form_config
            formConfigService.deleteById(formConfig.getId());
        }
        
        // 5. 删除 form_setting（通过 surveyId）
        formSettingService.deleteBySurveyId(id);
        
        // 6. 删除 form_logic（通过 surveyId）
        formLogicService.deleteBySurveyId(id);
        
        // 7. 删除 form_theme（通过 surveyId）
        formThemeService.deleteBySurveyId(id);
        
        // 8. 删除问卷（逻辑删除）
        return this.removeById(id);
    }

    /**
     * 验证问卷访问密码
     * 验证用户输入的密码是否与问卷设置的访问密码匹配
     * @param id 问卷ID
     * @param password 用户输入的密码
     * @return true表示密码正确，false表示密码错误或问卷不是密码访问类型
     */
    @Override
    public boolean verifyPassword(Long id, String password) {
        Survey survey = this.getById(id);
        if (survey == null) {
            return false;
        }
        
        // 如果问卷不是密码访问类型，返回false
        if (!"PASSWORD".equals(survey.getAccessType())) {
            return false;
        }
        
        // 验证密码
        if (survey.getPassword() == null || survey.getPassword().isEmpty()) {
            return false;
        }
        
        return survey.getPassword().equals(password);
    }

    /**
     * 管理员分页查询问卷列表（包含用户名）
     * 支持按关键词、状态、用户ID筛选问卷，返回包含用户名的问卷列表
     * @param page 分页参数
     * @param keyword 关键词，用于搜索问卷标题，可选
     * @param status 问卷状态（DRAFT-草稿，PUBLISHED-已发布），可选
     * @param userId 用户ID，可选
     * @return 问卷分页列表（包含用户名）
     */
    @Override
    public Page<SurveyVO> getAdminSurveyList(Page<SurveyVO> page, String keyword, String status, Long userId) {
        LambdaQueryWrapper<Survey> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Survey::getTitle, keyword);
        }
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Survey::getStatus, status);
        }
        
        if (userId != null) {
            wrapper.eq(Survey::getUserId, userId);
        }
        
        wrapper.orderByDesc(Survey::getCreateTime);
        Page<Survey> surveyPage = new Page<>(page.getCurrent(), page.getSize());
        Page<Survey> result = this.page(surveyPage, wrapper);
        
        Page<SurveyVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<SurveyVO> voList = result.getRecords().stream().map(survey -> {
            SurveyVO vo = new SurveyVO();
            BeanUtils.copyProperties(survey, vo);
            if (survey.getUserId() != null) {
                User user = userMapper.selectById(survey.getUserId());
                if (user != null) {
                    vo.setUsername(user.getUsername());
                }
            }
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }

    /**
     * 更新问卷状态
     * 更新指定问卷的状态（DRAFT-草稿，PUBLISHED-已发布，ENDED-已结束）
     * @param id 问卷ID
     * @param status 新状态
     * @return true表示更新成功，false表示更新失败
     * @throws RuntimeException 如果问卷不存在则抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSurveyStatus(Long id, String status) {
        Survey survey = this.getById(id);
        if (survey == null) {
            throw new RuntimeException("问卷不存在");
        }
        survey.setStatus(status);
        return this.updateById(survey);
    }

    /**
     * 获取问卷统计信息
     * 统计系统中的问卷总数、草稿数、已发布数、已结束数
     * @return 统计数据Map，包含totalSurveys、draftSurveys、publishedSurveys、endedSurveys字段
     */
    @Override
    public Map<String, Object> getSurveyStatistics() {
        long totalSurveys = this.count();
        long draftSurveys = this.count(new LambdaQueryWrapper<Survey>().eq(Survey::getStatus, "DRAFT"));
        long publishedSurveys = this.count(new LambdaQueryWrapper<Survey>().eq(Survey::getStatus, "PUBLISHED"));
        long endedSurveys = this.count(new LambdaQueryWrapper<Survey>().eq(Survey::getStatus, "ENDED"));
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalSurveys", totalSurveys);
        statistics.put("draftSurveys", draftSurveys);
        statistics.put("publishedSurveys", publishedSurveys);
        statistics.put("endedSurveys", endedSurveys);
        
        return statistics;
    }
}

