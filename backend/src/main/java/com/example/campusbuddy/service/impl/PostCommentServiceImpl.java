package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusbuddy.entity.GroupPost;
import com.example.campusbuddy.entity.PostComment;
import com.example.campusbuddy.mapper.GroupPostMapper;
import com.example.campusbuddy.mapper.PostCommentMapper;
import com.example.campusbuddy.service.GroupPostCacheService;
import com.example.campusbuddy.service.PostCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class PostCommentServiceImpl extends ServiceImpl<PostCommentMapper, PostComment> implements PostCommentService {

    @Autowired
    private GroupPostMapper groupPostMapper;
    
    @Autowired
    private GroupPostCacheService postCacheService;

    @Override
    @Transactional
    public Long addComment(PostComment comment) {
        // 设置初始值
        comment.setStatus("PUBLISHED");
        comment.setCreatedAt(new Date());
        comment.setUpdatedAt(new Date());
        
        // 保存评论
        save(comment);
        
        // 更新帖子评论计数
        GroupPost post = groupPostMapper.selectById(comment.getPostId());
        if (post != null) {
            post.setCommentCount(post.getCommentCount() == null ? 1 : post.getCommentCount() + 1);
            post.setUpdatedAt(new Date());
            groupPostMapper.updateById(post);
            
            // 清除相关缓存，因为评论数发生了变化
            postCacheService.evictPostDetailCache(comment.getPostId());
            postCacheService.evictGroupPostsCache(post.getGroupId());
            postCacheService.evictHotPostsCache();
        }
        
        return comment.getCommentId();
    }

    @Override
    @Transactional
    public boolean deleteComment(Long commentId, Long userId) {
        // 获取评论信息
        PostComment comment = getById(commentId);
        if (comment == null) {
            return false;
        }
        
        // 检查是否是作者
        boolean isAuthor = comment.getUserId().equals(userId);
        if (!isAuthor) {
            // 这里可以添加额外的权限检查，比如是否是管理员
            return false;
        }
        
        // 逻辑删除评论
        comment.setStatus("DELETED");
        comment.setUpdatedAt(new Date());
        boolean result = updateById(comment);
        
        // 更新帖子评论计数
        if (result) {
            GroupPost post = groupPostMapper.selectById(comment.getPostId());
            if (post != null && post.getCommentCount() > 0) {
                post.setCommentCount(post.getCommentCount() - 1);
                post.setUpdatedAt(new Date());
                groupPostMapper.updateById(post);
                
                // 清除相关缓存，因为评论数发生了变化
                postCacheService.evictPostDetailCache(comment.getPostId());
                postCacheService.evictGroupPostsCache(post.getGroupId());
                postCacheService.evictHotPostsCache();
            }
        }
        
        return result;
    }

    @Override
    public IPage<PostComment> getPostComments(Long postId, Integer pageNum, Integer pageSize) {
        // 构建查询条件
        LambdaQueryWrapper<PostComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PostComment::getPostId, postId)
                .eq(PostComment::getStatus, "PUBLISHED")
                .orderByAsc(PostComment::getCreatedAt);
        
        // 执行分页查询
        return page(new Page<>(pageNum, pageSize), queryWrapper);
    }

    @Override
    public PostComment getCommentDetail(Long commentId) {
        return getById(commentId);
    }
}
