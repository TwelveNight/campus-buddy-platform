package com.example.campusbuddy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campusbuddy.entity.PostComment;

public interface PostCommentService extends IService<PostComment> {
    // 添加评论
    Long addComment(PostComment comment);
    
    // 删除评论
    boolean deleteComment(Long commentId, Long userId);
    
    // 分页获取帖子评论
    IPage<PostComment> getPostComments(Long postId, Integer pageNum, Integer pageSize);
    
    // 获取评论详情
    PostComment getCommentDetail(Long commentId);
    
    // 编辑评论
    boolean updateComment(Long commentId, Long userId, String content);
}
