package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.Option;
import com.server.surveyanalystserver.mapper.OptionMapper;
import com.server.surveyanalystserver.service.OptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 选项Service实现类
 */
@Service
public class OptionServiceImpl extends ServiceImpl<OptionMapper, Option> implements OptionService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Option addOption(Option option) {
        this.save(option);
        return option;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Option updateOption(Option option) {
        this.updateById(option);
        return this.getById(option.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOption(Long id) {
        return this.removeById(id);
    }

    @Override
    public List<Option> getOptionsByQuestionId(Long questionId) {
        LambdaQueryWrapper<Option> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Option::getQuestionId, questionId);
        wrapper.orderByAsc(Option::getOrderNum);
        return this.list(wrapper);
    }
}

