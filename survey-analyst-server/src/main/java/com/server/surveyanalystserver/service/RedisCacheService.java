package com.server.surveyanalystserver.service;

import java.util.Map;

/**
 * Redis缓存服务接口
 */
public interface RedisCacheService {

    /**
     * 设置缓存
     * 将数据存储到Redis缓存中，可设置过期时间
     * @param key 缓存键
     * @param value 缓存值（对象会自动序列化为JSON）
     * @param timeout 过期时间（秒），如果为null则不设置过期时间（永久有效）
     */
    void set(String key, Object value, Long timeout);

    /**
     * 获取缓存
     * 从Redis缓存中获取数据，返回Object类型
     * @param key 缓存键
     * @return 缓存值，如果不存在或已过期则返回null
     */
    Object get(String key);

    /**
     * 获取缓存并转换为指定类型
     * 从Redis缓存中获取数据并自动转换为指定类型
     * @param key 缓存键
     * @param clazz 目标类型Class对象
     * @param <T> 泛型类型
     * @return 转换后的缓存值，如果不存在或转换失败则返回null
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 删除缓存
     * 从Redis中删除指定的缓存数据
     * @param key 缓存键
     */
    void delete(String key);

    /**
     * 批量删除缓存（支持通配符）
     * 根据模式批量删除匹配的缓存数据，常用于清除相关缓存
     * @param pattern 缓存键模式，支持通配符，如 "statistics:survey:*" 表示删除所有以该前缀开头的缓存
     */
    void deleteByPattern(String pattern);

    /**
     * 判断缓存是否存在
     * 检查指定键的缓存数据是否存在且未过期
     * @param key 缓存键
     * @return true表示缓存存在，false表示不存在或已过期
     */
    boolean exists(String key);

    /**
     * 设置缓存过期时间
     * 为已存在的缓存设置或更新过期时间
     * @param key 缓存键
     * @param timeout 过期时间（秒）
     */
    void expire(String key, long timeout);

    /**
     * 获取缓存并转换为Map类型
     * 从Redis缓存中获取数据并转换为Map类型，常用于获取复杂对象
     * @param key 缓存键
     * @return Map类型的缓存值，如果不存在或转换失败则返回null
     */
    Map<String, Object> getMap(String key);
}

