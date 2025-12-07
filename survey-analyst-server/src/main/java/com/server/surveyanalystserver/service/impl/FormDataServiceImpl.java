package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.FormData;
import com.server.surveyanalystserver.mapper.FormDataMapper;
import com.server.surveyanalystserver.service.FormDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 表单数据服务实现类
 */
@Service
public class FormDataServiceImpl extends ServiceImpl<FormDataMapper, FormData> implements FormDataService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FormData saveFormData(String formKey, Map<String, Object> originalData) {
        FormData formData = new FormData();
        formData.setFormKey(formKey);
        formData.setOriginalData(originalData);
        
        // 计算序号
        LambdaQueryWrapper<FormData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormData::getFormKey, formKey);
        long count = this.count(wrapper);
        formData.setSerialNumber((int) (count + 1));
        
        this.save(formData);
        return formData;
    }
    
    @Override
    public Page<FormData> getFormDataList(Page<FormData> page, String formKey) {
        LambdaQueryWrapper<FormData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormData::getFormKey, formKey);
        wrapper.orderByDesc(FormData::getCreateTime);
        return this.page(page, wrapper);
    }
    
    @Override
    public FormData getFormDataById(Long id) {
        return this.getById(id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFormData(Long id) {
        return this.removeById(id);
    }
}

