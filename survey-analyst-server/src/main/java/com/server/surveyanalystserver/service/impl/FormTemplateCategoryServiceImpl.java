package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.FormTemplateCategory;
import com.server.surveyanalystserver.mapper.FormTemplateCategoryMapper;
import com.server.surveyanalystserver.service.FormTemplateCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public List<FormTemplateCategory> listByUserId(Long userId) {
        LambdaQueryWrapper<FormTemplateCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormTemplateCategory::getIsDeleted, 0);
        // 查询系统分类（user_id为NULL）或当前用户的分类
        wrapper.and(w -> w.isNull(FormTemplateCategory::getUserId).or().eq(FormTemplateCategory::getUserId, userId));
        wrapper.orderByDesc(FormTemplateCategory::getSort);
        return this.list(wrapper);
    }

    @Override
    public List<FormTemplateCategory> listSystemCategories() {
        LambdaQueryWrapper<FormTemplateCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormTemplateCategory::getIsDeleted, 0);
        wrapper.isNull(FormTemplateCategory::getUserId);
        wrapper.orderByDesc(FormTemplateCategory::getSort);
        return this.list(wrapper);
    }
}

