package com.example.campusbuddy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.campusbuddy.entity.GroupPost;
import com.example.campusbuddy.entity.User;
import com.example.campusbuddy.vo.GroupPostVO;

import java.util.List;
import java.util.Map;

/**
 * 帖子缓存服务接口
 */
public interface GroupPostCacheService {
    
    /**
     * 缓存小组帖子列表
     * 
     * @param groupId 小组ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param page 帖子分页结果
     * @param expireSeconds 过期时间(秒)
     */
    void cacheGroupPosts(Long groupId, Integer pageNum, Integer pageSize, IPage<GroupPostVO> page, long expireSeconds);
    
    /**
     * 获取缓存的小组帖子列表
     * 
     * @param groupId 小组ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 帖子分页结果，未命中缓存返回null
     */
    IPage<GroupPostVO> getCachedGroupPosts(Long groupId, Integer pageNum, Integer pageSize);
    
    /**
     * 缓存帖子详情
     * 
     * @param postId 帖子ID
     * @param postVO 帖子VO对象
     * @param expireSeconds 过期时间(秒)
     */
    void cachePostDetail(Long postId, GroupPostVO postVO, long expireSeconds);
    
    /**
     * 获取缓存的帖子详情
     * 
     * @param postId 帖子ID
     * @return 帖子VO对象，未命中缓存返回null
     */
    GroupPostVO getCachedPostDetail(Long postId);
    
    /**
     * 批量缓存用户信息
     * 
     * @param userMap 用户ID到用户对象的映射
     * @param expireSeconds 过期时间(秒)
     */
    void batchCacheUsers(Map<Long, User> userMap, long expireSeconds);
    
    /**
     * 获取缓存的用户信息
     * 
     * @param userId 用户ID
     * @return 用户对象，未命中缓存返回null
     */
    User getCachedUser(Long userId);
    
    /**
     * 批量获取缓存的用户信息
     * 
     * @param userIds 用户ID列表
     * @return 用户ID到用户对象的映射
     */
    Map<Long, User> getBatchCachedUsers(List<Long> userIds);
    
    /**
     * 缓存热门帖子列表
     * 
     * @param posts 热门帖子列表
     * @param expireSeconds 过期时间(秒)
     */
    void cacheHotPosts(List<GroupPostVO> posts, long expireSeconds);
    
    /**
     * 获取缓存的热门帖子列表
     * 
     * @return 热门帖子列表，未命中缓存返回null
     */
    List<GroupPostVO> getCachedHotPosts();
    
    /**
     * 清除小组帖子列表缓存
     * 
     * @param groupId 小组ID，如果为null则清除所有小组的帖子列表缓存
     */
    void evictGroupPostsCache(Long groupId);
    
    /**
     * 清除帖子详情缓存
     * 
     * @param postId 帖子ID
     */
    void evictPostDetailCache(Long postId);
    
    /**
     * 清除用户缓存
     * 
     * @param userId 用户ID
     */
    void evictUserCache(Long userId);
    
    /**
     * 清除热门帖子缓存
     */
    void evictHotPostsCache();
    
    /**
     * 清除所有帖子相关缓存
     */
    void evictAllPostsCache();
}
