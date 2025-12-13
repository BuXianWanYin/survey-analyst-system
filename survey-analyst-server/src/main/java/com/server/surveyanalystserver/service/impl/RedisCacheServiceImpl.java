package com.server.surveyanalystserver.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.surveyanalystserver.service.RedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存服务实现类
 */
@Service
public class RedisCacheServiceImpl implements RedisCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 设置缓存
     * 将数据存储到Redis缓存中，可设置过期时间
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间（秒），如果为null或小于等于0则不设置过期时间
     */
    @Override
    public void set(String key, Object value, Long timeout) {
        if (timeout != null && timeout > 0) {
            redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    /**
     * 获取缓存
     * 从Redis缓存中获取数据（返回Object类型）
     * @param key 缓存键
     * @return 缓存值，如果不存在则返回null
     */
    @Override
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取缓存（指定类型）
     * 从Redis缓存中获取数据并转换为指定类型
     * @param key 缓存键
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 转换后的缓存值，如果不存在或转换失败则返回null
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        
        // 如果已经是目标类型，直接返回
        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        }
        
        // 如果是Map类型，尝试转换为目标类型
        if (value instanceof Map) {
            try {
                return objectMapper.convertValue(value, clazz);
            } catch (Exception e) {
                // 转换失败，返回null
                return null;
            }
        }
        
        // 其他情况，尝试JSON转换
        try {
            String json = objectMapper.writeValueAsString(value);
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 删除缓存
     * 从Redis缓存中删除指定键的数据
     * @param key 缓存键
     */
    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 按模式删除缓存
     * 删除匹配指定模式的所有缓存键
     * @param pattern 键模式（支持通配符，如"user:*"）
     */
    @Override
    public void deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 检查缓存是否存在
     * 检查指定键是否存在于Redis缓存中
     * @param key 缓存键
     * @return true表示存在，false表示不存在
     */
    @Override
    public boolean exists(String key) {
        Boolean result = redisTemplate.hasKey(key);
        return result != null && result;
    }

    /**
     * 设置缓存过期时间
     * 为指定键设置过期时间
     * @param key 缓存键
     * @param timeout 过期时间（秒）
     */
    @Override
    public void expire(String key, long timeout) {
        redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 获取缓存（Map类型）
     * 从Redis缓存中获取数据并转换为Map类型
     * @param key 缓存键
     * @return Map类型的缓存值，如果不存在或转换失败则返回null
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMap(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        
        // 尝试转换为Map
        try {
            return objectMapper.convertValue(value, Map.class);
        } catch (Exception e) {
            return null;
        }
    }
}

