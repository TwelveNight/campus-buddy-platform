package com.example.campusbuddy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campusbuddy.entity.GroupPost;

public interface GroupPostService extends IService<GroupPost> {
    // 分页查询小组帖子
    IPage<GroupPost> queryGroupPosts(Long groupId, Integer pageNum, Integer pageSize);
    
    // 发表帖子
    Long createPost(GroupPost post);
    
    // 更新帖子
    boolean updatePost(GroupPost post);
    
    // 删除帖子
    boolean deletePost(Long postId, Long userId);
    
    // 获取帖子详情
    GroupPost getPostDetail(Long postId);
    
    // 点赞帖子
    boolean likePost(Long postId, Long userId);
    
    // 取消点赞
    boolean unlikePost(Long postId, Long userId);
    
    // 增加评论数
    void increaseCommentCount(Long postId);
    
    // 减少评论数
    void decreaseCommentCount(Long postId);
    
    // =============== 管理员方法 ===============
    // 管理员分页查询帖子
    Page<GroupPost> adminPagePosts(Integer page, Integer size, String keyword, Long groupId, String status);
    
    // 管理员更新帖子状态
    boolean adminUpdatePostStatus(Long postId, String status);
    
    // 管理员删除帖子
    boolean adminDeletePost(Long postId);
}
