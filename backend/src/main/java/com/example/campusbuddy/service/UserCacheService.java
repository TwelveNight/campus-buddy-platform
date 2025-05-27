package com.example.campusbuddy.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.vo.UserVO;

import java.util.List;
import java.util.Map;

/**
 * 用户缓存服务接口
 */
public interface UserCacheService {
    
    /**
     * 缓存用户信息
     * @param user 用户实体
     */
    void cacheUser(User user);
    
    /**
     * 从缓存获取用户信息
     * @param userId 用户ID
     * @return 用户实体
     */
    User getCachedUser(Long userId);
    
    /**
     * 根据用户名从缓存获取用户信息
     * @param username 用户名
     * @return 用户实体
     */
    User getUserByUsernameFromCache(String username);
    
    /**
     * 缓存用户VO信息
     * @param userVO 用户VO
     */
    void cacheUserVO(UserVO userVO);
    
    /**
     * 从缓存获取用户VO信息
     * @param userId 用户ID
     * @return 用户VO
     */
    UserVO getCachedUserVO(Long userId);
    
    /**
     * 从缓存获取用户VO信息（别名方法）
     * @param userId 用户ID
     * @return 用户VO
     */
    UserVO getUserVOFromCache(Long userId);
    
    /**
     * 删除用户缓存
     * @param userId 用户ID
     */
    void evictUserCache(Long userId);
    
    /**
     * 缓存用户登录token
     * @param userId 用户ID
     * @param token JWT token
     * @param expireSeconds 过期时间（秒）
     */
    void cacheUserToken(Long userId, String token, long expireSeconds);
    
    /**
     * 获取用户登录token
     * @param userId 用户ID
     * @return JWT token
     */
    String getCachedUserToken(Long userId);
    
    /**
     * 获取用户登录token（别名方法）
     * @param userId 用户ID
     * @return JWT token
     */
    String getUserTokenFromCache(Long userId);
    
    /**
     * 删除用户token缓存
     * @param userId 用户ID
     */
    void evictUserToken(Long userId);
    
    /**
     * 缓存用户搜索结果
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页大小
     * @param userList 搜索结果
     * @param expireSeconds 过期时间（秒）
     */
    void cacheSearchResult(String keyword, Integer page, Integer size, List<UserVO> userList, long expireSeconds);
    
    /**
     * 缓存分页搜索结果
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页大小
     * @param result 分页搜索结果
     * @param expireSeconds 过期时间（秒）
     */
    void cacheSearchResult(String keyword, Integer page, Integer size, Page<UserVO> result, long expireSeconds);
    
    /**
     * 获取缓存的搜索结果
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    List<UserVO> getCachedSearchResult(String keyword, Integer page, Integer size);
    
    /**
     * 获取缓存的分页搜索结果
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页大小
     * @return 分页搜索结果
     */
    Page<UserVO> getSearchResultFromCache(String keyword, Integer page, Integer size);
    
    /**
     * 清空用户搜索缓存
     */
    void evictSearchCache();
    
    /**
     * 缓存用户信用积分
     * @param userId 用户ID
     * @param creditScore 信用积分
     * @param expireSeconds 过期时间（秒）
     */
    void cacheCreditScore(Long userId, Integer creditScore, long expireSeconds);
    
    /**
     * 从缓存获取用户信用积分
     * @param userId 用户ID
     * @return 信用积分，如果缓存中不存在则返回null
     */
    Integer getCachedCreditScore(Long userId);
    
    /**
     * 删除用户信用积分缓存
     * @param userId 用户ID
     */
    void evictCreditScoreCache(Long userId);
    
    /**
     * 批量缓存用户信用积分
     * @param creditScores 用户ID到信用积分的映射
     * @param expireSeconds 过期时间（秒）
     */
    void batchCacheCreditScores(Map<Long, Integer> creditScores, long expireSeconds);
    
    /**
     * 批量获取用户信用积分
     * @param userIds 用户ID列表
     * @return 用户ID到信用积分的映射
     */
    Map<Long, Integer> getBatchCachedCreditScores(List<Long> userIds);
}
