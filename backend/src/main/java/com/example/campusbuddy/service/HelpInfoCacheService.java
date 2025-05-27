package com.example.campusbuddy.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusbuddy.entity.HelpInfo;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.vo.HelpInfoDetailVO;
import com.example.campusbuddy.vo.HelpInfoVO;

import java.util.List;
import java.util.Map;

/**
 * 互助信息缓存服务接口
 */
public interface HelpInfoCacheService {
    
    /**
     * 缓存互助信息列表
     */
    void cacheHelpInfoList(String cacheKey, Page<HelpInfo> helpInfoPage, long expireSeconds);
    
    /**
     * 获取缓存的互助信息列表
     */
    Page<HelpInfo> getCachedHelpInfoList(String cacheKey);
    
    /**
     * 缓存互助信息详情
     */
    void cacheHelpInfoDetail(Long helpInfoId, HelpInfoDetailVO helpInfoDetail, long expireSeconds);
    
    /**
     * 获取缓存的互助信息详情
     */
    HelpInfoDetailVO getCachedHelpInfoDetail(Long helpInfoId);
    
    /**
     * 缓存用户信息
     */
    void cacheUser(Long userId, User user, long expireSeconds);
    
    /**
     * 批量缓存用户信息
     */
    void cacheUsers(Map<Long, User> userMap, long expireSeconds);
    
    /**
     * 获取缓存的用户信息
     */
    User getCachedUser(Long userId);
    
    /**
     * 批量获取缓存的用户信息
     */
    Map<Long, User> getCachedUsers(List<Long> userIds);
    
    /**
     * 缓存管理员查询结果
     */
    void cacheAdminHelpInfoList(String cacheKey, Page<HelpInfoVO> helpInfoPage, long expireSeconds);
    
    /**
     * 获取缓存的管理员查询结果
     */
    Page<HelpInfoVO> getCachedAdminHelpInfoList(String cacheKey);
    
    /**
     * 缓存搜索结果
     */
    void cacheSearchResults(String searchKey, Page<HelpInfo> helpInfoPage, long expireSeconds);
    
    /**
     * 获取缓存的搜索结果
     */
    Page<HelpInfo> getCachedSearchResults(String searchKey);
    
    /**
     * 批量更新浏览量计数（延迟写入）
     */
    void incrementViewCount(Long helpInfoId);
    
    /**
     * 刷新待更新的浏览量到数据库
     */
    void flushViewCounts();
    
    /**
     * 清除互助信息相关缓存
     */
    void clearHelpInfoCache(Long helpInfoId);
    
    /**
     * 清除互助信息列表缓存
     */
    void clearHelpInfoListCache();
    
    /**
     * 清除用户缓存
     */
    void clearUserCache(Long userId);
    
    /**
     * 清除所有互助信息缓存
     */
    void clearAllHelpInfoCache();
    
    /**
     * 生成列表缓存键
     */
    String generateListCacheKey(long page, long size, String type, String status, 
                               String publisherId, String keyword);
    
    /**
     * 生成搜索缓存键
     */
    String generateSearchCacheKey(String keyword, String type, String status, 
                                 long page, long size);
    
    /**
     * 生成管理员查询缓存键
     */
    String generateAdminCacheKey(Integer page, Integer size, String keyword, 
                                String type, String status);
}
