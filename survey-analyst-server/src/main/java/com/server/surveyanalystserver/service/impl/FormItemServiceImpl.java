package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.FormItem;
import com.server.surveyanalystserver.mapper.FormItemMapper;
import com.server.surveyanalystserver.service.FormItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 表单项服务实现类
 */
@Service
public class FormItemServiceImpl extends ServiceImpl<FormItemMapper, FormItem> implements FormItemService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(String formKey, List<FormItem> items) {
        // 先删除该 formKey 下的所有表单项
        LambdaQueryWrapper<FormItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormItem::getFormKey, formKey);
        this.remove(wrapper);
        
        // 批量插入新的表单项
        if (items != null && !items.isEmpty()) {
            this.saveBatch(items);
        }
    }
    
    @Override
    public List<FormItem> getByFormKey(String formKey) {
        LambdaQueryWrapper<FormItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormItem::getFormKey, formKey);
        wrapper.orderByAsc(FormItem::getSort);
        return this.list(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        this.removeById(id);
    }
}

