package com.example.campusbuddy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusbuddy.entity.Group;
import com.example.campusbuddy.entity.GroupMember;
import com.example.campusbuddy.entity.GroupPost;
import com.example.campusbuddy.mapper.GroupMapper;
import com.example.campusbuddy.mapper.GroupMemberMapper;
import com.example.campusbuddy.mapper.GroupPostMapper;
import com.example.campusbuddy.service.GroupPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class GroupPostServiceImpl extends ServiceImpl<GroupPostMapper, GroupPost> implements GroupPostService {

    @Autowired
    private GroupMemberMapper groupMemberMapper;
    
    @Autowired
    private GroupMapper groupMapper;

    @Override
    public IPage<GroupPost> queryGroupPosts(Long groupId, Integer pageNum, Integer pageSize) {
        // 构建查询条件
        LambdaQueryWrapper<GroupPost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupPost::getGroupId, groupId)
                .eq(GroupPost::getStatus, "PUBLISHED")
                .orderByDesc(GroupPost::getCreatedAt);
        
        // 执行分页查询
        return page(new Page<>(pageNum, pageSize), queryWrapper);
    }

    @Override
    @Transactional
    public Long createPost(GroupPost post) {
        // 设置初始值
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setStatus("PUBLISHED");
        post.setCreatedAt(new Date());
        post.setUpdatedAt(new Date());
        
        // 保存帖子
        save(post);
        
        // 更新小组帖子计数
        Group group = groupMapper.selectById(post.getGroupId());
        if (group != null) {
            group.setPostCount(group.getPostCount() + 1);
            group.setUpdatedAt(new Date());
            groupMapper.updateById(group);
        }
        
        return post.getPostId();
    }

    @Override
    public boolean updatePost(GroupPost post) {
        // 检查用户是否有权限更新
        GroupPost original = getById(post.getPostId());
        if (original == null || !original.getAuthorId().equals(post.getAuthorId())) {
            return false; // 非作者无权更新
        }
        
        post.setUpdatedAt(new Date());
        return updateById(post);
    }

    @Override
    @Transactional
    public boolean deletePost(Long postId, Long userId) {
        // 获取帖子信息
        GroupPost post = getById(postId);
        if (post == null) {
            return false;
        }
        
        // 检查是否是作者或管理员
        boolean isAuthor = post.getAuthorId().equals(userId);
        
        if (!isAuthor) {
            // 检查是否是小组管理员或创建者
            LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(GroupMember::getGroupId, post.getGroupId())
                    .eq(GroupMember::getUserId, userId)
                    .in(GroupMember::getRole, "ADMIN", "CREATOR");
            
            if (groupMemberMapper.selectCount(queryWrapper) == 0) {
                return false; // 既不是作者也不是管理员，无权删除
            }
        }
        
        // 逻辑删除帖子
        post.setStatus("DELETED");
        post.setUpdatedAt(new Date());
        boolean result = updateById(post);
        
        // 更新小组帖子计数
        if (result) {
            Group group = groupMapper.selectById(post.getGroupId());
            if (group != null && group.getPostCount() > 0) {
                group.setPostCount(group.getPostCount() - 1);
                group.setUpdatedAt(new Date());
                groupMapper.updateById(group);
            }
        }
        
        return result;
    }

    @Override
    public GroupPost getPostDetail(Long postId) {
        return getById(postId);
    }

    @Override
    public boolean likePost(Long postId, Long userId) {
        // 在实际应用中，这里应该先检查用户是否已经点过赞
        // 为简化代码，这里直接增加点赞数
        GroupPost post = getById(postId);
        if (post == null) {
            return false;
        }
        
        post.setLikeCount(post.getLikeCount() + 1);
        post.setUpdatedAt(new Date());
        return updateById(post);
    }

    @Override
    public boolean unlikePost(Long postId, Long userId) {
        // 在实际应用中，这里应该先检查用户是否已经点过赞
        // 为简化代码，这里直接减少点赞数
        GroupPost post = getById(postId);
        if (post == null || post.getLikeCount() <= 0) {
            return false;
        }
        
        post.setLikeCount(post.getLikeCount() - 1);
        post.setUpdatedAt(new Date());
        return updateById(post);
    }
}
