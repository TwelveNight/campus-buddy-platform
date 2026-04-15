package com.example.campusbuddy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campusbuddy.entity.PostComment;

import java.util.List;

public interface PostCommentService extends IService<PostComment> {
    // 添加评论
    Long addComment(PostComment comment);

    // 删除评论
    boolean deleteComment(Long commentId, Long userId);

    // 分页获取帖子顶层评论
    IPage<PostComment> getPostComments(Long postId, Integer pageNum, Integer pageSize);

    // 批量获取子评论（回复）
    List<PostComment> getRepliesByParentIds(List<Long> parentIds);

    // 获取评论详情
    PostComment getCommentDetail(Long commentId);

    // 编辑评论
    boolean updateComment(Long commentId, Long userId, String content);
}
