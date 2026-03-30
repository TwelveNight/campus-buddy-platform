package com.example.campusbuddy.service;

import com.example.campusbuddy.entity.User;

/**
 * 用户缓存服务接口
 */
public interface UserCacheService {

    void cacheUser(User user);

    User getCachedUser(Long userId);

    void evictUserCache(Long userId);

    void flushAllCache();
}
