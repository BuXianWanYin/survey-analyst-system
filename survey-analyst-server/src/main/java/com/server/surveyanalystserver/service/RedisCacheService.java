package com.server.surveyanalystserver.service;

import java.util.Map;

/**
 * Redis缓存服务接口
 */
public interface RedisCacheService {

    /**
     * 设置缓存
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间（秒），如果为null则不设置过期时间
     */
    void set(String key, Object value, Long timeout);

    /**
     * 获取缓存
     * @param key 缓存键
     * @return 缓存值，如果不存在返回null
     */
    Object get(String key);

    /**
     * 获取缓存并转换为指定类型
     * @param key 缓存键
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 缓存值，如果不存在返回null
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 删除缓存
     * @param key 缓存键
     */
    void delete(String key);

    /**
     * 批量删除缓存（支持通配符）
     * @param pattern 缓存键模式，如 "statistics:survey:*"
     */
    void deleteByPattern(String pattern);

    /**
     * 判断缓存是否存在
     * @param key 缓存键
     * @return 是否存在
     */
    boolean exists(String key);

    /**
     * 设置过期时间
     * @param key 缓存键
     * @param timeout 过期时间（秒）
     */
    void expire(String key, long timeout);

    /**
     * 获取缓存并转换为Map类型
     * @param key 缓存键
     * @return Map类型的缓存值，如果不存在返回null
     */
    Map<String, Object> getMap(String key);
}

