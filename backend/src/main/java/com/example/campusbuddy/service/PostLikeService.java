package com.example.campusbuddy.service;

import com.example.campusbuddy.entity.PostLike;
import com.baomidou.mybatisplus.extension.service.IService;

public interface PostLikeService extends IService<PostLike> {
    /**
     * 检查用户是否已经点赞过该帖子
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 是否已点赞
     */
    boolean isLiked(Long postId, Long userId);
    
    /**
     * 添加点赞记录
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean addLike(Long postId, Long userId);
    
    /**
     * 移除点赞记录
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean removeLike(Long postId, Long userId);
}
