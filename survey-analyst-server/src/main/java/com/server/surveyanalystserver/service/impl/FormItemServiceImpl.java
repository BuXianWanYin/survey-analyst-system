package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.surveyanalystserver.entity.FormItem;
import com.server.surveyanalystserver.mapper.FormItemMapper;
import com.server.surveyanalystserver.service.FormItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByFormKey(String formKey) {
        LambdaQueryWrapper<FormItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormItem::getFormKey, formKey);
        this.remove(wrapper);
    }
    
    @Override
    public long countByFormKey(String formKey) {
        LambdaQueryWrapper<FormItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FormItem::getFormKey, formKey);
        return this.count(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveFromMap(String formKey, List<Map<String, Object>> itemsData) {
        if (itemsData == null || itemsData.isEmpty()) {
            return;
        }
        
        List<FormItem> items = itemsData.stream().map(itemData -> {
            FormItem item = new FormItem();
            item.setFormKey(formKey);
            item.setFormItemId((String) itemData.get("formItemId"));
            item.setType((String) itemData.get("type"));
            item.setLabel((String) itemData.get("label"));
            item.setRequired((Integer) itemData.get("required"));
            item.setPlaceholder((String) itemData.get("placeholder"));
            
            // 处理 sort 字段
            Object sortObj = itemData.get("sort");
            if (sortObj instanceof Number) {
                item.setSort(((Number) sortObj).longValue());
            } else if (sortObj != null) {
                try {
                    item.setSort(Long.parseLong(sortObj.toString()));
                } catch (NumberFormatException e) {
                    item.setSort(0L);
                }
            } else {
                item.setSort(0L);
            }
            
            item.setScheme((String) itemData.get("scheme"));
            
            // 设置 span（栅格宽度）
            Object spanObj = itemData.get("span");
            if (spanObj != null) {
                if (spanObj instanceof Number) {
                    item.setSpan(((Number) spanObj).intValue());
                } else if (spanObj instanceof String) {
                    try {
                        item.setSpan(Integer.parseInt((String) spanObj));
                    } catch (NumberFormatException e) {
                        item.setSpan(24); // 默认值
                    }
                } else {
                    item.setSpan(24); // 默认值
                }
            } else {
                item.setSpan(24); // 默认值
            }
            
            // 设置 regList（正则验证规则）
            Object regListObj = itemData.get("regList");
            if (regListObj != null) {
                if (regListObj instanceof String) {
                    item.setRegList((String) regListObj);
                } else {
                    // 如果是对象，转换为JSON字符串
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        item.setRegList(objectMapper.writeValueAsString(regListObj));
                    } catch (Exception e) {
                        // 忽略转换错误
                        item.setRegList(null);
                    }
                }
            }
            
            // 设置 isHideType（隐藏类型）
            Object hideTypeObj = itemData.get("isHideType");
            if (hideTypeObj != null) {
                if (hideTypeObj instanceof Number) {
                    item.setIsHideType(((Number) hideTypeObj).intValue());
                } else if (hideTypeObj instanceof Boolean) {
                    item.setIsHideType((Boolean) hideTypeObj ? 1 : 0);
                } else {
                    item.setIsHideType(0);
                }
            } else {
                item.setIsHideType(0);
            }
            
            return item;
        }).collect(Collectors.toList());
        
        // 调用批量保存方法
        batchSave(formKey, items);
    }
}

