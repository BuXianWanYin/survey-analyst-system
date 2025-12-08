package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.FormTemplateCategory;
import com.server.surveyanalystserver.mapper.FormTemplateCategoryMapper;
import com.server.surveyanalystserver.service.FormTemplateCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 表单模板分类服务实现类
 */
@Service
public class FormTemplateCategoryServiceImpl extends ServiceImpl<FormTemplateCategoryMapper, FormTemplateCategory> implements FormTemplateCategoryService {

    @Override
    public List<FormTemplateCategory> listAll() {
        LambdaQueryWrapper<FormTemplateCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormTemplateCategory::getIsDeleted, 0);
        wrapper.orderByDesc(FormTemplateCategory::getSort);
        return this.list(wrapper);
    }
}

