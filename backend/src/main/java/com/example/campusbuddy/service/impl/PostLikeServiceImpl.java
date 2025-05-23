package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusbuddy.entity.PostLike;
import com.example.campusbuddy.mapper.PostLikeMapper;
import com.example.campusbuddy.service.PostLikeService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PostLikeServiceImpl extends ServiceImpl<PostLikeMapper, PostLike> implements PostLikeService {

    @Override
    public boolean isLiked(Long postId, Long userId) {
        LambdaQueryWrapper<PostLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, userId);
        return count(queryWrapper) > 0;
    }

    @Override
    public boolean addLike(Long postId, Long userId) {
        // 如果已经点赞过，不重复添加
        if (isLiked(postId, userId)) {
            return false;
        }
        
        PostLike postLike = new PostLike();
        postLike.setPostId(postId);
        postLike.setUserId(userId);
        postLike.setCreatedAt(new Date());
        return save(postLike);
    }

    @Override
    public boolean removeLike(Long postId, Long userId) {
        LambdaQueryWrapper<PostLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, userId);
        return remove(queryWrapper);
    }
}
