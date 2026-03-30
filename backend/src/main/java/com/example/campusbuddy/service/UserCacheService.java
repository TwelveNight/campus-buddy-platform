package com.example.campusbuddy.service;

import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.vo.UserVO;

import java.util.List;
import java.util.Map;

/**
 * 用户缓存服务接口
 */
public interface UserCacheService {

    void cacheUser(User user);

    User getCachedUser(Long userId);

    User getUserByUsernameFromCache(String username);

    void cacheUserVO(UserVO userVO);

    UserVO getCachedUserVO(Long userId);

    UserVO getUserVOFromCache(Long userId);

    void evictUserCache(Long userId);

    void evictUserCacheByUsername(String username);

    void cacheCreditScore(Long userId, Integer creditScore, long expireSeconds);

    Integer getCachedCreditScore(Long userId);

    void evictCreditScoreCache(Long userId);

    void batchCacheCreditScores(Map<Long, Integer> creditScores, long expireSeconds);

    Map<Long, Integer> getBatchCachedCreditScores(List<Long> userIds);

    void flushAllCache();
}
