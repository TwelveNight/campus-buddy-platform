package com.example.campusbuddy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.campusbuddy.vo.GroupPostVO;

/**
 * 帖子缓存服务接口
 */
public interface GroupPostCacheService {

    void cacheGroupPosts(Long groupId, Integer pageNum, Integer pageSize, IPage<GroupPostVO> page, long expireSeconds);

    IPage<GroupPostVO> getCachedGroupPosts(Long groupId, Integer pageNum, Integer pageSize);

    void cachePostDetail(Long postId, GroupPostVO postVO, long expireSeconds);

    GroupPostVO getCachedPostDetail(Long postId);

    void evictGroupPostsCache(Long groupId);

    void evictPostDetailCache(Long postId);

    void evictAllPostsCache();
}
