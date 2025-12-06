package com.server.surveyanalystserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.server.surveyanalystserver.entity.Option;

import java.util.List;

/**
 * 选项Service接口
 */
public interface OptionService extends IService<Option> {

    /**
     * 添加选项
     * @param option 选项信息
     * @return 创建的选项
     */
    Option addOption(Option option);

    /**
     * 更新选项
     * @param option 选项信息
     * @return 更新后的选项
     */
    Option updateOption(Option option);

    /**
     * 删除选项
     * @param id 选项ID
     * @return 是否成功
     */
    boolean deleteOption(Long id);

    /**
     * 获取题目的所有选项
     * @param questionId 题目ID
     * @return 选项列表
     */
    List<Option> getOptionsByQuestionId(Long questionId);
}

